package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static Server.Server.clients;

public class ServerHandler implements Runnable {

    ServerSocket serverSocket;

    public ServerHandler(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            String serverMessage;
            while (!serverSocket.isClosed() && (serverMessage = console.readLine()) != null) {

                if (serverMessage.equals(".bye")) {
                    disconnectAllPlayers();
                    serverSocket.close();
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnectAllPlayers() {
        for (Socket clientSocket : clients.values()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
