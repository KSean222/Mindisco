package mindisco;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.DiscordEvent;
import io.anuke.arc.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.game.EventType.*;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.net.Administration;
import io.anuke.mindustry.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class MindiscoPlugin extends Plugin{

    private HashMap<String, TimedValue<Long>> verificationUUIDs;
    private HashMap<String, Long> discordAccounts;


    public MindiscoPlugin(){
        final String token = Core.settings.getDataDirectory().child("mods/mindisco.token").readString();

        verificationUUIDs = new HashMap<>();
        discordAccounts = new HashMap<>();
        Events.on(PlayerConnect.class, event -> {
            Long discordAccountID = discordAccounts.get(event.player.getInfo().id);
            if (discordAccountID != null) {

                return;
            }
            TimedValue timedValue = verificationUUIDs.get(event.player.name);
            if (timedValue == null) {
                Log.info(event.player.getInfo().id + " doesn't have a valid verification code.");
                Call.onKick(event.player.con, "Please get a verification code first!");
                return;
            }
            verificationUUIDs.remove(event.player.name);
            if (timedValue.expired()) {
                Log.info(event.player.getInfo().id + " has an expired verification code.");
                Call.onKick(event.player.con, "This code has expired!");
                return;
            }
            discordAccounts.put(event.player.getInfo().id, (Long)timedValue.data);
            Log.info("Verified " + event.player.getInfo().id + " as " + timedValue.data + ".");
            Call.onKick(event.player.con, "Account verified! You can now join this server.");
        });
        Catnip.catnipAsync(token).thenAccept(catnip -> {
            catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
                if(msg.author().bot()) return;
                if(msg.content().equals("+md verify")) {
                    msg.author().createDM().thenAcceptAsync(dm -> {
                        String uuid = java.util.UUID.randomUUID().toString();
                        verificationUUIDs.put(uuid, new TimedValue<>(msg.author().idAsLong(), 1000 * 60 * 5));
                        dm.sendMessage("Code: `" + uuid + "`\nUse this as your username when joining the server.\nHurry, it expires in a minute!");
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
            discordAccounts.put(args[0], Long.MIN_VALUE);
            Log.info("Force verified " + args[0] + ".");
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
    }
}
