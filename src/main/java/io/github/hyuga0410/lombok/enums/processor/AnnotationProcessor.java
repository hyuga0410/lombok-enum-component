package io.github.hyuga0410.lombok.enums.processor;

import io.github.hyuga0410.lombok.enums.constants.EnumConstants;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.InputStream;
import java.util.*;

/**
 * 注解处理器 初始类
 *
 * @author pengqinglong
 * @since 2022/5/23
 */
public class AnnotationProcessor extends AbstractProcessor {

    private static final List<AgentProcessor> PROCESSORS = new ArrayList<>();

    private static final String META_INF_PATH = "/META-INF/services/io.github.hyuga0410.lombok.Processors";

    static {
        try {
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

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        for (AgentProcessor processor : PROCESSORS) {
            processor.init(processingEnv);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean flag = false;
        for (AgentProcessor processor : PROCESSORS) {
            flag = flag | processor.process(annotations, roundEnv);
        }
        return flag;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        for (AgentProcessor processor : PROCESSORS) {
            set.addAll(processor.getSupportedAnnotationTypes());
        }
        return set;
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

}
