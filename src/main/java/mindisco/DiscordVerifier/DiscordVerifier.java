package mindisco.DiscordVerifier;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.ImageOptions;
import mindisco.Constants;
import mindisco.DiscordVerifier.Ban.BanData;
import mindisco.DiscordVerifier.Ban.BanList;
import mindisco.TimedValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class DiscordVerifier {
    private HashMap<String, TimedValue<User>> verificationUUIDs;
    private HashMap<String, User> discordAccounts;
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
    public User getDiscord(String uuidUUSID) {
        return discordAccounts.get(uuidUUSID);
    }
    public VerifyState tryVerifyUUIDUUSID(String uuidUUSID, String uuid) {
        TimedValue<User> value = verificationUUIDs.get(uuid);
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
    public String createVerifyToken(User account, int timeout){
        String uuid = java.util.UUID.randomUUID().toString();
        verificationUUIDs.put(uuid, new TimedValue<>(account, timeout));
        return uuid;
    }
    public void forceVerifyUUIDUUSID(String uuidUUSID) {
        discordAccounts.put(uuidUUSID, new User() {
            @Override
            public boolean animatedAvatar() {
                return false;
            }

            @Nonnull
            @Override
            public String defaultAvatarUrl() {
                return null;
            }

            @Nullable
            @Override
            public String avatarUrl(@Nonnull ImageOptions options) {
                return null;
            }

            @Nullable
            @Override
            public String avatarUrl() {
                return null;
            }

            @Nonnull
            @Override
            public String effectiveAvatarUrl(@Nonnull ImageOptions options) {
                return null;
            }

            @Nonnull
            @Override
            public String effectiveAvatarUrl() {
                return null;
            }

            @Nonnull
            @Override
            public String username() {
                return null;
            }

            @Nonnull
            @Override
            public String discriminator() {
                return null;
            }

            @Nullable
            @Override
            public String avatar() {
                return null;
            }

            @Override
            public boolean bot() {
                return false;
            }

            @Override
            public long idAsLong() {
                return 0;
            }

            @Override
            public Catnip catnip() {
                return null;
            }
        });
    }
    public void unverifyUUIDUUSID(String uuidUUSID) {
        discordAccounts.remove(uuidUUSID);
    }

    public boolean isBanned(User user){
        return banlist.isBanned(user.idAsLong());
    }
    public void ban(BanData banData){
        banlist.ban(banData);
        updateBanFile();
    }
    private void updateBanFile(){
        Constants.mindiscoBanFile.writeString(Constants.jsonInstance.toJson(banlist, BanList.class), false);
    }
}

