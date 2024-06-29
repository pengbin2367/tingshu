package com.atguigu.tingshu.common.login;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.common.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;

@Component
@Aspect
public class LoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

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
        // 测试方便，不进行拦截
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

        // 校验token
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(SystemConstant.PUBLIC_KEY));
        // 判断盗用问题：ip/设备绑定
        String ipAddress = IpUtil.getIpAddress(request);
        Object o = redisTemplate.opsForValue().get("User_Login_Info_" + ipAddress);
        if (o == null || !o.equals(token)) {
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        // 获取载荷
        String claims = jwt.getClaims();
        // 反序列化
        Map<String, String> map = JSONObject.parseObject(claims, Map.class);
        String userId = map.get("userId");
        int role = Integer.parseInt(map.get("role"));
        int roleInfo = guiguLogin.role();
        if (role < roleInfo) {
            throw new GuiguException(ResultCodeEnum.PERMISSION);
        }

        // FIXME 存储到本地线程 测试方便，使用 1
        AuthContextHolder.setUserId(1L);
//        AuthContextHolder.setUserId(Long.valueOf(userId));

        // 令牌到期时间
        Long eTimes = Long.valueOf(map.get("e_times"));
        Long time = System.currentTimeMillis() - eTimes;
        // 剩余有效期大于12小时，延长token过期时间
        if (time >= 3600000 * 12) {
            // redisTemplate.expire("User_Login_Info_" + ipAddress, 30, TimeUnit.MINUTES);
            // 抛给前端这个异常，前端会停止等待当前请求并发起刷新令牌的请求，令牌刷新后，再发起当前请求
            throw new GuiguException(ResultCodeEnum.SIGN_OVERDUE);
        }

        Object result = point.proceed(args);
        AuthContextHolder.removeUserId();
        return result;
    }
}
