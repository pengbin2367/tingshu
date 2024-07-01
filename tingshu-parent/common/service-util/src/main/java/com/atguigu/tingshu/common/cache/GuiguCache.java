package com.atguigu.tingshu.common.cache;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GuiguCache {

    String prefix() default "cache";
}
