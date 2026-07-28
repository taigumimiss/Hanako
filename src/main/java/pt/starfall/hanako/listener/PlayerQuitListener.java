package pt.starfall.hanako.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.starfall.hanako.manager.VanishManager;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        VanishManager.getInstance().removeVanished(event.getPlayer().getUniqueId());
    }
}
