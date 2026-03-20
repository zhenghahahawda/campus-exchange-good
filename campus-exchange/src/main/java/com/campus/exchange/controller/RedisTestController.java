package com.campus.exchange.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis")
    public String testRedis() {
        try {
            redisTemplate.opsForValue().set("test_key", "连接成功");
            String value = (String) redisTemplate.opsForValue().get("test_key");
            redisTemplate.delete("test_key");
            return "Redis 连接成功！测试值: " + value;
        } catch (Exception e) {
            return "Redis 连接失败: " + e.getMessage();
        }
    }
}
