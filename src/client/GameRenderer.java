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

    public void draw(Graphics graphics) {
        if (player.getName() == null) {
            drawWelcomeScreen(graphics);
        }


        drawGame(graphics);
        drawChat(graphics);
    }

    public void drawWelcomeScreen(Graphics graphics) {

        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth("Type a nickname in chat to connect.");

        graphics.setColor(Color.BLACK);
        graphics.drawString("Type a nickname in chat to connect.", (Client.WIDTH / 2) - textWidth / 2, (Client.HEIGHT / 2) - 25);

    }

    public void drawChat(Graphics graphics) {
        for (Player player : playersMap.values()) {
            if (chatMap.containsKey(player.getUuid())) {
                ChatMessage chatMessage = chatMap.get(player.getUuid());

                if (chatMessage.isExpired()) {
                    chatMap.remove(player.getUuid());
                    continue;
                }

                String content = chatMessage.getContent();
                FontMetrics metrics = graphics.getFontMetrics();
                int textWidth = metrics.stringWidth(content);

                graphics.setColor(Color.RED);
                graphics.drawString(content, (int) player.getX() - textWidth / 2, (int) player.getY() - 48);
            }
        }
    }

    public void drawGame(Graphics graphics) {

        //Shadows render before the player
        for (Player player : playersMap.values()) {
            graphics.setColor(new Color(0, 0, 0, 100));
            graphics.fillOval((int) player.getX() - 24, (int) player.getY() + 12, 48, 24);
        }

        for (Player player : playersMap.values()) {


            if (player.getUuid().equals(this.player.getUuid())) {

                graphics.setColor(Color.BLUE);
                graphics.drawOval((int) player.getX() - 25, (int) player.getY() + 12, 50, 24);
            }

            graphics.setColor(Color.BLUE);
            graphics.fillOval((int) player.getX() - 24, (int) player.getY() - 24, 48, 48);


            FontMetrics metrics = graphics.getFontMetrics();
            int textWidth = metrics.stringWidth(player.getName());

            graphics.setColor(Color.BLACK);
            graphics.drawString(player.getName(), (int) player.getX() - textWidth / 2, (int) player.getY() + 50);


        }

    }
}
