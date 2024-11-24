package client;

import entities.Player;

import java.util.HashMap;
import java.util.UUID;

public class PacketProcessor {

    private final HashMap<UUID, Player> playersMap;
    private final Player player;

    public PacketProcessor(Player player, HashMap<UUID, Player> playersMap) {
        this.player = player;
        this.playersMap = playersMap;
    }

    public void processMessage(String message) {
        String[] rawData = message.split(" ");

        String packetType = rawData[0];

        switch (packetType.toUpperCase()) {

            case "PLAYER_DATA" -> {
                Player player = initializePlayer(rawData);

                playersMap.putIfAbsent(player.getUuid(), player);
            }

            case "ACCOUNT_DATA" -> {
                Player newPlayer = initializePlayer(rawData);
                this.player.CloneFrom(newPlayer);
            }

            case "PLAYER_MOVE" -> {
                UUID uuid = UUID.fromString(rawData[1]);
                float x = Float.parseFloat(rawData[2]);
                float y = Float.parseFloat(rawData[3]);

                playersMap.get(uuid).setDestination(x, y);
            }

            case "PLAYER_DISCONNECT" -> {
                UUID uuid = UUID.fromString(rawData[1]);
                playersMap.remove(uuid);
            }
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


}
