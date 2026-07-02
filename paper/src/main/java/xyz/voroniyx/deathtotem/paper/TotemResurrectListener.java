package xyz.voroniyx.deathtotem.paper;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class TotemResurrectListener implements Listener {

    private final DeathTotemPlugin plugin;

    public TotemResurrectListener(DeathTotemPlugin plugin) {
        this.plugin = plugin;
    }

    // ignoreCancelled = true: the event is fired cancelled when the entity has no totem,
    // so we only react to actual, successful resurrections (mirrors the Fabric mixin, which
    // runs on a RETURN value of true from checkTotemDeathProtection).
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Run one tick later so the held totem has already been consumed, keeping the
        // "only consume when it was the last totem" check consistent with the Fabric build.
        plugin.getServer().getScheduler().runTask(plugin, () -> PaperTotemPop.handle(plugin, player));
    }
}
