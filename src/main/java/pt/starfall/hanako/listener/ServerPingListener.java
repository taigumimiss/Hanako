package pt.starfall.hanako.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pt.starfall.hanako.manager.VanishManager;

public class ServerPingListener implements Listener {

    @EventHandler
    public void onServerPing(PaperServerListPingEvent event) {
        VanishManager vanishManager = VanishManager.getInstance();

        event.getListedPlayers().removeIf(
                listedPlayerInfo -> VanishManager.getInstance().isVanished(listedPlayerInfo.id())
        );

        int visiblePlayers = event.getNumPlayers() - vanishManager.getOnlineVanishedCount();

        event.setNumPlayers(Math.max(0, visiblePlayers));
    }
}
