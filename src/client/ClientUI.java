package client;

import entities.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class ClientUI {
    private static final int SERVER_PORT = 22201;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    HashMap<UUID, Player> playersMap = new HashMap<>();

    private Player player;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField chatField;
    private JPanel gamePanel;
    private PrintWriter out;

    public ClientUI() {
        frame = new JFrame("Realm Engine Prototype");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(300, HEIGHT));

        frame.add(scrollPane, BorderLayout.EAST);

        chatField = new JTextField();
        chatField.addActionListener(e -> sendMessage());
        frame.add(chatField, BorderLayout.SOUTH);

        new javax.swing.Timer(20, e -> {
            moveToDestination();
            gamePanel.repaint();
        }).start();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);

            }
        };

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (out != null) {
                    out.println("player_move " + player.getUuid() + " " + x + " " + y);
                    System.out.println("player_move " + player.getUuid() + " " + x + " " + y);
                }
            }
        });

        gamePanel.addMouseListener(new MouseAdapter() {
        });

        gamePanel.setBackground(Color.GREEN);
        frame.add(gamePanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ClientUI clientUI = new ClientUI();
        clientUI.startClient();
    }

    private void drawGame(Graphics g) {

        for (Player player : playersMap.values()) {

            g.setColor(Color.BLUE);
            g.fillOval((int) player.getX() - 25, (int) player.getY() - 25, 50, 50);

            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(player.getName());

            g.setColor(Color.BLACK);
            g.drawString(player.getName(), (int) player.getX() - textWidth / 2, (int) player.getY() + 50);
        }

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
                out.println("player_pos " + player.getUuid() + " " + player.getX() + " " + player.getY());
                continue;
            }

            float moveX = speed * (dx / distance);
            float moveY = speed * (dy / distance);
            player.setPosition(player.getX() + moveX, player.getY() + moveY);

        }
    }

    private Player initializePlayer(String[] rawData) {

        UUID uuid = UUID.fromString(rawData[1]);
        String name = rawData[2];
        float positionX = Float.parseFloat(rawData[3]);
        float positionY = Float.parseFloat(rawData[4]);
        float destinationX = Float.parseFloat(rawData[5]);
        float destinationY = Float.parseFloat(rawData[6]);

        return new Player(uuid, name, positionX, positionY, destinationX, destinationY);
    }

    private void sendMessage() {
        String messageText = chatField.getText();
        if (messageText.trim().isEmpty()) return;

        out.println(messageText);
        chatField.setText("");
    }

    public void startClient() {
        try {
            Socket socket = new Socket("localhost", SERVER_PORT);
            System.out.println("✅ Connected to " + socket.getRemoteSocketAddress());

            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String serverMessage;

                    while ((serverMessage = in.readLine()) != null) {

                        if (serverMessage.startsWith("player_data")) {

                            String[] rawData = serverMessage.split(" ");

                            Player player = initializePlayer(rawData);

                            playersMap.putIfAbsent(player.getUuid(), player);
                        }

                        if (serverMessage.startsWith("account_data")) {
                            String[] rawData = serverMessage.split(" ");

                            player = initializePlayer(rawData);

                        }

                        if (serverMessage.startsWith("player_move")) {
                            String[] rawData = serverMessage.split(" ");

                            UUID uuid = UUID.fromString(rawData[1]);
                            float x = Float.parseFloat(rawData[2]);
                            float y = Float.parseFloat(rawData[3]);

                            playersMap.get(uuid).setDestination(x, y);

                        }

                        if (serverMessage.startsWith("player_disconnect")) {
                            String[] rawData = serverMessage.split(" ");

                            UUID uuid = UUID.fromString(rawData[1]);

                            playersMap.remove(uuid);

                        }

                        String finalServerMessage = serverMessage;

                        System.out.println("[" + LocalDateTime.now() + "] " + finalServerMessage);

                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(finalServerMessage + "\n");

                        });


                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    System.exit(1);
                } finally {
                    System.out.println("❌ You have been disconnected by the server.");
                    System.exit(1);
                }
            }).start();


        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}
