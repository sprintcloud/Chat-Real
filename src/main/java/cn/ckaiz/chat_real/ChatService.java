package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Xin Jie, Ibrahim
 */
public class ChatService {
    private static final String CHANNEL = "chat_global";

    public void startChat(String user, RedisManager redisManager){
        try(Jedis jedis = redisManager.getResource()){
            System.out.println("* HISTORIAL *");
            System.out.println(redisManager.obtenerHistorial(CHANNEL));
            System.out.println("* * * * * * * * * + *");
            
            

            jedis.sadd("users_online", user);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try (Jedis hookJedis  = redisManager.getResource()) {
                    hookJedis.srem("users_online", user);
                    System.out.println("\033[31m[Sistema] Usuario eliminado de la lista online.\033[0m");
                }
            }));
            MessageSubscriber subscriber = new MessageSubscriber(user,redisManager);
            
            new Thread(subscriber).start();
            
            handleUserInput(jedis,user);
        }finally {
            try(Jedis jedis = redisManager.getResource()){
                jedis.srem("users_online", user);
            }
        }
    }
    
    private void handleUserInput(Jedis jedis, String user){
        try(Scanner scanner = new Scanner(System.in)){
            System.out.println("\033[H\033[2J");
            System.out.flush();
            while(!Thread.currentThread().isInterrupted()){
                System.out.print("\r" + user + ": ");
                String message = scanner.nextLine().trim();
                
                if ("exit".equalsIgnoreCase(message)) {
                    System.exit(0);
                }
                
                if ("users".equals(message)) {
                    Set<String> users = jedis.smembers("users_online");
                    System.out.println("Usuarios en línea: " +
                            users.stream()
                                    .sorted()
                                    .collect(Collectors.joining(", ")));
                    continue;
                }
                
                publishMessage(jedis, user, message);
            }
        }catch (Exception e) {
            System.err.println("Entrada/salida error: " + e.getMessage());
        } finally {
            try(jedis){
                jedis.srem("users_online", user);
            }
        }
    }
    
    private String formatMessage(String user, String message){
        return String.format("[%s] %s", user, message);
    }
    
    private void publishMessage(Jedis jedis, String user, String message) {
        try {
            jedis.publish(CHANNEL, formatMessage(user, message));
        } catch (Exception e) {
            System.err.println("Publicación fallida: " + e.getMessage());
        }
    }
}
