package pt.starfall.hanako.listener;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pt.starfall.hanako.manager.VanishChestManager;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Location loc = getChestLocation(event.getInventory());
        if (loc == null) return;

        VanishChestManager.getInstance().onChestOpen(loc, player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Location loc = getChestLocation(event.getInventory());
        if (loc == null) return;

        VanishChestManager.getInstance().onChestClose(loc, player.getUniqueId());
    }

    private Location getChestLocation(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof Chest chest) {
            return chest.getLocation();
        }
        if (holder instanceof DoubleChest) {
            return inventory.getLocation();
        }
        return null;
    }
}
