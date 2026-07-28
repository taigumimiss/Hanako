package pt.starfall.hanako.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import pt.starfall.hanako.util.SchedulerUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishChestManager {

    //TODO: Suppress chest sounds when opened by vanished

    private static VanishChestManager instance;

    private final Map<BlockPos, ChestState> chests = new ConcurrentHashMap<>();

    private VanishChestManager() {
    }

    public static synchronized void initialize() {
        if (instance != null) {
            throw new IllegalStateException("VanishChestManager already is initialized");
        }

        instance = new VanishChestManager();
    }

    public static VanishChestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("VanishChestManager isnt initialized");
        }

        return instance;
    }

    public void onChestOpen(Location loc, UUID playerUUID) {
        VanishManager vanish = VanishManager.getInstance();
        BlockPos key = key(loc);
        ChestState state = chests.computeIfAbsent(key, k -> new ChestState());
        state.positions.add(key);
        boolean wasVanishOnly = state.isVanishOnly();

        if (vanish.isVanished(playerUUID)) {
            state.vanished++;
        } else {
            state.normal++;
            if (wasVanishOnly) {
                for (BlockPos pos : state.positions) {
                    sendBlockAction(pos, 1);
                }
            }
        }

        if (state.positions.size() < 2) {
            registerOtherHalf(loc, state);
        }
    }

    public void onChestClose(Location loc, UUID playerUUID) {
        VanishManager vanish = VanishManager.getInstance();
        BlockPos key = key(loc);
        ChestState state = chests.get(key);
        if (state == null) return;

        boolean wasVanishOnly = state.isVanishOnly();

        if (vanish.isVanished(playerUUID)) {
            state.vanished = Math.max(0, state.vanished - 1);
        } else {
            if (state.normal == 1 && state.vanished > 0) {
                for (BlockPos pos : state.positions) {
                    sendBlockAction(pos, 0);
                }
            }
            state.normal = Math.max(0, state.normal - 1);
        }

        if (state.isEmpty()) {
            scheduleCleanup(state);
        }
    }

    public boolean shouldSuppressAnimation(String world, int x, int y, int z) {
        ChestState state = chests.get(new BlockPos(world, x, y, z));
        return state != null && state.isVanishOnly();
    }

    private void scheduleCleanup(ChestState state) {
        Set<BlockPos> positions = new HashSet<>(state.positions);
        SchedulerUtil.runSync(() -> {
            for (BlockPos pos : positions) {
                if (chests.get(pos) == state) {
                    chests.remove(pos);
                }
            }
        });
    }

    private void registerOtherHalf(Location loc, ChestState state) {
        BlockPos otherKey = findOtherHalf(loc);
        if (otherKey == null) return;

        ChestState existing = chests.putIfAbsent(otherKey, state);
        if (existing == null || existing == state) {
            state.positions.add(otherKey);
        }
    }

    private BlockPos findOtherHalf(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        InventoryHolder holder = loc.getBlock().getState() instanceof Chest chest
                ? chest.getInventory().getHolder() : null;
        if (!(holder instanceof DoubleChest dc)) return null;

        BlockPos thisKey = key(loc);
        if (dc.getLeftSide() instanceof BlockState leftBs) {
            BlockPos leftKey = key(leftBs.getLocation());
            if (!thisKey.equals(leftKey)) return leftKey;
        }
        if (dc.getRightSide() instanceof BlockState rightBs) {
            BlockPos rightKey = key(rightBs.getLocation());
            if (!thisKey.equals(rightKey)) return rightKey;
        }
        return null;
    }

    private void sendBlockAction(BlockPos pos, int actionData) {
        World world = Bukkit.getWorld(pos.world());
        if (world == null) return;

        int blockTypeId = SpigotConversionUtil
                .fromBukkitBlockData(world.getBlockAt(pos.x(), pos.y(), pos.z()).getBlockData())
                .getGlobalId();

        Vector3i vector = new Vector3i(pos.x(), pos.y(), pos.z());
        WrapperPlayServerBlockAction packet =
                new WrapperPlayServerBlockAction(vector, 1, actionData, blockTypeId);

        VanishManager vanish = VanishManager.getInstance();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (vanish.isVanished(player.getUniqueId())) continue;
            if (!player.getWorld().getName().equals(pos.world())) continue;

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }

    private static BlockPos key(Location loc) {
        return new BlockPos(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private static final class ChestState {
        int vanished = 0;
        int normal = 0;
        final Set<BlockPos> positions = ConcurrentHashMap.newKeySet();

        boolean isVanishOnly() {
            return vanished > 0 && normal == 0;
        }

        boolean isEmpty() {
            return vanished <= 0 && normal <= 0;
        }
    }

    private record BlockPos(String world, int x, int y, int z) {
    }
}
