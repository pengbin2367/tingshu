package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Component
@Aspect
public class LoginAspect {

    @SneakyThrows
    @Around(value = "@annotation(com.atguigu.tingshu.common.login.GuiguLogin)")
    public Object loginAspect(ProceedingJoinPoint point) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 获取方法对象
        Method method = signature.getMethod();
        // 获取这个方法的注解
        GuiguLogin guiguLogin = method.getAnnotation(GuiguLogin.class);
        // 获取是否需要登陆属性
        Object[] args = point.getArgs();
        if (!guiguLogin.isLogin()) {
            // 不需要则获取方法的参数，直接执行方法返回
            return point.proceed(args);
        }

        // 在实际控制层方法执行前，获取这个用户是否登陆 token请求头
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            // 已登陆，直接执行方法返回
            return point.proceed(args);
        }

        // 未登录获取请求
        HttpServletRequest request = requestAttributes.getRequest();
        // 从请求头中获取token参数
        String token = request.getHeader("token");
        // token 为空引导用户登陆（返回LOGIN_AUTH）
        if (StringUtils.isEmpty(token)) {
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }

        // TODO 校验token
        return point.proceed(args);
    }
}
