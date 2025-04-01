package cn.ckaiz.chat_real;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.Scanner;

public class SimpleChatApp {
    private static final String CHANNEL = "chat_global"; // Canal común para todos
    private Jedis subJedis; // Conexión para suscribirse
    private Jedis pubJedis; // Conexión para publicar
    private String user;

    public SimpleChatApp(String user) {
        // Crear conexiones separadas para suscripción y publicación
        this.subJedis =  new Jedis("localhost", 6379);
        this.pubJedis = new Jedis("localhost", 6379);
        this.user = user;
    }

    public void startChat() {
        // Iniciar un hilo para suscribirse y leer mensajes
        new Thread(() -> {
            subJedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    String s = message.split(":")[0];
                    if(Objects.equals(user, s)){
                        return;
                    }

                    // Mostrar los mensajes recibidos
                    System.out.println("\n" + message) ;
                    System.out.println(user +":") ;

                }
            }, CHANNEL);
        }).start();

        // Bucle para permitir que el usuario escriba mensajes y enviarlos
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(user + ": ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Cerrando chat...");
                break;
            }

            // Publicar el mensaje usando la conexión separada para publicar
            pubJedis.publish(CHANNEL, user + ": " + message);
        }

        // Cerrar conexiones al salir
        subJedis.close();
        pubJedis.close();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce tu nombre de usuario:");
        String user = scanner.nextLine();

        SimpleChatApp chatApp = new SimpleChatApp(user);
        chatApp.startChat();
    }
}
