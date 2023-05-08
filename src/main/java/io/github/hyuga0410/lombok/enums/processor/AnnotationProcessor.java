package io.github.hyuga0410.lombok.enums.processor;

import io.github.hyuga0410.lombok.enums.constants.EnumConstants;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.InputStream;
import java.util.*;

/**
 * 自定义注解处理器 初始类
 * <p>
 * - 继承AbstractProcessor
 * - 创建文件resources/META-INF/services/javax.annotation.processing.Processor
 * - 在文件中写入注解处理器的全称，包括包路径：io.github.hyuga0410.lombok.enums.processor.AnnotationProcessor
 *
 * @author pengqinglong
 * @since 2022/5/23
 */
public class AnnotationProcessor extends AbstractProcessor {

    private static final List<AgentProcessor> PROCESSORS = new ArrayList<>();

    /**
     * 自定义lombok注解处理器路径
     */
    private static final String META_INF_PATH = "/META-INF/services/io.github.hyuga0410.lombok.Processors";

    static {
        try {
            // 开启代理
            AgentProcessor.addOpensForAgent();

            InputStream resourceAsStream = AnnotationProcessor.class.getResourceAsStream(META_INF_PATH);
            Properties properties = new Properties();
            properties.load(resourceAsStream);

            ClassLoader loader = AgentProcessor.class.getClassLoader();
            for (Object value : properties.keySet()) {
                Class<?> loadClass = loader.loadClass(value.toString());
                Object o = loadClass.getDeclaredConstructor().newInstance();
                PROCESSORS.add((AgentProcessor) o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        for (AgentProcessor processor : PROCESSORS) {
            processor.init(processingEnv);
        }
    }

    /**
     * 处理来自上一轮的类型元素的一组注释接口，并返回此处理器是否声称这些注释接口。
     * <p>
     * 如果返回true，则声明注释接口，后续处理器将不会被要求处理它们；
     * <p>
     * 如果返回false，注释接口将无人认领，随后的处理器可能会被要求处理它们。处理器可能始终返回相同的布尔值，或者可能根据自己选择的标准改变结果。
     * <p>
     * 如果处理器支持“x”，并且根元素没有注释，则输入集将是空的。处理器必须优雅地处理一组空的注释。
     *
     * @param annotations 请求处理的注释接口
     * @param roundEnv    有关当前和上一轮的信息的环境
     * @return boolean
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean flag = false;
        for (AgentProcessor processor : PROCESSORS) {
            flag = flag | processor.process(annotations, roundEnv);
        }
        return flag;
    }

    /**
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
        Set<String> set = new HashSet<>();
        for (AgentProcessor processor : PROCESSORS) {
            set.addAll(processor.getSupportedAnnotationTypes());
        }
        return set;
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

}
