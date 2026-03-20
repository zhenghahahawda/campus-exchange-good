package com.campus.exchange;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        try {
            redisTemplate.opsForValue().set("test_key", "test_value");
            String value = (String) redisTemplate.opsForValue().get("test_key");
            System.out.println("Redis 连接成功，测试读写: " + value);
            redisTemplate.delete("test_key");
        } catch (Exception e) {
            System.err.println("Redis 连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
