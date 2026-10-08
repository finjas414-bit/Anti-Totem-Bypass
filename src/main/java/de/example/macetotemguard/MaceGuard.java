package de.example.maceguard;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MaceGuard extends JavaPlugin implements Listener, CommandExecutor {

    /*
     * true  = Anti-Totem-Bypass enabled
     * false = Totem bypass allowed
     */
    private volatile boolean enabled = true;

    /*
     * Players who have already popped a Totem during this server tick.
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
     * Allows only one Totem activation per player per server tick.
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

        /*
         * The player has already used a Totem during this tick.
         * Prevent another Totem from activating.
         */
        if (poppedThisTick.contains(uuid)) {
            event.setCancelled(true);
            return;
        }

        /*
         * First Totem pop this tick.
         */
        poppedThisTick.add(uuid);

        /*
         * Clear the protection on the next server tick.
         */
        getServer().getScheduler().runTask(this, () -> {
            poppedThisTick.remove(uuid);
        });
    }

    /**
     * Blocks only Mace damage after a Totem has already popped
     * during the same server tick.
     *
     * Other damage types, including projectile/railgun damage,
     * are NOT blocked.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockMaceDamageAfterTotem(EntityDamageByEntityEvent event) {

        if (!enabled) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        /*
         * Has this player already popped a Totem this tick?
         */
        if (!poppedThisTick.contains(victim.getUniqueId())) {
            return;
        }

        /*
         * Only block direct attacks from a player.
         */
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        /*
         * Only block the attack if the attacker is actually
         * holding a Mace.
         */
        if (attacker.getInventory().getItemInMainHand().getType() != Material.MACE) {
            return;
        }

        /*
         * This is a Mace attack against a player who already
         * popped a Totem during this tick.
         */
        event.setCancelled(true);
    }

    /**
     * /totembypass
     *
     * Disables Anti-Totem-Bypass.
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

        /**
         * /antitotembypass
         *
         * Enables Anti-Totem-Bypass.
         */
        if (command.getName().equalsIgnoreCase("antitotembypass")) {

            enabled = true;
            poppedThisTick.clear();

            sender.sendMessage("§aAnti-Totem-Bypass is now §lON§a.");
            sender.sendMessage("§7Mace Totem bypass is blocked.");

            getLogger().info(
                    sender.getName() + " enabled Anti-Totem-Bypass."
            );

            return true;
        }

        return true;
    }
}
