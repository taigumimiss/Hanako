package pt.starfall.hanako.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pt.starfall.hanako.manager.VanishManager;

import java.util.UUID;

public class VanishCommand {

    public static int execute(CommandSourceStack src, String targetName) {
        if (!(src.getSender() instanceof Player player)) {
            src.getSender().sendMessage("Hey! este commando so pode ser usado por players!");
            return 0;
        }

        UUID targetUuid;

        if (targetName == null) {
            targetUuid = player.getUniqueId();
        } else {
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (target.getUniqueId() == null) {
                return 0;
            }
            targetUuid = target.getUniqueId();
        }

        VanishManager.getInstance().toggleVanish(Bukkit.getPlayer(targetUuid));
        return 1;
    }




    public static int executeDefault(CommandSourceStack src) {
        return execute(src, null);
    }
}
