package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

import java.util.Scanner;

public class ChatService {
    private static final String CHANNEL = "chat_global";
    private Historial historial = new Historial();

    public void startChat(String user, RedisManager redisManager){
        try(Jedis subJedis = redisManager.getResource();
            Jedis pubJedis = redisManager.getResource()){
            
            MessageSubscriber subscriber = new MessageSubscriber(subJedis, user);
            
            new Thread(subscriber).start();
            
            handleUserInput(pubJedis,user);

            subJedis.close();
            pubJedis.close();
        }
    }
    
    private void handleUserInput(Jedis jedis, String user){
        try(Scanner scanner = new Scanner(System.in)){

            System.out.println(historial.obtenerHistorial(CHANNEL));

            while(true){
                System.out.print(user+": ");
                String message = scanner.nextLine().trim();
                
                if("exit".equals(message)){
                    System.out.println("""
                            退出聊天...""");
                    break;
                }
                
                jedis.publish(CHANNEL, formatMessage(user,message));
            }
        }
    }
    
    private String formatMessage(String user, String message){
        return String.format("[%s] %s", user, message);
    }
}
