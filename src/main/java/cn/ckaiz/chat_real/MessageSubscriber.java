package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class MessageSubscriber implements Runnable {
    private final String currentUser;
    private final Jedis jedis;
    private Historial historial;

    public MessageSubscriber(Jedis jedis, String currentUser) {
        this.jedis = jedis;
        this.currentUser = currentUser;
        this.historial = new Historial();
    }
    
    @Override
    public void run() {
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                String msg_user = message.split(" ")[0].trim().replaceAll("^\\[|\\]", "");
                if (!currentUser.equals(msg_user)) {
                    System.out.println("\n【"+channel+" 新消息】：" + message);
                    historial.guardarMensaje(message, channel);
                }
            }
        }, "chat_global");
    }
}
