package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

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
            String channelKey = "Historial:" + channel;
            
            Transaction tx = jedis.multi();
            
            tx.lpush(channelKey, message);
            tx.llen(channelKey);
            
            List<Object> results = tx.exec();
            
            long total = (Long) results.get(1);
            
            int start = Math.max(0, (int)total - 11);
            int end = (int) (Math.min(total, 10) - 1);
            
            tx = jedis.multi();
            tx.ltrim(channelKey, start, end);
            tx.exec();
        }catch (Exception e) {
            System.err.println("Error fetching history from Redis: " + e.getMessage());
        }
    }
    
    public void storeOfflineMessage(String sender,String received,String channel ,String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            String offlineKey = "Offline_Messages:" + received;
            jedis.lpush(offlineKey,message);
            jedis.expire(offlineKey, 7*24*3600);
            String allMessagesKey = "All_Messages:" + channel;
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            String messageId = channel + ":" + timestamp;
            
            jedis.hset(messageId, "sender", sender);
            jedis.hset(messageId, "content", message);
            jedis.hset(messageId, "timestamp", timestamp);
            
            jedis.zadd(allMessagesKey + ":sorted", Long.parseLong(timestamp), messageId);
            
        }
    }
    
    public List<String> getAllMessagesSorted(String channel) {
        try (Jedis jedis = jedisPool.getResource()) {
            String sortedKey = "All_Messages:" + channel + ":sorted";

            return jedis.zrevrange(sortedKey, 0, -1);
        }
    }
}
