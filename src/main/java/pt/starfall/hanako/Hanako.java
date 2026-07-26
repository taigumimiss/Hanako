package pt.starfall.hanako;

import org.bukkit.plugin.java.JavaPlugin;
import pt.starfall.hanako.commands.VanishCommandManager;
import pt.starfall.hanako.config.ConfigManager;
import pt.starfall.hanako.listener.PlayerJoinListener;
import pt.starfall.hanako.manager.VanishManager;

public final class Hanako extends JavaPlugin {

    @Override
    public void onEnable() {
        VanishManager.initialize(this);

        //Events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

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
}
