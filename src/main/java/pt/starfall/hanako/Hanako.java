package pt.starfall.hanako;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pt.starfall.hanako.commands.VanishCommandManager;
import pt.starfall.hanako.listener.PlayerJoinListener;
import pt.starfall.hanako.listener.ServerPingListener;
import pt.starfall.hanako.manager.VanishManager;

public final class Hanako extends JavaPlugin {
    private static Hanako instance;


    @Override
    public void onEnable() {
        instance = this;

        VanishManager.initialize(this);

        //Events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);

        //Commands
        VanishCommandManager.initialize();
        registerCommands();
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
    }

    public static Hanako getInstance() {
        return instance;
    }
}
