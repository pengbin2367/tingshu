package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public synchronized void setRedis() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 1, TimeUnit.MINUTES);
        if (lock) {
            Integer redisValue = (Integer) redisTemplate.opsForValue().get("tingshu_test");
            if (redisValue != null) {
                redisValue++;
                redisTemplate.opsForValue().set("tingshu_test", redisValue);
            }
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
            script.setResultType(Long.class);
            redisTemplate.execute(script, List.of("lock"), uuid);
//            String redisUuid = (String) redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(redisUuid)) {
//                redisTemplate.delete("lock");
//            }
        } else {
            setRedis();
        }
    }
}
