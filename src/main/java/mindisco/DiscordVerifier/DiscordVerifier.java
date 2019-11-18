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
        if(Constants.mindiscoBanFile.exists()){
            banlist = Constants.jsonInstance.fromJson(BanList.class, Constants.mindiscoBanFile.readString());
        } else {
            banlist = new BanList();
            updateBanFile();
        }
    }
    public Long getDiscord(String uuidUUSID) {
        return discordAccounts.get(uuidUUSID);
    }
    public VerifyState tryVerifyUUIDUUSID(String uuidUUSID, String uuid) {
        TimedValue<Long> value = verificationUUIDs.get(uuid);
        if (value == null) {
            return VerifyState.INVALID;
        }
        verificationUUIDs.remove(uuid);
        if (value.expired()) {
            return VerifyState.EXPIRED;
        }
        discordAccounts.put(uuidUUSID, value.data);
        return VerifyState.SUCCESS;
    }
    public String createVerifyToken(Long account, int timeout){
        String uuid = java.util.UUID.randomUUID().toString();
        verificationUUIDs.put(uuid, new TimedValue<>(account, timeout));
        return uuid;
    }
    public void forceVerifyUUIDUUSID(String uuidUUSID) {
        discordAccounts.put(uuidUUSID, Long.MIN_VALUE);
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

