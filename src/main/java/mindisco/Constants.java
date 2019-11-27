package mindisco;

import io.anuke.arc.Core;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.serialization.Json;
import io.anuke.mindustry.net.Administration;

import java.io.File;

public class Constants {
    public static final FileHandle mindiscoRoot = Core.settings.getDataDirectory().child("mods/mindisco/");
    public static final String mindiscoBotToken = mindiscoRoot.child("token.txt").readString();
    public static final String defaultBannedByName = "anonymous";
    public static final String discordBanMessage = "Whoopsie! Looks like you've been banned. If you suspect this is a mistake, please contact the moderators.\nReason: %s";
    public static final String verificationAlreadyVerified = "This code is invalid!";
    public static final String verificationInvalidCode = "This code is invalid!";
    public static final String verificationExpiredCode = "This code has expired!";
    public static final String verificationSuccessMessage = "Successfully verified! Re-join to play.";
    public static final String playerNotFound = "[scarlet]Player %s was not found!";
    public static final String idNotFound = "[scarlet]ID %d was not found!";
    public static final String playerNoPermission = "[scarlet]You don't have the permission to use this command!";
    public static final Administration administration = new Administration();
    public static final String getDiscordInfoText = "%s:\nDiscord name: %s\nDiscord ID: %s";
    public static final String invalidIdArg = "[scarlet] ID %s is invalid!";

    public static final Json jsonInstance = new Json();
    public static final FileHandle mindiscoBanFile = mindiscoRoot.child("bans.json");
}
