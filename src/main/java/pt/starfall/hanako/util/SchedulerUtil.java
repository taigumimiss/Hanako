package pt.starfall.hanako.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import pt.starfall.hanako.Hanako;

public class SchedulerUtil {
    public static void runAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(Hanako.getInstance(), ignored -> task.run());
    }

    public static void runSync(Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(Hanako.getInstance(), ignored -> task.run());
    }

    public static void scheduleLocation(Location location, Runnable task) {
        Hanako.getInstance().getServer().getRegionScheduler().execute(Hanako.getInstance(), location, task);
    }

    public static void scheduleEntity(Entity entity, Runnable task) {
        runSync(task);
    }
}
