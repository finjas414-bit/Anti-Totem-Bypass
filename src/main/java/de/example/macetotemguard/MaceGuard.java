package de.example.maceguard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

public final class MaceGuard extends JavaPlugin implements Listener, CommandExecutor {

    /*
     * true  = Anti-Totem-Bypass is enabled
     * false = Totem bypass is allowed
     *
     * Always starts enabled after a server restart.
     */
    private volatile boolean enabled = true;

    /*
     * Players who already popped a Totem during the current server tick.
     */
    private final Set<UUID> poppedThisTick = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("totembypass") != null) {
            getCommand("totembypass").setExecutor(this);
        }

        if (getCommand("antitotembypass") != null) {
            getCommand("antitotembypass").setExecutor(this);
        }

        getLogger().info("Anti-Totem-Bypass enabled.");
        getLogger().info("Protection: ON");
    }

    /**
     * Handles Totem activation.
     *
     * When Anti-Totem-Bypass is enabled:
     * - Only one Totem may activate per player per server tick.
     * - Additional Totem activations in the same tick are cancelled.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTotemPop(EntityResurrectEvent event) {

        if (!enabled) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        // Player already activated a Totem during this tick.
        if (poppedThisTick.contains(uuid)) {
            event.setCancelled(true);
            return;
        }

        // First Totem activation this tick.
        poppedThisTick.add(uuid);

        /*
         * Remove the player from the set on the next server tick.
         * This means the protection lasts exactly for the current tick.
         */
        getServer().getScheduler().runTask(this, () -> {
            poppedThisTick.remove(uuid);
        });
    }

    /**
     * Prevents additional damage after a successful Totem activation
     * during the same server tick.
     *
     * This is important for mechanics that try to trigger several
     * Totem pops/damage events inside one tick.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFurtherDamageAfterPop(EntityDamageEvent event) {

        if (!enabled) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (poppedThisTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    /**
     * /totembypass
     *
     * Disables Anti-Totem-Bypass and allows the bypass mechanic.
     *
     * /antitotembypass
     *
     * Enables Anti-Totem-Bypass and blocks the bypass mechanic.
     */
    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission("antitotembypass.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("totembypass")) {

            enabled = false;
            poppedThisTick.clear();

            sender.sendMessage("§cAnti-Totem-Bypass is now §lOFF§c.");
            sender.sendMessage("§7Totem bypass is allowed.");

            getLogger().info(
                    sender.getName() + " disabled Anti-Totem-Bypass."
            );

            return true;
        }

        if (command.getName().equalsIgnoreCase("antitotembypass")) {

            enabled = true;
            poppedThisTick.clear();

            sender.sendMessage("§aAnti-Totem-Bypass is now §lON§a.");
            sender.sendMessage("§7Totem bypass is blocked.");

            getLogger().info(
                    sender.getName() + " enabled Anti-Totem-Bypass."
            );

            return true;
        }

        return true;
    }
}
