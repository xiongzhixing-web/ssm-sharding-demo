package com.soecode.lyf.util.api;

import java.lang.annotation.*;

/**
 * 文档生成注解
 */
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocAnnotation {
    //参数说明
    String comment() default "";
    //是否必填
    boolean isFill() default false;
    //参数名：不是必填
    String name() default "";
}
