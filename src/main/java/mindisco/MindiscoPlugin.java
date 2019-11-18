package mindisco;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.DiscordEvent;
import io.anuke.arc.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.core.NetServer;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.game.EventType;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.net.Administration;
import io.anuke.mindustry.net.Packets;
import io.anuke.mindustry.plugin.Plugin;
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
        Events.on(PlayerLeave.class, event -> {
            for(Administration.PlayerInfo playerInfo: Constants.administration.getBanned()){

            }
        });
        Events.on(WorldLoadEvent.class, event -> {
            for (Player player: Vars.playerGroup.all()) {
                handlePlayerJoin(player);
            }
        });
        Catnip.catnipAsync(Constants.mindiscoBotToken).thenAccept(catnip -> {
            catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
                if(msg.author().bot()) return;
                if(msg.content().equals("+md verify")) {
                    msg.author().createDM().thenAcceptAsync(dm -> {
                        long authorID = msg.author().idAsLong();
                        if(verifier.isBanned(authorID)) {
                            dm.sendMessage(Constants.discordBanMessage);
                        } else {
                            String uuid = verifier.createVerifyToken(authorID, 1000 * 60 * 5);
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
        handler.<Player>register("get-discord", "<player>", "Whisper text to another player.", (args, player) -> {
            Player other = Vars.playerGroup.all().find(p -> p.name.equals(args[0]));
            if(other == null){
                player.sendMessage(String.format(Constants.playerNotFound, args[0]));
            } else {
                player.sendMessage(other.name + "'s discord ID: " + verifier.getDiscord(getUUIDUSID(other)));
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

        handler.<Player>register("ban", "<code>", "Command to verify yourself.", (args, player) -> {
        });
    }
    private String getUUIDUSID(Player player){
        return player.uuid + " " + player.usid;
    }
    private void handlePlayerJoin(Player player){
        Long discordAccountID = verifier.getDiscord(getUUIDUSID(player));
        if (discordAccountID == null) {
            player.setTeam(Team.derelict);
            player.kill();
            player.sendMessage("Looks like you're not verified. Get a verify code and verify yourself using /verify to continue!");
        }
    }
}
