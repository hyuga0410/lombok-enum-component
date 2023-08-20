package io.github.hyuga0410.lombok.enums.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import io.github.hyuga0410.lombok.enums.annotations.EnumDesc;
import io.github.hyuga0410.lombok.enums.constants.EnumConstants;
import io.github.hyuga0410.lombok.enums.parent.Parent;
import sun.misc.Unsafe;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * AgentProcessor
 * <p>
 * 注解处理器（APT）
 * <p>
 * APT 全称为 Annotation Processing Tool，可翻译为注解处理器，APT 工具是用于注解处理的命令行程序，它可以找到源码中对应注解的对象并使用注解处理器对其进行处理。
 * 一般来说，我们会使用 APT 生成一些源码，然后加入编译目录进行编译，从而简化开发周期。
 *
 * @author pengqinglong
 * @since 2022/5/9
 */
public abstract class AgentProcessor extends AbstractProcessor {

    /**
     * 提供tree的实现。<br>
     * 这不是任何受支持的API的一部分。如果您编写的代码依赖于此，那么您将自担风险。本代码及其内部接口可随时更改或删除，恕不另行通知。
     */
    protected JavacTrees javacTrees;
    /**
     * tree的工厂类。<br>
     * 这不是任何受支持的API的一部分。如果您编写的代码依赖于此，那么您将自担风险。本代码及其内部接口可随时更改或删除，恕不另行通知。
     */
    protected TreeMaker treeMaker;
    /**
     * 访问编译器的名称表。定义了标准名称，以及创建新名称的方法。
     * <p>
     * 这不是任何受支持的API的一部分。如果您编写依赖于此的代码，您将自行承担风险。此代码及其内部接口如有更改或删除，恕不另行通知。
     */
    protected Names names;

    /**
     * 通过将{@code processingEnv}字段设置为{@code processingEnv}参数的值，用处理环境初始化处理器。
     * <p>
     * 如果在同一对象上多次调用此方法，将抛出{@code IllegalStateException}。
     *
     * @param processingEnv 访问工具框架提供给处理器的设施的环境
     * @throws IllegalStateException 如果此方法被多次调用。
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.javacTrees = JavacTrees.instance(processingEnv);

        Method[] declaredMethods = processingEnv.getClass().getDeclaredMethods();

        for (Method declaredMethod : declaredMethods) {
            if (EnumConstants.GET_CONTEXT.equals(declaredMethod.getName())) {
                try {
                    Context context = (Context) declaredMethod.invoke(processingEnv, (Object[]) null);
                    this.treeMaker = TreeMaker.instance(context);
                    this.names = Names.instance(context);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 如果处理器类用{@link SupportedSourceVersion}注释，请在注释中返回源版本。如果类没有如此注释，则返回{@link SourceVersion#RELEASE_6}。
     *
     * @return 该处理器支持的最新源版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (EnumConstants.VERSION == EnumConstants.JDK_17) {
            return SourceVersion.valueOf(EnumConstants.RELEASE_17);
        }
        return SourceVersion.valueOf(EnumConstants.RELEASE_11);
    }

    /**
     * 需要处理的注解类型
     * <p>
     * 如果处理器类用{@link SupportedAnnotationTypes}注释，则返回一个不可修改的集合，其字符串集与注释相同。如果类没有如此注释，则返回一个空集。
     * <p>
     * 如果{@linkplain ProcessingEnvironment#getSourceVersion()}不支持模块，
     * 换句话说，如果它小于或等于{@link SourceVersion#RELEASE_8 RELEASE_8}，
     * 那么任何前导的{@linkplain Processor#getSupportedAnnotationTypes()}都会从名称中删除。
     *
     * @return 此处理器支持的注释接口的名称，如果没有，则为空集.
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getAnnotationTypes();
    }

    /**
     * 需要处理的注解类型
     * <p>
     * 如果处理器类用{@link SupportedAnnotationTypes}注释，则返回一个不可修改的集合，其字符串集与注释相同。如果类没有如此注释，则返回一个空集。
     * <p>
     * 如果{@linkplain ProcessingEnvironment#getSourceVersion()}不支持模块，
     * 换句话说，如果它小于或等于{@link SourceVersion#RELEASE_8 RELEASE_8}，
     * 那么任何前导的{@linkplain Processor#getSupportedAnnotationTypes()}都会从名称中删除。
     *
     * @return 此处理器支持的注释接口的名称，如果没有，则为空集.
     */
    protected abstract Set<String> getAnnotationTypes();

