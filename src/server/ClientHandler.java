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
    private Player player = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("NEW PLAYER IS TRYING TO CONNECT");


            String message;
            while (player == null && (message = in.readLine()) != null) {
                if (message.startsWith("new_player")) {
                    String[] rawData = message.split(" ");

                    String playerNickname = rawData[1];

                    player = new Player(playerNickname, 0, 0);

                    clients.put(player, socket);
                    sendAccountData();
                    sendRoomData();
                    broadcast("Player " + player.getName() + " has connected to the server.");
                }
            }

            while ((message = in.readLine()) != null) {

                processMessage(message);


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

    private void processMessage(String message) {
        String[] rawData = message.split(" ");

        String packetType = rawData[0];

        switch (packetType.toUpperCase()) {

            case "PLAYER_POS" -> updatePlayerPosition(rawData);


            case "PLAYER_MOVE" -> updatePlayerDestination(rawData);


            default -> broadcast("player_message " + player.getUuid() + " " + message);


        }
    }

    private void updatePlayerPosition(String[] rawData) {

        UUID uuid = UUID.fromString(rawData[1]);
        float x = Float.parseFloat(rawData[2]);
        float y = Float.parseFloat(rawData[3]);

        for (Player player : clients.keySet()) {
            if (player.getUuid().equals(uuid)) {
                player.setPosition(x, y);
            }
        }

        broadcast("player_pos " + player.getUuid() + " " + x + " " + y);
    }

    private void updatePlayerDestination(String[] rawData) {

        float x = Float.parseFloat(rawData[2]);
        float y = Float.parseFloat(rawData[3]);

        for (Player player : clients.keySet()) {
            if (player.getUuid().equals(this.player.getUuid())) {
                player.setDestination(x, y);
            }
        }

        broadcast("player_move " + player.getUuid() + " " + x + " " + y);
    }

}
