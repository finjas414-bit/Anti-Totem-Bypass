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

    private final Set<UUID> poppedThisTick = ConcurrentHashMap.newKeySet();
    private volatile boolean enabled = true;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("maceguard") != null) {
            getCommand("maceguard").setExecutor(this);
        }

        getLogger().info("MaceGuard enabled. Anti-Totem-Bypass: ON");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTotemPop(EntityResurrectEvent event) {
        if (!enabled || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (poppedThisTick.contains(uuid)) {
            event.setCancelled(true);
            return;
        }

        poppedThisTick.add(uuid);

        getServer().getScheduler().runTask(this, () ->
                poppedThisTick.remove(uuid)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFurtherDamageAfterPop(EntityDamageEvent event) {
        if (!enabled || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (poppedThisTick.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("maceguard.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendStatus(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> {
                enabled = !enabled;
                sender.sendMessage(enabled
                        ? "§aMaceGuard Anti-Totem-Bypass is now §lON§a."
                        : "§cMaceGuard Anti-Totem-Bypass is now §lOFF§c.");
            }
            case "on", "enable" -> {
                enabled = true;
                sender.sendMessage("§aMaceGuard Anti-Totem-Bypass is now §lON§a.");
            }
            case "off", "disable" -> {
                enabled = false;
                sender.sendMessage("§cMaceGuard Anti-Totem-Bypass is now §lOFF§c.");
            }
            case "status" -> sendStatus(sender);
            default -> sender.sendMessage(
                    "§eUsage: §f/maceguard <toggle|on|off|status>"
            );
        }

        return true;
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage(
                enabled
                        ? "§7MaceGuard Anti-Totem-Bypass: §a§lON"
                        : "§7MaceGuard Anti-Totem-Bypass: §c§lOFF"
        );
    }
}
