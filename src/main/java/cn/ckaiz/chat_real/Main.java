package cn.ckaiz.chat_real;

import java.util.Scanner;

/**
 * @author chong, Ibrahim
 */
public class Main {
    public static void main(String[] args) {
        
        try(RedisManager redisManager = new RedisManager("localhost",6379,null)){
            System.out.print("Escribe tu nombre de usuario: ");
            String user = new Scanner(System.in).nextLine();
            
            ChatService chat = new ChatService(redisManager,user);
            
            chat.startChat();
        }
    }
}
