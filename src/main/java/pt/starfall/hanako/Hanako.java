package pt.starfall.hanako;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import org.bukkit.plugin.java.JavaPlugin;
import pt.starfall.hanako.commands.VanishCommandManager;
import pt.starfall.hanako.config.ConfigManager;
import pt.starfall.hanako.listener.ChestPacketListener;
import pt.starfall.hanako.listener.PlayerInteractListener;
import pt.starfall.hanako.listener.PlayerJoinListener;
import pt.starfall.hanako.listener.PlayerQuitListener;
import pt.starfall.hanako.listener.ServerPingListener;
import pt.starfall.hanako.data.RedisManager;
import pt.starfall.hanako.manager.VanishChestManager;
import pt.starfall.hanako.manager.VanishManager;

public final class Hanako extends JavaPlugin {
    private static Hanako instance;


    @Override
    public void onEnable() {
        instance = this;

        //Managers
        ConfigManager.initialize();
        VanishManager.initialize(this);
        VanishChestManager.initialize();
        RedisManager.initialize();
        RedisManager.getInstance().connect();
        RedisManager.getInstance().addVanishListener(uuid -> VanishManager.getInstance().setVanished(uuid, true));
        RedisManager.getInstance().addUnvanishListener(uuid -> VanishManager.getInstance().setVanished(uuid, false));

        //Events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);

        //Commands
        VanishCommandManager.initialize();
        registerCommands();
    }

    @Override
    public void onLoad() {
        //PacketEvents
        PacketEvents.getAPI().getEventManager().registerListener(new ChestPacketListener(), PacketListenerPriority.NORMAL);
    }

    private void registerCommands() {
        var lifecycleManager = getLifecycleManager();
        lifecycleManager.registerEventHandler(
                io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS,
                event -> {
                    event.registrar().register(VanishCommandManager.getInstance().build());
                }
        );
    }

    @Override
    public void onDisable() {
        RedisManager.getInstance().disconnect();
    }

    public static Hanako getInstance() {
        return instance;
    }
}
