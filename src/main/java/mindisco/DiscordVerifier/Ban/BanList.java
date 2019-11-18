package mindisco.DiscordVerifier.Ban;

import java.util.ArrayList;

public class BanList {
    public ArrayList<BanData> bans;
    public BanList(){
        bans = new ArrayList<>();
    }
    public boolean isBanned(long discordID){
        for(BanData ban: bans){
            if(ban.discordID == discordID){
                return true;
            }
        }
        return false;
    }

    public void ban(BanData banData) {
        bans.add(banData);
    }
}
