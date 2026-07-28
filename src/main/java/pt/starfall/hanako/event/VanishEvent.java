package pt.starfall.hanako.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VanishEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID id;

    public VanishEvent(UUID id) {
        this.id = id;
    }

    public UUID getVanishedID() {
        return id;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
