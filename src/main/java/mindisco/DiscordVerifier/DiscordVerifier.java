package mindisco.DiscordVerifier;

import mindisco.Constants;
import mindisco.DiscordVerifier.Ban.BanData;
import mindisco.DiscordVerifier.Ban.BanList;
import mindisco.TimedValue;

import java.util.HashMap;

public class DiscordVerifier {
    private HashMap<String, TimedValue<Long>> verificationUUIDs;
    private HashMap<String, Long> discordAccounts;
    private BanList banlist;
    public DiscordVerifier(){
        verificationUUIDs = new HashMap<>();
        discordAccounts = new HashMap<>();
        banlist = Constants.jsonInstance.fromJson(BanList.class, Constants.mindiscoBanFile.readString());
    }
    public Long getDiscord(String ip) {
        return discordAccounts.get(ip);
    }
    public VerifyState tryVerifyIP(String ip, String uuid) {
        TimedValue<Long> value = verificationUUIDs.get(uuid);
        if (value == null) {
            return VerifyState.INVALID;
        }
        verificationUUIDs.remove(uuid);
        if (value.expired()) {
            return VerifyState.EXPIRED;
        }
        discordAccounts.put(ip, value.data);
        return VerifyState.SUCCESS;
    }
    public String createVerifyToken(Long account, int timeout){
        String uuid = java.util.UUID.randomUUID().toString();
        verificationUUIDs.put(uuid, new TimedValue<>(account, timeout));
        return uuid;
    }
    public void forceVerifyIP(String ip) {
        discordAccounts.put(ip, Long.MIN_VALUE);
    }
    public boolean isBanned(long discordID){
        return banlist.isBanned(discordID);
    }
    public void ban(BanData banData){
        banlist.ban(banData);
        updateBanFile();
    }
    private void updateBanFile(){
        Constants.mindiscoBanFile.writeString(Constants.jsonInstance.toJson(banlist, BanList.class), false);
    }
}

