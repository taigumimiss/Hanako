package pt.starfall.hanako.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.bukkit.entity.Player;
import pt.starfall.hanako.manager.VanishChestManager;
import pt.starfall.hanako.manager.VanishManager;

public class ChestPacketListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.BLOCK_ACTION) {
            handleBlockAction(event);
        } else if (event.getPacketType() == PacketType.Play.Server.SOUND_EFFECT) {
            handleSoundEffect(event);
        }
    }

    private void handleBlockAction(PacketSendEvent event) {
        WrapperPlayServerBlockAction packet = new WrapperPlayServerBlockAction(event);

        if (packet.getActionId() != 1) return;

        Player recipient = event.getPlayer();
        if (recipient == null) return;

        if (VanishManager.getInstance().isVanished(recipient.getUniqueId())) return;

        Vector3i pos = packet.getBlockPosition();

        if (VanishChestManager.getInstance().shouldSuppressAnimation(
                recipient.getWorld().getName(), pos.getX(), pos.getY(), pos.getZ())) {
            event.setCancelled(true);
        }
    }

    private void handleSoundEffect(PacketSendEvent event) {
        Player recipient = event.getPlayer();
        if (recipient == null) return;

        if (VanishManager.getInstance().isVanished(recipient.getUniqueId())) return;

        WrapperPlayServerSoundEffect packet = new WrapperPlayServerSoundEffect(event);

        String soundKey = packet.getSound().getSoundId().toString();
        if (!soundKey.contains("chest")) return;

        var pos = packet.getPosition();
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        int z = (int) pos.getZ();

        if (VanishChestManager.getInstance().shouldSuppressSound(
                recipient.getWorld().getName(), x, y, z)) {
            event.setCancelled(true);
        }
    }
}
