package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * @author Xin Jie, Ibrahim
 */
public class MessageSubscriber implements Runnable {
    private final String currentUser;
    private final RedisManager redisManager;
    
    public MessageSubscriber(String currentUser, RedisManager redisManager) {
        this.currentUser = currentUser;
        this.redisManager = redisManager;
    }
    
    @Override
    public void run() {
        try(Jedis jedis = redisManager.getSubscriberResource()){
            jedis.subscribe(createSubscriber(),"m06");
        } catch (JedisConnectionException e){
            System.err.println("Excepción de conexión de suscripción: " + e.getMessage());
        }
    }
    
    private JedisPubSub createSubscriber() {
        return new JedisPubSub() {
         @Override
         public void onMessage(String channel, String message) {
             String[] msg_array = message.split(" ",4);
             if (msg_array[1].startsWith("/sendto") && msg_array.length > 3){
                     String sender = msg_array[0];
                     String received = msg_array[2];
                     String message_pri = msg_array[3];
                     if(currentUser.equals(received)) {
                         System.out.print("\n【Ha llegado un nuevo mensaje privado】"+ sender+ " " + message_pri + "\n");
                         System.out.print(currentUser + ": ");
                     }
                 return;
             }

             String msgUser = message.split(":")[0].trim();
             if (!currentUser.equals(msgUser)) {
                 System.out.print("\n【"+channel+" ha llegado un nuevo menssaje】" + message+"\n");
                 System.out.print(currentUser+": ");
                 redisManager.guardarMensaje(message, channel);
             }
         }
        };
    }
}
