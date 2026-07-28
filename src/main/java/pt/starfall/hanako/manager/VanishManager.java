package pt.starfall.hanako.manager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pt.starfall.hanako.data.RedisManager;

import java.util.*;


public class VanishManager {
    private static VanishManager instance;
    private final JavaPlugin plugin;

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

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
        setVanished(target.getUniqueId(), true);
    }

    public void disableVanish(Player target) {
        setVanished(target.getUniqueId(), false);
    }

    public void toggleVanish(Player target) {
        if (hiddenPlayers.contains(target.getUniqueId())) {
            disableVanish(target);
        } else {
            enableVanish(target);
        }
    }

    public void setVanished(UUID uuid, boolean vanished) {
        if (vanished) {
            if (!hiddenPlayers.add(uuid)) return;
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                hidePlayer(player);
                showBossBar(player);
            }
            RedisManager.getInstance().setVanished(uuid, true);
        } else {
            if (!hiddenPlayers.remove(uuid)) return;
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                hideBossBar(player);
                showPlayerToAll(player);
            }
            RedisManager.getInstance().setVanished(uuid, false);
        }
    }

    public void restoreVanished(Player player) {
        hiddenPlayers.add(player.getUniqueId());
        hidePlayer(player);
        showBossBar(player);
    }

    public void hidePlayer(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(target) && !viewer.hasPermission("hanako.see")) {
                viewer.hidePlayer(plugin, target);
            }
        }
    }

    private void showPlayerToAll(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.showPlayer(plugin, player);
        }
    }

    public void showBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar existing = bossBars.remove(uuid);
        if (existing != null) {
            existing.removeAll();
        }

        BossBar bossBar = Bukkit.createBossBar(
                "Estás em vanish",
                BarColor.GREEN,
                BarStyle.SOLID
        );

        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);

        bossBars.put(uuid, bossBar);
    }

    public void hideBossBar(Player player) {
        BossBar bossBar = bossBars.remove(player.getUniqueId());

        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void removeVanished(UUID uuid) {
        hiddenPlayers.remove(uuid);
        BossBar bossBar = bossBars.remove(uuid);
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
        return (int) hiddenPlayers.stream()
                .filter(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    return player != null && player.isOnline();
                })
                .count();
    }
}
