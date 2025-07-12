package com.xantamlock.core.listener;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.lock.Lock;
import com.xantamlock.core.lock.LockManager;
import com.xantamlock.core.tool.LockToolHandler;
import net.md\_5.bungee.api.chat.ClickEvent;
import net.md\_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LockListener implements Listener {

```
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location loc = clicked.getLocation();
        String locString = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        // === LOCKED INTERACTION HANDLING ===
        Lock partLock = LockManager.getLockByPart(locString);
        if (partLock != null) {
            event.setCancelled(true); // Prevent default interaction

            player.sendMessage(ChatColor.YELLOW + "[XantamLock] This is part of lock: " + partLock.getName());

            if (LockManager.canAccess(player, partLock)) {
                player.sendMessage(ChatColor.GREEN + "[XantamLock] You have access to this lock.");
                player.spigot().sendMessage(new ComponentBuilder("[Use]")
                        .color(net.md_5.bungee.api.ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lock use " + partLock.getName()))
                        .create());
            } else {
                player.sendMessage(ChatColor.RED + "[XantamLock] You don’t have access to this lock.");
            }

            return;
        }

        // === TOOL & PUNCH MODE HANDLING ===
        boolean punchMode = LockToolHandler.hasPunch(player.getUniqueId());
        boolean toolMode = LockToolHandler.isToolEnabled(player.getUniqueId());

        if (!punchMode && !toolMode) return;

        if (toolMode) {
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand.getType() != Material.STICK) return;

            ItemMeta meta = inHand.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            boolean isLockTool = meta.getLore().stream().anyMatch(s -> s.toLowerCase().contains("lock entities"));
            if (!isLockTool) return;
        }

        Lock focused = LockManager.getFocusedLock(player.getUniqueId());
        if (focused == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[XantamLock]&b You must have a focused lock."));
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            focused.addPart(locString);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[XantamLock]&a Block added to lock '&f" + focused.getName() + "&a'."));
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            focused.removePart(locString);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[XantamLock]&c Block removed from lock '&f" + focused.getName() + "&c'."));
        }

        if (punchMode) {
            LockToolHandler.consumePunch(player.getUniqueId());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[XantamLock]&b Punch mode disabled."));
        }
    }
```

}
