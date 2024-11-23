package Client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class ClientUI {
    private static final int SERVER_PORT = 22201;
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

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

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);

            }
        };

        gamePanel.setBackground(Color.GREEN);
        frame.add(gamePanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ClientUI clientUI = new ClientUI();
        clientUI.startClient();
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval(100, 100, 50, 50);
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

                        String finalServerMessage = serverMessage;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalServerMessage + "\n"));
                        System.out.println("[" + LocalDateTime.now() + "] " + finalServerMessage);

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
