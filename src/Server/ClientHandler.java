package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import static Server.Server.clients;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private String playerNickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("NEW PLAYER IS TRYING TO CONNECT");
            out.println("Enter your nickname: ");
            playerNickname = in.readLine();
            clients.put(playerNickname, socket);
            broadcast("Player " + playerNickname + " has connected to the server.");

            String message;
            while ((message = in.readLine()) != null) {

                broadcast(playerNickname + ": " + message);

            }
        } catch (IOException e) {

            if (playerNickname != null) {
                System.out.println("Connection error from " + playerNickname + ": " + e.getMessage());
            }

        } finally {

            if (playerNickname != null) {
                clients.remove(playerNickname);
                broadcast(playerNickname + " has disconnected from the server.");
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void broadcast(String message) {
        System.out.println("[" + LocalDateTime.now() + "] " + message);

        for (Socket clientSocket : clients.values()) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
