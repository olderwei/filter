package com.itpuber.annotation;

import com.itpuber.enums.Strategy;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by yoyo on 17/5/20.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TargetMethod {

    String filterKey();

    /**
     * 采取的缓存策略
     * @return
     */
    Strategy strategy() default Strategy.GUAVA_UNIT_LIMIT;

    /**
     * 防刷时间单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 该时间段最大允许次数
     * @return
     */
    long maxCount() default 10000L;

    /**
     * 自定义错误结果
     */
    String failResult();
}
