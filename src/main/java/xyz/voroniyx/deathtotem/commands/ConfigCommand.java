package xyz.voroniyx.deathtotem.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.apache.commons.lang3.NotImplementedException;
import xyz.voroniyx.deathtotem.DeathTotemMod;


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
        );
    }
}
