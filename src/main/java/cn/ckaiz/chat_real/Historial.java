package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

import java.util.List;

public class Historial {

    private RedisManager redisManager = new RedisManager("localhost", 6379, null);
    private Jedis jedis  = redisManager.getResource();

    public void guardarMensaje(String message,String channel) {
        jedis.lpush("Historial:"+channel, message);
    }

    public List<String> obtenerHistorial( String channel) {

        List<String> mensajes = jedis.lrange("Historial:"+channel,0, 9);

        if (mensajes == null) {
            return null;
        } else {
            return mensajes;
        }
    }
}
