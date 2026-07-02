package xyz.voroniyx.deathtotem.paper;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.util.UUID;

public final class PaperTotemPop {

    private PaperTotemPop() {
    }

    public static void handle(DeathTotemPlugin plugin, Player player) {
        ModConfig config = plugin.getConfigManager().getData();
        UUID playerUUID = player.getUniqueId();

        boolean hasEnableOverride = config.HasActiveEnableTotemConsumeOverwriteThatIsTrue(playerUUID);
        boolean globalEnable = config.EnableTotemConsume;

        if (!hasEnableOverride && !globalEnable) {
            return;
        }

        String playerName = player.getName();

        boolean hasTotemInInv = player.getInventory().contains(Material.TOTEM_OF_UNDYING);

        Boolean consumeWhenLastOverride = config.GetTotemConsumeOnlyWhenLastTotemUsedOverwrite(playerUUID);
        boolean finalConsumeWhenLastOnly = (consumeWhenLastOverride != null)
                ? consumeWhenLastOverride
                : config.TotemConsumeOnlyWhenLastTotemUsed;

        if (hasTotemInInv && finalConsumeWhenLastOnly) {
            return;
        }

        World level = player.getWorld();

        for (Chunk chunk : level.getLoadedChunks()) {
            // useSnapshot = false: work with live block states so inventory edits hit the world.
            for (BlockState blockState : chunk.getTileEntities(false)) {
                if (!(blockState instanceof Barrel barrel)) {
                    continue;
                }

                Inventory inventory = barrel.getInventory();

                int totemIndex = indexOfOnlySingleTotem(inventory);
                if (totemIndex == -1) {
                    continue;
                }

                ItemStack totemStack = inventory.getItem(totemIndex);
                if (totemStack == null) {
                    continue;
                }

                ItemMeta meta = totemStack.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) {
                    continue;
                }

                String totemName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
                if (totemName.equals(playerName)) {
                    // The barrel holds a single totem, so consuming one empties the slot.
                    inventory.setItem(totemIndex, null);
                    return;
                }
            }
        }
    }

    private static int indexOfOnlySingleTotem(Inventory inventory) {
        int totemIndex = -1;
        boolean foundSomethingElse = false;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack == null || stack.getType().isAir()) {
                continue;
            }

            if (totemIndex != -1 || stack.getType() != Material.TOTEM_OF_UNDYING || stack.getAmount() != 1) {
                foundSomethingElse = true;
                break;
            }

            totemIndex = i;
        }

        return (!foundSomethingElse && totemIndex != -1) ? totemIndex : -1;
    }
}
