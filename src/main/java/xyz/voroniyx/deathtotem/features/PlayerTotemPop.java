package xyz.voroniyx.deathtotem.features;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import xyz.voroniyx.deathtotem.DeathTotemMod;
import xyz.voroniyx.deathtotem.mixin_interfaces.IChunkMapMixin;

public class PlayerTotemPop {
    public static void handle(ServerPlayer player) {
        if(!DeathTotemMod.CONFIG.getData().EnableTotemConsume) {
            return;
        }

        ServerLevel level = player.level();
        String playerName = player.getName().getString();

        var hasTotemInInv = player.getInventory().contains(x -> x.is(Items.TOTEM_OF_UNDYING));
        if(hasTotemInInv && DeathTotemMod.CONFIG.getData().TotemConsumeOnlyWhenLastTotemUsed) {
            return;
        }

        var chunkMap = ((IChunkMapMixin) level.getChunkSource().chunkMap).deathtotemmod$GetVisibleChunkMap();

        for (var chunkHolder : chunkMap.values()) {
            LevelChunk chunk = chunkHolder.getTickingChunk();
            if (chunk == null) continue;

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof BarrelBlockEntity barrel) {

                    int totemIndex = hasOnlySingleTotem(barrel);
                    if (totemIndex == -1) continue;

                    ItemStack totemStack = barrel.getItem(totemIndex);

                    if (totemStack.has(DataComponents.CUSTOM_NAME)) {
                        String totemName = totemStack.getHoverName().getString();

                        if (totemName.equals(playerName)) {
                            totemStack.consume(1, player);
                            barrel.setChanged();
                            player.level().updateNeighborsAt(barrel.getBlockPos(), barrel.getBlockState().getBlock());
                            return;
                        }
                    }
                }
            }
        }
    }

    private static int hasOnlySingleTotem(BarrelBlockEntity barrel) {
        int totemIndex = -1;
        boolean foundSomethingElse = false;

        for (int i = 0; i < barrel.getContainerSize(); i++) {
            ItemStack stack = barrel.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (totemIndex != -1 || !stack.is(Items.TOTEM_OF_UNDYING) || stack.getCount() != 1) {
                foundSomethingElse = true;
                break;
            }

            totemIndex = i;
        }

        return (!foundSomethingElse && totemIndex != -1) ? totemIndex : -1;
    }
}