    /**
     * 注解类型比对
     */
    protected boolean jcEquals(JCTree.JCAnnotation annotation) {
        return annotation.type.toString().equals(EnumDesc.class.getCanonicalName());
    }

    /**
     * 字段类型比对
     */
    protected boolean typeEquals(JCTree.JCVariableDecl jcVariableDecl) {
        Type type = jcVariableDecl.getType().type;
        if (!(type instanceof Type.ClassType)) {
            return false;
        }
        Type.ClassType classType = (Type.ClassType) jcVariableDecl.getType().type;
        classType = (Type.ClassType) classType.baseType();

        if (classType.supertype_field == null) {
            return false;
        }
        return classType.supertype_field.baseType().tsym.toString().equals(Enum.class.getCanonicalName());
    }

    /**
     * 字符串首字母大写
     */
    protected String upperCase(String str) {
        String suffix;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        bytes[0] = (byte) (bytes[0] - 32);
        suffix = new String(bytes);
        return suffix;
    }

    /**
     * 开启代理
     */
    public static void addOpensForAgent() {
        Class<?> cModule;
        try {
            cModule = Class.forName(EnumConstants.JAVA_LANG_MODULE);
        } catch (ClassNotFoundException e) {
            return;
        }
        Unsafe unsafe = getUnsafe();
        Object jdkCompilerModule = getJdkCompilerModule();
        Module ownModule = AgentProcessor.class.getModule();
        String[] allPackages = {
                "com.sun.tools.javac.code",
                "com.sun.tools.javac.comp",
                "com.sun.tools.javac.file",
                "com.sun.tools.javac.main",
                "com.sun.tools.javac.model",
                "com.sun.tools.javac.parser",
                "com.sun.tools.javac.processing",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util",
                "com.sun.tools.javac.jvm",
                "com.sun.tools.javac.api"
        };

        try {
            Method m = cModule.getDeclaredMethod(EnumConstants.IMPL_ADD_OPENS, String.class, cModule);
            assert unsafe != null;
            long firstFieldOffset = getFirstFieldOffset(unsafe);
            unsafe.putBooleanVolatile(m, firstFieldOffset, true);
            for (String p : allPackages) {
                m.invoke(jdkCompilerModule, p, ownModule);
            }
        } catch (Exception ignore) {
            System.out.println(1);
        }
    }

    private static long getFirstFieldOffset(Unsafe unsafe) {
        try {
            return unsafe.objectFieldOffset(Parent.class.getDeclaredField(EnumConstants.FIRST));
        } catch (NoSuchFieldException e) {
            // can't happen.
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            // can't happen
            throw new RuntimeException(e);
        }
    }

    private static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField(EnumConstants.THE_UNSAFE);
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取JDK编译模块
     */
    public static Object getJdkCompilerModule() {
        try {
            Class<?> cModuleLayer = Class.forName(EnumConstants.JAVA_LANG_MODULE_LAYER);
            Method mBoot = cModuleLayer.getDeclaredMethod(EnumConstants.BOOT);
            Object bootLayer = mBoot.invoke(null);
            Class<?> cOptional = Class.forName(EnumConstants.JAVA_UTIL_OPTIONAL);
            Method mFindModule = cModuleLayer.getDeclaredMethod(EnumConstants.FIND_MODULE, String.class);
            Object oCompilerO = mFindModule.invoke(bootLayer, EnumConstants.JDK_COMPILER);
            return cOptional.getDeclaredMethod(EnumConstants.GET).invoke(oCompilerO);
        } catch (Exception e) {
            return null;
        }
    }

}