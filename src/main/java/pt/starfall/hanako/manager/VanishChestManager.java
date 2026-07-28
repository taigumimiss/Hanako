package pt.starfall.hanako.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishChestManager {

    private static VanishChestManager instance;

    private final JavaPlugin plugin;
    private final Map<BlockPos, Set<UUID>> chestViewers = new ConcurrentHashMap<>();

    private VanishChestManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static synchronized void initialize(JavaPlugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("VanishChestManager already is initialized");
        }

        instance = new VanishChestManager(plugin);
    }

    public static VanishChestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("VanishChestManager isnt initialized");
        }

        return instance;
    }

    public void onChestOpen(Location loc, UUID playerUUID) {
        BlockPos key = fromLocation(loc);
        chestViewers.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(playerUUID);
    }

    public void onChestClose(Location loc, UUID playerUUID) {
        BlockPos key = fromLocation(loc);
        Set<UUID> viewers = chestViewers.get(key);
        if (viewers != null) {
            viewers.remove(playerUUID);
            if (viewers.isEmpty()) {
                chestViewers.remove(key);
            }
        }
    }

    public boolean shouldSuppressAnimation(Location loc) {
        BlockPos key = fromLocation(loc);
        Set<UUID> viewers = chestViewers.get(key);
        if (viewers == null || viewers.isEmpty()) {
            return false;
        }
        return viewers.stream().allMatch(VanishManager.getInstance()::isVanished);
    }

    private static BlockPos fromLocation(Location loc) {
        return new BlockPos(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private record BlockPos(String world, int x, int y, int z) {
    }
}
