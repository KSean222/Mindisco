package mindisco;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.DiscordEvent;
import io.anuke.arc.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.plugin.Plugin;
import mindisco.DiscordVerifier.DiscordVerifier;

public class MindiscoPlugin extends Plugin{

    private DiscordVerifier verifier;
    public MindiscoPlugin(){
        verifier = new DiscordVerifier();

        Events.on(PlayerConnect.class, event -> {
            Long discordAccountID = verifier.getDiscord(event.player.getInfo().lastIP);
            if (discordAccountID == null) {
                event.player.setTeam(Team.derelict);
                event.player.kill();
                event.player.sendMessage("Looks like you're not verified. Get a verify code and verify yourself using /verify to continue!");
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
        handler.register("forceverify", "<text...>", "Force verify a player ID", args -> {
            verifier.forceVerifyIP(args[0]);
            Log.info("Force verified ip " + args[0] + ".");
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){

        //register a whisper command which can be used to send other players messages
        handler.<Player>register("getdiscord", "<player>", "Whisper text to another player.", (args, player) -> {
            //find player by nam

            Player other = Vars.playerGroup.all().find(p -> p.name.equalsIgnoreCase(args[0]));

            //give error message with scarlet-colored text if player isn't found
            if(other == null){
                player.sendMessage("[scarlet]No player by that name found!");
                return;
            }

            //send the other player a message, using [lightgray] for gray text color and [] to reset color
            //player.sendMessage("[lightgray](whisper) " + player.name + ":[] " + args[1]);
        });

        handler.<Player>register(Constants.verificationCommandName, Constants.verificationCommandArgs, Constants.verificationCommandDescription, (args, player) -> {
            String ip = player.getInfo().lastIP;
            if (verifier.getDiscord(ip) != null) {
                player.sendMessage(Constants.verificationAlreadyVerified);
                return;
            }
            switch (verifier.tryVerifyIP(ip, args[0])) {
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
    }
}
