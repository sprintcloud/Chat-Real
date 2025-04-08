package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

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
    
    public List<String> obtenerHistorial(String channel) {
        
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("Channel name cannot be null or empty");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange("Historial:"+channel, 0, 9);
        }catch (Exception e) {
            System.err.println("Error fetching history from Redis: " + e.getMessage());
            return List.of();
        }
    }
    
    public void guardarMensaje(String message,String channel) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush("Historial:"+channel, message);
        }catch (Exception e) {
            System.err.println("Error fetching history from Redis: " + e.getMessage());
        }
    }
}
