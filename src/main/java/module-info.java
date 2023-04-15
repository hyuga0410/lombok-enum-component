module commons {

    requires jdk.compiler;
    requires jdk.unsupported;

    exports cn.hyuga.lombok.enums.processor;
    exports cn.hyuga.lombok.enums.annotations;
    exports cn.hyuga.lombok.enums.constants;

}