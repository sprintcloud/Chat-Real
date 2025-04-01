package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {
        Jedis jedis = RedisManager.getConnection();
        jedis.set("usuario1", "esto es una prueba");
        System.out.println("Mensaje guardado: " + jedis.get("usuario1"));
    }

}
