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

}