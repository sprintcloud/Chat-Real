package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager implements AutoCloseable {
    private JedisPool jedisPool;
    
    public RedisManager(String host, int port, String password) {
        this.jedisPool = new JedisPool(
                new JedisPoolConfig(),
                host,
                port,
                2000,
                password
        );
    }
    
    public Jedis getResource() {
        return jedisPool.getResource();
    }
    
    @Override
    public void close() {
        jedisPool.close();
    }
}
