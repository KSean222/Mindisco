package mindisco.DiscordVerifier.Ban;

public class BanData {
    public long discordID;
    public String reason;
    public String bannedBy;
    public BanData(long discordID, String reason, String bannedBy){
        this.discordID = discordID;
        this.reason = reason;
        this.bannedBy = bannedBy;
    }
}
