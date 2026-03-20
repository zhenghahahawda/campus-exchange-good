package com.campus;

import redis.clients.jedis.Jedis;

public class RedisConnectionTest {
    public static void main(String[] args) {
        Jedis jedis = null;
        try {
            jedis = new Jedis("106.52.174.132", 6379);
            jedis.auth("Redis@1234");

            String pong = jedis.ping();
            System.out.println("Redis 连接成功: " + pong);

            jedis.set("test_key", "test_value");
            String value = jedis.get("test_key");
            System.out.println("测试读写: " + value);

            jedis.del("test_key");
            System.out.println("Redis 测试完成");

        } catch (Exception e) {
            System.err.println("Redis 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
