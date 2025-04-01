package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class ChatSubscriber {
    public static void main(String[] args) {
        Jedis jedis = RedisManager.getConnection();
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Nuevo mensaje recibido: " + message);
            }
        }, "chat_global");
    }
}
