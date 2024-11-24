package entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessage {

    private final UUID playerUUID;
    private final String content;
    private final LocalDateTime expire_at;

    public ChatMessage(UUID playerUUID, String content) {
        this.playerUUID = playerUUID;
        this.content = content;
        this.expire_at = LocalDateTime.now().plusSeconds(5);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getContent() {
        return content;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expire_at);
    }


}
