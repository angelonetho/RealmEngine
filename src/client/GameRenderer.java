package client;

import entities.ChatMessage;
import entities.Player;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

public class GameRenderer {

    private final HashMap<UUID, Player> playersMap;
    private final HashMap<UUID, ChatMessage> chatMap;
    private final Player player;

    public GameRenderer(HashMap<UUID, Player> playersMap, HashMap<UUID, ChatMessage> chatMap, Player player) {
        this.playersMap = playersMap;
        this.chatMap = chatMap;
        this.player = player;
    }

    public void drawGame(Graphics g) {

        if (player.getName() == null) {
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth("Type a nickname in chat to connect.");

            g.setColor(Color.BLACK);
            g.drawString("Type a nickname in chat to connect.", (Client.WIDTH / 2) - textWidth / 2, (Client.HEIGHT / 2) - 25);
        }

        for (Player player : playersMap.values()) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillOval((int) player.getX() - 24, (int) player.getY() + 12, 48, 24);
        }

        for (Player player : playersMap.values()) {


            g.setColor(Color.BLUE);
            g.fillOval((int) player.getX() - 24, (int) player.getY() - 24, 48, 48);

            if (player.getUuid().equals(this.player.getUuid())) {

                g.setColor(Color.blue);
                g.drawOval((int) player.getX() - 25, (int) player.getY() + 12, 50, 24);
            }

            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(player.getName());

            g.setColor(Color.BLACK);
            g.drawString(player.getName(), (int) player.getX() - textWidth / 2, (int) player.getY() + 50);


            // Chat
            if (chatMap.containsKey(player.getUuid())) {
                ChatMessage chatMessage = chatMap.get(player.getUuid());

                if (chatMessage.isExpired()) {
                    chatMap.remove(player.getUuid());
                    continue;
                }

                String content = chatMessage.getContent();

                g.setColor(Color.RED);
                g.drawString(content, (int) player.getX() - textWidth / 2, (int) player.getY() - 48);
            }
        }

    }
}
