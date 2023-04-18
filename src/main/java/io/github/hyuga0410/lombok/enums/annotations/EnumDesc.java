package io.github.hyuga0410.lombok.enums.annotations;

import java.lang.annotation.*;

/**
 * 枚举描述 对类以及字段 自动生成相应的desc方法
 *
 * @author pengqinglong
 * @since 2022/5/9
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface EnumDesc {

    String DESC = "desc";

    /**
     * 注意： <br>
     * - 不支持在Enum类上使用@EnumDesc <br>
     * - 不支持attributes属性为非String类型（编译失败） <br>
     * - @EnumDesc支持在常规类上使用，且类成员变量有枚举变量，且枚举变量的属性中有 {@code attributes} 所包含的常量名和对应的get方法（常规类使用仅desc生效） <br>
     * - @EnumDesc支持在常规类中的枚举成员变量上使用，效果同上，同时支持添加枚举中存在的String类型常量到 {@code attributes}（不可添加非String类型常量和非public方法）
     *
     * @return String[]
     */
    String[] attributes() default {DESC};

}