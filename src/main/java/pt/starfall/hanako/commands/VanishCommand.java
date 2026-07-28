package pt.starfall.hanako.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pt.starfall.hanako.manager.VanishManager;

import java.util.UUID;

public class VanishCommand {

    public static int execute(CommandSourceStack src, String targetName, boolean silent) {
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

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            src.getSender().sendMessage("Hey! esse jogador não está online!");
            return 0;
        }

        boolean wasVanished = VanishManager.getInstance().isVanished(targetUuid);
        VanishManager.getInstance().toggleVanish(target);

        if (!silent) {
            if (wasVanished) {
                Bukkit.broadcast(Component.text("§e" + target.getName() + " joined the game"));
            } else {
                Bukkit.broadcast(Component.text("§e" + target.getName() + " left the game"));
            }
        }

        return 1;
    }
}
