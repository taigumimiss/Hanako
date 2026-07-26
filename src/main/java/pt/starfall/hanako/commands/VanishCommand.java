package pt.starfall.hanako.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import pt.starfall.hanako.manager.VanishManager;

public class VanishCommand {

    public static int execute(CommandSourceStack src, String targetName) {
        if (!(src.getSender() instanceof Player player)) {
            src.getSender().sendMessage("Hey! este commando so pode ser usado por players!");
            return 0;
        }
        VanishManager.getInstance().toggleVanish(player);
        return 1;
    }


    public static int executeDefault(CommandSourceStack src) {
        return execute(src, null);
    }
}
