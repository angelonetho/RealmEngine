package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int SERVER_PORT = 22201;

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", SERVER_PORT);
            System.out.println("✅ Connected to " + socket.getRemoteSocketAddress());

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(in.readLine());

            new Thread(() -> {
                try {
                    String serverMessage;

                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    System.exit(1);
                } finally {
                    System.out.println("❌ You have been disconnected by the server.");
                    System.exit(1);
                }
            }).start();


            String userMessage;
            while ((userMessage = console.readLine()) != null) {
                out.println(userMessage);
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());

        }
    }
}
