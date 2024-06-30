package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public synchronized void setRedis() {
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "setnx");
        if (lock) {
            Integer redisValue = (Integer) redisTemplate.opsForValue().get("tingshu_test");
            if (redisValue != null) {
                redisValue++;
                redisTemplate.opsForValue().set("tingshu_test", redisValue);
            }
            redisTemplate.delete("lock");
        } else {
            setRedis();
        }
    }
}
