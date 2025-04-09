package cn.ckaiz.chat_real;

import java.util.Scanner;

/**
 * @author chong, Ibra
 */
public class Main {
    public static void main(String[] args) {
        
        try(RedisManager redisManager = new RedisManager("localhost",6379,null)){
            ChatService chat = new ChatService(redisManager);
            
            System.out.print("Escribe tu nombre de usuario: ");
            String user = new Scanner(System.in).nextLine();
            
            chat.startChat(user);
        }
    }
}
