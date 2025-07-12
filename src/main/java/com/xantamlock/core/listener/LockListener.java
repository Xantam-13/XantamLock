package com.xantamlock.core.listener;

import com.xantamlock.core.lock.Lock;
import com.xantamlock.core.lock.LockFocusTracker;
import com.xantamlock.core.lock.LockManager;
import com.xantamlock.core.tool.LockToolHandler;
import com.xantamlock.core.tool.VisualiserHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class LockListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() == Material.AIR) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String location = serialize(block.getLocation());

        // Handle punch mode
        if (LockToolHandler.hasPunch(uuid)) {
            LockToolHandler.consumePunch(uuid);
            handleAddOrRemove(player, location, block.getType().name(), block.getLocation());
            event.setCancelled(true);
            return;
        }

        // Handle tool mode
        if (LockToolHandler.isToolEnabled(uuid) && isLockingTool(player)) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                handleAdd(player, location);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                handleRemove(player, location);
            }
            event.setCancelled(true);
            return;
        }

        // Lock access logic (always runs)
        Lock lock = LockManager.getLockByPart(location);
        if (lock != null) {
            boolean canAccess = LockManager.canAccess(uuid, lock);

            if (!canAccess && lock.getMode() == com.xantamlock.core.lock.LockMode.PRIVATE) {
                player.sendMessage(color("&6[XantamLock]&c This is locked: '&f" + lock.getName() + "&c'. You cannot open it."));
                event.setCancelled(true);
                return;
            }

            player.sendMessage(color("&6[XantamLock]&b You opened lock: '&f" + lock.getName() + "&b'."));

            // Offer [Use] if not focused
            String focused = LockFocusTracker.getFocusedLockId(uuid);
            if (focused == null || !focused.equals(lock.getId())) {
                TextComponent useMsg = Component.text(ChatColor.translateAlternateColorCodes('&', "&6[XantamLock] "))
                        .append(Component.text("You have opened lock: ", NamedTextColor.GRAY))
                        .append(Component.text(lock.getName(), NamedTextColor.AQUA))
                        .append(Component.text(" — "))
                        .append(Component.text("[Use]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.runCommand("/lock use " + lock.getName()))
                                .hoverEvent(HoverEvent.showText(Component.text("Click to use this lock"))));

                player.sendMessage(useMsg);
            }
        }
    }

    private void handleAddOrRemove(Player player, String location, String type, Location loc) {
        Lock lock = LockManager.getFocusedLock(player.getUniqueId());
        if (lock == null) {
            player.sendMessage(color("&6[XantamLock]&c No active lock selected."));
            return;
        }

        if (lock.getParts().contains(location)) {
            lock.removePart(location);
            player.sendMessage(color("&6[XantamLock]&a Removed " + type + " from lock '&f" + lock.getName() + "&a'."));
        } else {
            lock.addPart(location);
            player.sendMessage(color("&6[XantamLock]&a Added " + type + " to lock '&f" + lock.getName() + "&a'."));
        }
    }

    private void handleAdd(Player player, String location) {
        Lock lock = LockManager.getFocusedLock(player.getUniqueId());
        if (lock == null) return;

        if (!lock.getParts().contains(location)) {
            lock.addPart(location);
            player.sendMessage(color("&6[XantamLock]&a Block added to lock '&f" + lock.getName() + "&a'."));
        } else {
            player.sendMessage(color("&6[XantamLock]&b That block is already locked."));
        }
    }

    private void handleRemove(Player player, String location) {
        Lock lock = LockManager.getFocusedLock(player.getUniqueId());
        if (lock == null) return;

        if (lock.getParts().contains(location)) {
            lock.removePart(location);
            player.sendMessage(color("&6[XantamLock]&a Block removed from lock '&f" + lock.getName() + "&a'."));
        } else {
            player.sendMessage(color("&6[XantamLock]&b That block is not part of your lock."));
        }
    }

    private boolean isLockingTool(Player player) {
        if (player.getInventory().getItemInMainHand() == null) return false;
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Locking Tool");
    }

    private String serialize(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
