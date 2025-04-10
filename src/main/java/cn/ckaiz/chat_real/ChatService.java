package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Xin Jie, Ibrahim
 */
public class ChatService {
    private static final String CHANNEL = "m06";
    private static final String ONLINE_USERS = "online_users";
    private final RedisManager redisManager;
    private final String CurrentUser;
    
    public ChatService(RedisManager redisManager, String user) {
        this.redisManager = redisManager;
        this.CurrentUser = user;
    }
    
    public void userOnline(String user) {
        try(Jedis jedis = redisManager.getResource()){
            jedis.sadd(ONLINE_USERS, user);
        }
    }
    
    public void userOffline(String user) {
        try(Jedis jedis = redisManager.getResource()){
            jedis.srem(ONLINE_USERS, user);
        }
    }
    
    public void syncOfflineMessages(String user) {
        try(Jedis jedis = redisManager.getResource()){
            String offlineKey = "Offline_Messages:"+user;
            List<String> messages = jedis.lrange(offlineKey, 0, -1);
            
            if(!messages.isEmpty()){
                System.out.println("【Mensajes no leídos】 Total " + messages.size()+" mensajes");
                
                List<String> sorted = redisManager.getAllMessagesSorted(CHANNEL);
                
                
                    sorted.forEach(msgId -> {
                        Map<String, String> messageData = jedis.hgetAll(msgId);
                        
                        String formattedMessage = messageData.entrySet().stream()
                                .filter(entry -> !"timestamp".equals(entry.getKey()))
                                .map(entry -> {
                                    String key = entry.getKey();
                                    return key + ": " + entry.getValue();
                                })
                                .collect(Collectors.joining(", "));
                        
                        System.out.println("【Mensaje Detalle】" + formattedMessage);
                        
                        String sortedKey = "All_Messages:"+CHANNEL+":sorted";
                        jedis.zrem(sortedKey,msgId);
                        jedis.del(msgId);
                        
                    });
                
                jedis.del(offlineKey);
                
            }
        }
    }
    
    private void mostrarHistorial() {
        List<String> history = redisManager.obtenerHistorial(CHANNEL);
        if (history == null || history.isEmpty()) {
            return;
        }
        System.out.println("--- HISTORIAL ---");
        history.forEach(msg -> {
            String formattedMsg = formatHistoricalMessage(msg);
            System.out.println(formattedMsg);
        });
        System.out.println("----------------");
    }
    
    private String formatHistoricalMessage(String rawMessage) {
        String[] parts = rawMessage.replaceAll("^\\[|]", "").split(" ", 2);
        if (parts.length < 2) {
            return rawMessage;
        }
        
        String user = parts[0];
        String message = parts[1];
        
        return String.format("%s: %s",
                user,
                message
        );
    }
    
    public void startChat(){
            mostrarHistorial();
            
            userOnline(CurrentUser);
            
            syncOfflineMessages(CurrentUser);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                userOffline(CurrentUser);
                System.out.print("\n\033[31m[Sistema] Usuario eliminado de la lista online.\033[0m");
            }));
            MessageSubscriber subscriber = new MessageSubscriber(CurrentUser,redisManager);
            new Thread(subscriber).start();
            handleUserInput();
    }
    
    private void handleUserInput(){
        try(Scanner scanner = new Scanner(System.in); Jedis jedis = redisManager.getResource()){
            System.out.println("\033[H\033[2J");
            System.out.flush();
            while(!Thread.currentThread().isInterrupted()){
                System.out.print( CurrentUser + ": ");
                String message = scanner.nextLine().trim();
                
                if ("exit".equalsIgnoreCase(message)) {
                    System.exit(0);
                }
                
                if ("users".equals(message)) {
                    Set<String> users = jedis.smembers(ONLINE_USERS);
                    System.out.println("Usuarios en línea: " +
                            users.stream()
                                    .sorted()
                                    .collect(Collectors.joining(", ")));
                    continue;
                }

                if(message.startsWith("/comment")){
                    String[] param = message.split(" ",3);
                    if(param.length > 2){
                        String receiver = param[1];
                        String message_pri = param[2];
                        String formattedMessage = String.format("[%s] %s: %s", new Date(), CurrentUser, message_pri );
                        redisManager.storeOfflineMessage(CurrentUser, receiver,CHANNEL,formattedMessage);
                        continue;
                    }
                }

                publishMessage(String.format("%s: %s", CurrentUser,message));
            }
        }catch (Exception e) {
            System.err.println("Entrada/salida error: " + e.getMessage());
        }
    }
    
    private void publishMessage(String message) {
        try(Jedis jedis = redisManager.getResource()) {
            jedis.publish(CHANNEL, message);
        } catch (Exception e) {
            System.err.println("Publicación fallida: " + e.getMessage());
        }
    }
}
