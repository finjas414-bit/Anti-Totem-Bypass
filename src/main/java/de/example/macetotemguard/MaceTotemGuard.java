package de.example.macetotemguard;

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

public final class MaceTotemGuard extends JavaPlugin implements Listener {

    // Players who have already popped a Totem during the current server tick.
    private final Set<UUID> poppedThisTick = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MaceTotemGuard enabled - one Totem per player per tick, with same-tick post-pop protection.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTotemResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        // Only one Totem resurrection is allowed for this player during a tick.
        if (poppedThisTick.contains(uuid)) {
            event.setCancelled(true);
            return;
        }

        // Allow the first Totem pop.
        poppedThisTick.add(uuid);

        // Remove the guard at the start of the next server tick.
        getServer().getScheduler().runTask(this, () -> poppedThisTick.remove(uuid));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventDeathAfterTotemThisTick(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // After a Totem has popped, prevent all further damage during the
        // remainder of this server tick. This means a player with only one
        // Totem cannot pop it and then immediately die in the same tick.
        if (poppedThisTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
