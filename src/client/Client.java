package client;

import entities.ChatMessage;
import entities.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class Client {
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
    private static final int SERVER_PORT = 22201;
    private final HashMap<UUID, Player> playersMap = new HashMap<>();
    private final HashMap<UUID, ChatMessage> chatMap = new HashMap<>();

    private final Player player = new Player(null, 0, 0);

    private JFrame frame;
    private JTextField chatField;
    private JPanel gamePanel;
    private GameRenderer renderer;
    private JLabel statusLabel;

    private NetworkManager networkManager;


    public Client() {
        frame = new JFrame("Realm Engine Prototype");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        statusLabel = new JLabel("Conectando ao servidor...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(statusLabel, BorderLayout.NORTH);

        chatField = new JTextField();
        chatField.addActionListener(e -> sendMessage());
        frame.add(chatField, BorderLayout.SOUTH);


        renderer = new GameRenderer(playersMap, chatMap, player);


        gamePanel = new JPanel() {


            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderer.draw(g);
            }
        };


        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (player.getName() != null && networkManager != null) {
                    networkManager.sendMessage("player_move " + player.getUuid() + " " + x + " " + y);
                }
            }
        });

        gamePanel.addMouseListener(new MouseAdapter() {
        });

        gamePanel.setBackground(Color.GREEN);
        frame.add(gamePanel, BorderLayout.CENTER);

        new javax.swing.Timer(20, e -> {
            moveToDestination();
            gamePanel.repaint();
        }).start();

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Client clientUI = new Client();
        clientUI.startClient();
    }

    private void moveToDestination() {
        float speed = 8;

        for (Player player : playersMap.values()) {

            float dx = player.getDestinationX() - player.getX();
            float dy = player.getDestinationY() - player.getY();

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance == 0) {
                continue;
            }

            if (distance <= speed) {
                player.setPosition(player.getDestinationX(), player.getDestinationY());

                if (player.getUuid().equals(this.player.getUuid())) {
                    networkManager.sendMessage("player_pos " + player.getUuid() + " " + player.getX() + " " + player.getY());
                }
                continue;
            }

            float moveX = speed * (dx / distance);
            float moveY = speed * (dy / distance);
            player.setPosition(player.getX() + moveX, player.getY() + moveY);

        }
    }

    private void sendMessage() {
        String messageText = chatField.getText();
        if (messageText.trim().isEmpty()) return;

        if (player.getName() == null) {
            networkManager.sendMessage("new_player " + messageText);
            chatField.setText("");
            return;
        }

        networkManager.sendMessage(messageText);
        chatField.setText("");
    }

    public void startClient() {
        PacketProcessor packetHandler = new PacketProcessor(player, playersMap, chatMap);

        try {
            Socket socket = new Socket("localhost", SERVER_PORT);
            System.out.println("✅ Connected to " + socket.getRemoteSocketAddress());
            statusLabel.setVisible(false);

            networkManager = new NetworkManager(socket);
            BufferedReader in = networkManager.getInputReader();

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        packetHandler.processMessage(serverMessage);
                        System.out.println("[" + LocalDateTime.now() + "] " + serverMessage);
                    }

                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());

                } finally {
                    System.out.println("❌ You have been disconnected by the server.");
                    statusLabel.setText("❌ You have been disconnected by the server.");
                    statusLabel.setVisible(true);

                }
            }).start();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            statusLabel.setText("❌ Falha ao conectar-se ao servidor: " + e.getMessage());
        }
    }


}
