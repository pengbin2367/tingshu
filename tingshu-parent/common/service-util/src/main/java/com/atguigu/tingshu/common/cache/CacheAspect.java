package com.atguigu.tingshu.common.cache;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class CacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.atguigu.tingshu.common.cache.GuiguCache)")
    public Object cacheAspect(ProceedingJoinPoint joinPoint) {
        Object result = new Object();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        GuiguCache guiguCache = method.getAnnotation(GuiguCache.class);
        String key = guiguCache.prefix();
        Object[] args = joinPoint.getArgs();
        key += Arrays.asList(args).toString();
        String redisData = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotEmpty(redisData)) {
            return JSONObject.parseObject(redisData, method.getReturnType());
        }
        RLock lock = redissonClient.getLock(key + ":lock");
        if (lock.tryLock(100, 100, TimeUnit.SECONDS)) {
            try {
                result = joinPoint.proceed(args);
                if (result == null) {
                    Class<?> returnType = method.getReturnType();
                    result = returnType.getConstructor().newInstance();
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 5, TimeUnit.MINUTES);
                } else {
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 1, TimeUnit.DAYS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("切面缓存操作数据库异常：{}", e.getMessage());
            } finally {
                lock.unlock();
            }
        }
        return result;
    }
}
