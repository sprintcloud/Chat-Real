package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

public class ChatPublisher {
    private Jedis jedis;

    public ChatPublisher() {
        this.jedis = RedisManager.getConnection();
    }

    public void sendMessage(String user, String message) {
        jedis.publish("chat_global", user + ": " + message);
    }
}
