package pt.starfall.hanako.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pt.starfall.hanako.data.RedisManager;
import pt.starfall.hanako.manager.VanishManager;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();

        if (RedisManager.getInstance().isVanished(joining.getUniqueId())) {
            VanishManager.getInstance().restoreVanished(joining);
        }

        VanishManager.getInstance().hideVanishedFrom(joining);
    }
}
