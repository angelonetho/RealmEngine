package server;

import entities.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;

import static server.Server.clients;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private Player player;

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
            String playerNickname = in.readLine();

            player = new Player(playerNickname, 0, 0);

            clients.put(player, socket);
            sendAccountData();
            sendRoomData();
            broadcast("Player " + player.getName() + " has connected to the server.");

            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("player_move")) {
                    String[] rawData = message.split(" ");

                    UUID uuid = UUID.fromString(rawData[1]);
                    float x = Float.parseFloat(rawData[2]);
                    float y = Float.parseFloat(rawData[3]);

                    for (Player player : clients.keySet()) {
                        if (player.getUuid().equals(uuid)) {
                            player.setDestination(x, y);
                        }
                    }

                    broadcast(message);

                } else if (message.startsWith("player_pos")) {
                    String[] rawData = message.split(" ");

                    UUID uuid = UUID.fromString(rawData[1]);
                    float x = Float.parseFloat(rawData[2]);
                    float y = Float.parseFloat(rawData[3]);

                    for (Player player : clients.keySet()) {
                        if (player.getUuid().equals(uuid)) {
                            player.setPosition(x, y);
                        }
                    }

                    broadcast(message);

                } else {
                    broadcast(player.getName() + ": " + message);
                }


            }
        } catch (IOException e) {

            if (player != null && !socket.isClosed()) {
                System.out.println("Connection error from " + player.getName() + ": " + e.getMessage());
            }

        } finally {

            if (player != null) {
                clients.remove(player);
                broadcast(player.getName() + " has disconnected from the server.");
                broadcast("player_disconnect " + player.getUuid());
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

    private void sendAccountData() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("account_data " + player);
            System.out.println("account_data " + player);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendRoomData() {
        broadcast("player_data " + player);
        for (Player player : clients.keySet()) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("player_data " + player);
                System.out.println("player_data " + player);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
