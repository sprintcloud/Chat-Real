package cn.ckaiz.chat_real;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try(RedisManager redisManager = new RedisManager("localhost",6379,null)){
            ChatService chatService = new ChatService();
            
            System.out.print("Escribe tu nombre de usuario: ");
            String user = new Scanner(System.in).nextLine();
            
            chatService.startChat(user,redisManager);
        }
    }
}
