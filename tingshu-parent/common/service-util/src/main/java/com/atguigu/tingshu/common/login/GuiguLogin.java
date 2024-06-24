package com.atguigu.tingshu.common.login;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GuiguLogin {

    boolean isLogin() default true;

    // 访问权限：0-普通会员 1-Vip会员 2-超级管理员
    int role() default 0;
}
