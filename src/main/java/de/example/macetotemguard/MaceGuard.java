package de.example.maceguard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MaceGuard extends JavaPlugin implements Listener {

    private final Set<UUID> poppedThisTick = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MaceGuard enabled: max 1 Totem pop per player per tick.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTotemPop(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Do not interfere with a resurrection that another plugin has
        // already cancelled.
        if (event.isCancelled()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        // A second resurrection attempt during this exact server tick
        // is blocked. This is the important anti multi-pop protection.
        if (poppedThisTick.contains(uuid)) {
            event.setCancelled(true);
            return;
        }

        // First Totem pop this tick is allowed.
        poppedThisTick.add(uuid);

        // At the start of the next server tick the player can pop another
        // Totem normally if another lethal hit occurs.
        getServer().getScheduler().runTask(this, () -> poppedThisTick.remove(uuid));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFurtherDamageAfterPop(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Once the Totem has saved the player, prevent any additional damage
        // during the remainder of the same server tick. This makes a single
        // Totem sufficient to survive a same-tick burst of lethal damage.
        if (poppedThisTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
