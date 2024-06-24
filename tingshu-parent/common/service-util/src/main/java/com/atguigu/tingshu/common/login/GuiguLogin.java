package com.atguigu.tingshu.common.login;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GuiguLogin {

    boolean isLogin() default true;
}
