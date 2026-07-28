package pt.starfall.hanako.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import pt.starfall.hanako.manager.VanishChestManager;

public class ChestPacketListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.BLOCK_ACTION) return;

        WrapperPlayServerBlockAction packet = new WrapperPlayServerBlockAction(event);

        if (packet.getActionId() != 1) return;

        Player recipient = event.getPlayer();
        if (recipient == null) return;

        World world = recipient.getWorld();
        Vector3i pos = packet.getBlockPosition();
        Location loc = new Location(world, pos.getX(), pos.getY(), pos.getZ());

        if (VanishChestManager.getInstance().shouldSuppressAnimation(loc)) {
            event.setCancelled(true);
        }
    }
}
