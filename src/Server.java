import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(2201);
        System.out.println("Server running on port 2201");

        boolean stopSignal = false;

        while (!stopSignal) {
            try {
                Socket clientSocket = server.accept();
                System.out.println("Client connected");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;
                while((message = in.readLine()) != null) {
                    System.out.println("Client: " + message);

                    if(message.equalsIgnoreCase("/sair")) {
                        out.println("Disconnecting...");
                        System.out.println("Client disconnected.");
                        break;
                    }

                    if(message.equalsIgnoreCase("/stop")) {
                        out.println("Stopping server...");
                        System.out.println("Received stop signal");
                        stopSignal = true;
                        break;
                    }

                    out.println("Server received:" + message);
                }

                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error on client: " + e.getMessage());
            }
        }
    }
}
