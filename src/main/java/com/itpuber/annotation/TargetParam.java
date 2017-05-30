package com.itpuber.annotation;

import java.lang.annotation.*;

/**
 * Created by yoyo on 17/5/23.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface TargetParam {
}
