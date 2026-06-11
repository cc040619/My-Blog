package com.my.blog.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisCache {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 缓存对象
     */
    public <T> void setCacheObject(String key, T value) {
        String json = JSON.toJSONString(value);
        redisTemplate.opsForValue().set(key, json);
    }

    /**
     * 缓存对象并设置过期时间
     */
    public <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit) {
        String json = JSON.toJSONString(value);
        redisTemplate.opsForValue().set(key, json, timeout, unit);
    }

    /**
     * 获取缓存对象
     */
    public <T> T getCacheObject(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * 删除缓存对象
     */
    public void deleteObject(String key) {
        redisTemplate.delete(key);
    }
}
