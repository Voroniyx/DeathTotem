package xyz.voroniyx.deathtotem.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.apache.commons.lang3.NotImplementedException;
import xyz.voroniyx.deathtotem.DeathTotemMod;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.util.concurrent.CompletableFuture;


public class ConfigCommand extends BaseCommand {

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) {
        throw new NotImplementedException("Use one of the private execute* methods!");
    }

    private int executeUpdateTotemConsume(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            boolean newValue = BoolArgumentType.getBool(ctx, "value");

            var config = DeathTotemMod.CONFIG.getData();

            config.EnableTotemConsume = newValue;
            var saved = DeathTotemMod.CONFIG.saveSave();

            if (!saved) {
                source.sendSystemMessage(Component.literal("Could not persist new value. After next restart, it might be gone :(").withColor(TextColor.RED));
                return 1;
            }

            source.sendSystemMessage(Component.literal("Successfully set \"EnableTotemConsume\" to " + newValue).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    private int executeUpdateConsumeOnlyWhenLastTotemUsed(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            boolean newValue = BoolArgumentType.getBool(ctx, "value");

            var config = DeathTotemMod.CONFIG.getData();

            config.TotemConsumeOnlyWhenLastTotemUsed = newValue;
            var saved = DeathTotemMod.CONFIG.saveSave();

            if (!saved) {
                source.sendSystemMessage(Component.literal("Could not persist new value. After next restart, it might be gone :(").withColor(TextColor.RED));
                return 1;
            }

            source.sendSystemMessage(Component.literal("Successfully set \"TotemConsumeOnlyWhenLastTotemUsed\" to " + newValue).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    private int executeGetConfig(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();

            var config = DeathTotemMod.CONFIG.getData();

            source.sendSystemMessage(Component.empty()
                    .append(Component.literal("Totem in a Barrel Config").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                    .append(Component.literal("\n").withStyle(ChatFormatting.RESET))
                    .append(Component.literal("EnableTotemConsume: ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.valueOf(config.EnableTotemConsume)).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("\n").withStyle(ChatFormatting.RESET))
                    .append(Component.literal("TotemConsumeOnlyWhenLastTotemUsed: ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.valueOf(config.TotemConsumeOnlyWhenLastTotemUsed)).withStyle(ChatFormatting.GRAY))
            );

            var configs = config.PlayerOverrides;
            var playerList = ctx.getSource().getServer().getPlayerList();

            for (ModConfig.PlayerOverrides override : configs) {
                if (override.TotemConsumeOnlyWhenLastTotemUsed == null && override.EnableTotemConsume == null) {
                    continue;
                }

                var targetPlayer = playerList.getPlayer(override.PlayerUUID);

                String displayName = (targetPlayer != null)
                        ? targetPlayer.getGameProfile().name()
                        : override.PlayerUUID.toString();

                var component = Component.empty()
                        .append(Component.literal(displayName + " override's:").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("\n").withStyle(ChatFormatting.RESET));

                if (override.EnableTotemConsume != null) {
                    component.append(Component.literal("EnableTotemConsume: " + override.EnableTotemConsume).withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n").withStyle(ChatFormatting.RESET));
                }

                if (override.TotemConsumeOnlyWhenLastTotemUsed != null) {
                    component.append(Component.literal("TotemConsumeOnlyWhenLastTotemUsed: " + override.TotemConsumeOnlyWhenLastTotemUsed).withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n").withStyle(ChatFormatting.RESET));
                }

                source.sendSystemMessage(component);
            }

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error("Error executing get config command", ex);
            return 1;
        }
    }

    private int executePlayerOverride(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            String option = StringArgumentType.getString(ctx, "option");
            boolean newValue = BoolArgumentType.getBool(ctx, "value");


            var config = DeathTotemMod.CONFIG.getData();

            var success = config.AddOrUpdatePlayerOverride(player.getUUID(), option, newValue);

            if (!success) {
                source.sendSystemMessage(Component.literal("Could not add player override. Please try again.").withColor(TextColor.RED));
                return 1;
            }

            var saved = DeathTotemMod.CONFIG.saveSave();

            if (!saved) {
                source.sendSystemMessage(Component.literal("Could not persist player overwrite. After next restart, it might be gone :(").withColor(TextColor.RED));
                return 1;
            }

            source.sendSystemMessage(Component.literal("Successfully set \"" + option + "\" to " + newValue + " for player: " + player.getPlainTextName()).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tiab")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                        .then(
                                Commands.literal("config")
                                        .then(Commands.literal("EnableTotemConsume")
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(this::executeUpdateTotemConsume)
                                                )
                                        )
                                        .then(Commands.literal("TotemConsumeOnlyWhenLastTotemUsed")
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(this::executeUpdateConsumeOnlyWhenLastTotemUsed)
                                                )
                                        )
                                        .then(Commands.literal("Get")
                                                .executes(this::executeGetConfig)
                                        )
                        )
                        .then(
                                Commands.literal("override")
                                        .then(Commands
                                                .argument("player", EntityArgument.player())
                                                .then(Commands
                                                        .argument("option", StringArgumentType.string()).suggests(this::suggestOption)
                                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                                .executes(this::executePlayerOverride)
                                                        )
                                                )
                                        )
                        )
        );


    }

    private CompletableFuture<Suggestions> suggestOption(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        builder.suggest("EnableTotemConsume");
        builder.suggest("TotemConsumeOnlyWhenLastTotemUsed");
        return builder.buildFuture();
    }
}
