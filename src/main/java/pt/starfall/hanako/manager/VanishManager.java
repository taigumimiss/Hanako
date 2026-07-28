package pt.starfall.hanako.manager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public class VanishManager {
    private static VanishManager instance;
    private final JavaPlugin plugin;

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<UUID, BossBar> BossBars = new HashMap<>();

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
            throw new IllegalStateException("VanishManager isnt initialized");
        }

        return instance;
    }

    public void enableVanish(Player target) {
        hiddenPlayers.add(target.getUniqueId());
        hidePlayer(target);
        showBossBar(target);
    }

    public void disableVanish(Player target) {
        hiddenPlayers.remove(target.getUniqueId());
        hideBossBar(target);
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

    public void showBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(
                "Estás em vanish",
                BarColor.GREEN,
                BarStyle.SOLID
        );

        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);

        BossBars.put(player.getUniqueId(), bossBar);
    }

    public void hideBossBar(Player player) {
        BossBar bossBar = BossBars.remove(player.getUniqueId());

        if (bossBar != null) {
            bossBar.removeAll();
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

    public boolean isVanished(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public int getOnlineVanishedCount() {
        return hiddenPlayers.size();
    }
}
