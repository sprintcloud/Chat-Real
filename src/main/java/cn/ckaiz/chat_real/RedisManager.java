package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * @author Xin Jie, Ibrahim
 */
public class RedisManager implements AutoCloseable {
    private final JedisPool jedisPool;
    private final JedisPool subscriberPool;
    
    
    public RedisManager(String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(10);
        poolConfig.setMaxTotal(50);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        this.jedisPool = new JedisPool(poolConfig, host, port,2000,password);
        this.subscriberPool = new JedisPool(poolConfig, host, port,2000,password);
    }
    
    public Jedis getResource() {
        return jedisPool.getResource();
    }
    
    public Jedis getSubscriberResource() {
        return subscriberPool.getResource();
    }
    @Override
    public void close() {
        jedisPool.close();
        subscriberPool.close();
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
