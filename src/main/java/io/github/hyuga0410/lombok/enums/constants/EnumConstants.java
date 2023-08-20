package io.github.hyuga0410.lombok.enums.constants;

/**
 * EnumConstants
 *
 * @author pengqinglong
 * @since 2022/5/10
 */
public interface EnumConstants {

    String DESC = "desc";
    String GET_CONTEXT = "getContext";
    int JDK_17 = 17;
    String RELEASE_17 = "RELEASE_17";
    String RELEASE_11 = "RELEASE_11";
    int VERSION = Integer.parseInt(System.getProperty("java.specification.version"));

    String GET = "get";
    String BOOT = "boot";
    String FIRST = "first";
    String THE_UNSAFE = "theUnsafe";
    String FIND_MODULE = "findModule";
    String JDK_COMPILER = "jdk.compiler";
    String IMPL_ADD_OPENS = "implAddOpens";
    String JAVA_LANG_MODULE = "java.lang.Module";
    String JAVA_UTIL_OPTIONAL = "java.util.Optional";
    String JAVA_LANG_MODULE_LAYER = "java.lang.ModuleLayer";

}