package xyz.voroniyx.deathtotem.paper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.util.List;

public final class TiabCommand {

    private TiabCommand() {
    }

    public static LiteralCommandNode<CommandSourceStack> build(DeathTotemPlugin plugin) {
        return Commands.literal("tiab")
                .requires(source -> source.getSender().hasPermission(DeathTotemPlugin.COMMAND_PERMISSION))
                .then(Commands.literal("config")
                        .then(Commands.literal("EnableTotemConsume")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setEnableTotemConsume(plugin, ctx))
                                )
                        )
                        .then(Commands.literal("TotemConsumeOnlyWhenLastTotemUsed")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setConsumeOnlyWhenLast(plugin, ctx))
                                )
                        )
                        .then(Commands.literal("Get")
                                .executes(ctx -> getConfig(plugin, ctx))
                        )
                )
                .then(Commands.literal("override")
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .then(Commands.argument("option", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("EnableTotemConsume");
                                            builder.suggest("TotemConsumeOnlyWhenLastTotemUsed");
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> setPlayerOverride(plugin, ctx))
                                        )
                                )
                        )
                )
                .build();
    }

    private static int setEnableTotemConsume(DeathTotemPlugin plugin, CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        boolean newValue = BoolArgumentType.getBool(ctx, "value");

        ModConfig config = plugin.getConfigManager().getData();
        config.EnableTotemConsume = newValue;

        if (!plugin.getConfigManager().saveSave()) {
            sender.sendMessage(Component.text("Could not persist new value. After next restart, it might be gone :(", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Component.text("Successfully set \"EnableTotemConsume\" to " + newValue, NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int setConsumeOnlyWhenLast(DeathTotemPlugin plugin, CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        boolean newValue = BoolArgumentType.getBool(ctx, "value");

        ModConfig config = plugin.getConfigManager().getData();
        config.TotemConsumeOnlyWhenLastTotemUsed = newValue;

        if (!plugin.getConfigManager().saveSave()) {
            sender.sendMessage(Component.text("Could not persist new value. After next restart, it might be gone :(", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Component.text("Successfully set \"TotemConsumeOnlyWhenLastTotemUsed\" to " + newValue, NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int getConfig(DeathTotemPlugin plugin, CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        ModConfig config = plugin.getConfigManager().getData();

        sender.sendMessage(Component.empty()
                .append(Component.text("Totem in a Barrel Config", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.newline())
                .append(Component.text("EnableTotemConsume: ", NamedTextColor.WHITE))
                .append(Component.text(String.valueOf(config.EnableTotemConsume), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("TotemConsumeOnlyWhenLastTotemUsed: ", NamedTextColor.WHITE))
                .append(Component.text(String.valueOf(config.TotemConsumeOnlyWhenLastTotemUsed), NamedTextColor.GRAY))
        );

        for (ModConfig.PlayerOverrides override : config.PlayerOverrides) {
            if (override.TotemConsumeOnlyWhenLastTotemUsed == null && override.EnableTotemConsume == null) {
                continue;
            }

            Player online = plugin.getServer().getPlayer(override.PlayerUUID);
            String displayName = (online != null) ? online.getName() : override.PlayerUUID.toString();

            Component component = Component.text(displayName + " override's:", NamedTextColor.AQUA);

            if (override.EnableTotemConsume != null) {
                component = component.append(Component.newline())
                        .append(Component.text("EnableTotemConsume: " + override.EnableTotemConsume, NamedTextColor.GRAY));
            }

            if (override.TotemConsumeOnlyWhenLastTotemUsed != null) {
                component = component.append(Component.newline())
                        .append(Component.text("TotemConsumeOnlyWhenLastTotemUsed: " + override.TotemConsumeOnlyWhenLastTotemUsed, NamedTextColor.GRAY));
            }

            sender.sendMessage(component);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setPlayerOverride(DeathTotemPlugin plugin, CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
        List<Player> resolved;
        try {
            resolved = resolver.resolve(ctx.getSource());
        } catch (Exception ex) {
            sender.sendMessage(Component.text("Could not resolve target player.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (resolved.isEmpty()) {
            sender.sendMessage(Component.text("No matching player found.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Player player = resolved.get(0);
        String option = StringArgumentType.getString(ctx, "option");
        boolean newValue = BoolArgumentType.getBool(ctx, "value");

        ModConfig config = plugin.getConfigManager().getData();

        if (!config.AddOrUpdatePlayerOverride(player.getUniqueId(), option, newValue)) {
            sender.sendMessage(Component.text("Could not add player override. Please try again.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (!plugin.getConfigManager().saveSave()) {
            sender.sendMessage(Component.text("Could not persist player override. After next restart, it might be gone :(", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Component.text("Successfully set \"" + option + "\" to " + newValue + " for player: " + player.getName(), NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }
}
