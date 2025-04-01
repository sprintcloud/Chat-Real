package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

public class RedisManager {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static Jedis jedis;

    public static Jedis getConnection() {
        if (jedis == null) {
            jedis = new Jedis(REDIS_HOST, REDIS_PORT);
            System.out.println("Conectado a Redis");
        }
        return jedis;
    }
}
