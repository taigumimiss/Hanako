package pt.starfall.hanako.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;


public class VanishCommandManager {
    private static VanishCommandManager instance;

    private VanishCommandManager() {
    }

    public static synchronized void initialize() {
        if (instance != null) {
            throw new IllegalStateException("VanishCommandManager already initialized");
        }
        instance = new VanishCommandManager();
    }

    public static VanishCommandManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("VanishCommandManager not initialized");
        }
        return instance;
    }

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            builder.suggest(player.getName());
        }
        return builder.buildFuture();
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("vanish")
                .requires(src -> src.getSender().hasPermission("hanako.use"))
                .executes(ctx -> VanishCommand.executeDefault(ctx.getSource()))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(VanishCommandManager::suggestOnlinePlayers)
                        .executes(ctx -> VanishCommand.execute(ctx.getSource(), ctx.getArgument("player", String.class)))
                )
                .build();
    }


}
