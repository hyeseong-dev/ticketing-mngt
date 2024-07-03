package com.mgnt.common.util.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckAccess {
    String[] roles() default {"USER"};

    String resourceType();
}
