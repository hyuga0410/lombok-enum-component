package cn.hyuga.lombok.enums.processor;

import cn.hyuga.lombok.enums.annotations.EnumDesc;
import cn.hyuga.lombok.enums.constants.EnumConstants;
import cn.hyuga.lombok.enums.parent.Parent;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import sun.misc.Unsafe;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
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

    protected JavacTrees javacTrees;
    protected TreeMaker treeMaker;
    protected Names names;

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
     * jdk版本17
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
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getAnnotationTypes();
    }

    /**
     * 需要处理的注解集合
     *
     * @return Set
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
            cModule = Class.forName("java.lang.Module");
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
            Method m = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
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
            return unsafe.objectFieldOffset(Parent.class.getDeclaredField("first"));
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
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
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
            Class<?> cModuleLayer = Class.forName("java.lang.ModuleLayer");
            Method mBoot = cModuleLayer.getDeclaredMethod("boot");
            Object bootLayer = mBoot.invoke(null);
            Class<?> cOptional = Class.forName("java.util.Optional");
            Method mFindModule = cModuleLayer.getDeclaredMethod("findModule", String.class);
            Object oCompilerO = mFindModule.invoke(bootLayer, "jdk.compiler");
            return cOptional.getDeclaredMethod("get").invoke(oCompilerO);
        } catch (Exception e) {
            return null;
        }
    }

}