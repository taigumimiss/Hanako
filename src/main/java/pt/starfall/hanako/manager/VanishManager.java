package pt.starfall.hanako.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class VanishManager {
    private static VanishManager instance;
    private final JavaPlugin plugin;

    private final Set<UUID> hiddenPlayers = new HashSet<>();

    private VanishManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static synchronized void initialize(JavaPlugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("Hey! VanishManager already is initialized");
        }

        instance = new VanishManager(plugin);
    }

    public static VanishManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("VanishManager isnt initialized rip");
        }

        return instance;
    }

    public void enableVanish(Player target) {
        hiddenPlayers.add(target.getUniqueId());
        hidePlayer(target);
    }

    public void disableVanish(Player target) {
        hiddenPlayers.remove(target.getUniqueId());
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.showPlayer(plugin, target);
        }
    }

    public void toggleVanish(Player target) {
        if (hiddenPlayers.contains(target.getUniqueId())) {
            disableVanish(target);
        } else {
            enableVanish(target);
        }
    }

    public void hidePlayer(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(target) && !viewer.hasPermission("hanako.see")) {
                viewer.hidePlayer(plugin, target);
            }
        }
    }

    public void hideVanishedFrom(Player viewer) {
        if (hiddenPlayers.isEmpty() || viewer.hasPermission("hanako.see")) {
            return;
        }

        for (UUID vanishedId : hiddenPlayers) {
            Player vanished = Bukkit.getPlayer(vanishedId);

            if (vanished == null || !vanished.isOnline() || vanished.equals(viewer)) {
                continue;
            }

            viewer.hidePlayer(plugin, vanished);
        }
    }
}
