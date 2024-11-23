package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 22201;
    static ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("✅ Server is running...");

            while (true) {
                Socket clientSocket = server.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
