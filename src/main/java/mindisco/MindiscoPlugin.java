package mindisco;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.shard.DiscordEvent;
import io.anuke.arc.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.net.Administration;
import io.anuke.mindustry.plugin.Plugin;
import mindisco.DiscordVerifier.Ban.BanData;
import mindisco.DiscordVerifier.DiscordVerifier;

public class MindiscoPlugin extends Plugin{
    private DiscordVerifier verifier;
    public MindiscoPlugin(){
        verifier = new DiscordVerifier();
        Events.on(PlayerConnect.class, event -> {
            //Log.info(event.player.getInfo().id);
            //Log.info(event.player.getInfo().adminUsid);
            //Log.info(getUUIDUSID(event.player));
            handlePlayerJoin(event.player);
        });
        Events.on(WorldLoadEvent.class, event -> {
            for (Player player: Vars.playerGroup.all()) {
                handlePlayerJoin(player);
            }
        });
        Catnip.catnipAsync(Constants.mindiscoBotToken).thenAccept(catnip -> {
            catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
                User author = msg.author();
                if(author.bot()) return;
                if(msg.content().equals("+md verify")) {
                    author.createDM().thenAcceptAsync(dm -> {
                        if(verifier.isBanned(author)) {
                            dm.sendMessage(Constants.discordBanMessage);
                        } else {
                            String uuid = verifier.createVerifyToken(author, 1000 * 60 * 5);
                            dm.sendMessage("Code: `/verify " + uuid + "`\nRun this command when joining the server.\nHurry, it expires in 5 minutes!");
                        }
                    });
                }
            });
            catnip.connect();
        });
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("forceverify", "<uuid-usid...>", "Force verify a player ID", args -> {
            verifier.forceVerifyUUIDUUSID(args[0]);
            Log.info("Force verified " + args[0] + ".");
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){

        //register a whisper command which can be used to send other players messages
        handler.<Player>register("get-discord", "<player...>", "Get info about this player's discord account.", (args, player) -> {
            Player other = Vars.playerGroup.all().find(p -> p.name.equals(args[0]));
            if(other == null){
                player.sendMessage(String.format(Constants.playerNotFound, args[0]));
            } else {
                User user = verifier.getDiscord(getUUIDUSID(other));
                player.sendMessage(String.format(Constants.getDiscordInfoText, other.name, user.discordTag(), user.id()));
            }
        });

        handler.<Player>register("verify", "<code>", "Command to verify yourself.", (args, player) -> {
            String uuidUSID = getUUIDUSID(player);
            if (verifier.getDiscord(uuidUSID) != null) {
                player.sendMessage(Constants.verificationAlreadyVerified);
                return;
            }
            switch (verifier.tryVerifyUUIDUUSID(uuidUSID, args[0])) {
                case INVALID:
                    player.sendMessage(Constants.verificationInvalidCode);
                    break;
                case EXPIRED:
                    player.sendMessage(Constants.verificationExpiredCode);
                    break;
                case SUCCESS:
                    Call.onKick(player.con, Constants.verificationSuccessMessage);
                    break;
            }
        });

        handler.<Player>register("ban-discord-id", "<discord-id> [reason...]", "Ban using a discord ID, preventing it from being used for future verifications.", (args, player) -> {
            if(!player.isAdmin){
                player.sendMessage(Constants.playerNoPermission);
                return;
            }
            String banReason = args.length > 1 ? args[1] : "(No reason provided)";
            try{
                long id = Long.parseLong(args[0]);
                User banner = verifier.getDiscord(getUUIDUSID(player));
                verifier.ban(new BanData(id, banReason, banner.discordTag() + " (" + banner.id() + ")"));
                for(Player p: Vars.playerGroup.all()) {
                    String uuidUSID = getUUIDUSID(p);
                    if(verifier.isBanned(verifier.getDiscord(uuidUSID))){
                        verifier.unverifyUUIDUUSID(uuidUSID);
                        Call.onKick(p.con, String.format(Constants.discordBanMessage, banReason));
                    }
                }
            } catch (NumberFormatException e) {
                player.sendMessage(String.format(Constants.invalidIdArg, args[0]));
            }
        });
    }
    private String getUUIDUSID(Player player){
        return getUUIDUSID(player.getInfo());
    }
    private String getUUIDUSID(Administration.PlayerInfo playerInfo){
        return playerInfo.id + " " + playerInfo.adminUsid;
    }
    private void handlePlayerJoin(Player player){
        User user = verifier.getDiscord(getUUIDUSID(player));
        if (user == null) {
            player.setTeam(Team.derelict);
            player.kill();
            player.sendMessage("Looks like you're not verified. Get a verify code and verify yourself using /verify to continue!");
        }
    }
}
