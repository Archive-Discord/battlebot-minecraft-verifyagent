package kr.battlebot.battlebotverifyagent.model;

import java.util.UUID;

public class Verify {
    private UUID userId;
    private String address;
    private String discordId;
    private String timeStamp;

    @Override
    public String toString() {
        return "Verify (userId:" + userId + " discordId:" + discordId + " timeStamp:" + timeStamp + ")";
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public UUID getId() {
        return userId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getDiscordId() {
        return discordId;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
}
