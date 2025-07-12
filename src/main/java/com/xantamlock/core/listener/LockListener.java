package com.xantamlock.core.listener;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.config.ConfigManager;
import com.xantamlock.core.integration.VaultIntegration;
import com.xantamlock.core.lock.*;
import com.xantamlock.core.tool.LockToolHandler;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class LockListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        String locString = clicked.getWorld().getName() + "," + clicked.getX() + "," + clicked.getY() + "," + clicked.getZ();
        UUID playerId = player.getUniqueId();

        // Check if block is part of any lock
        for (Lock lock : LockManager.getAllLocks()) {
            if (lock.getParts().contains(locString)) {
                boolean isOwner = lock.getOwner().equals(playerId);
                boolean isPublic = lock.getMode() == LockMode.PUBLIC;

                if (!isOwner && !isPublic) {
                    player.sendMessage(color("&6[XantamLock]&c This block is locked."));
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage(color("&6[XantamLock]&a You accessed lock: '&f" + lock.getName() + "&a'."));
                TextComponent suggestion = Component.text("[Use]", NamedTextColor.DARK_AQUA)
                        .clickEvent(ClickEvent.runCommand("/lock use " + lock.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to focus this lock")));
                player.sendMessage(Component.text(" ").append(suggestion));
                break;
            }
        }

        // Punch and tool logic
        boolean punchMode = LockToolHandler.hasPunch(playerId);
        boolean toolMode = LockToolHandler.isToolEnabled(playerId);

        if (!punchMode && !toolMode) return;

        if (toolMode) {
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand.getType() != Material.STICK) return;

            ItemMeta meta = inHand.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            boolean isLockTool = meta.getLore().stream().anyMatch(s -> s.toLowerCase().contains("lock entities"));
            if (!isLockTool) return;
        }

        Lock lock = LockManager.getFocusedLock(playerId);
        if (lock == null) {
            player.sendMessage(color("&6[XantamLock]&b You must have a focused lock."));
            return;
        }

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            double cost = ConfigManager.getAddEntityCost();
            VaultIntegration vault = XantamLock.getInstance().getVaultIntegration();
            if (cost > 0 && vault.isEnabled()) {
                if (!vault.hasEnough(player.getName(), cost)) {
                    player.sendMessage(color("&6[XantamLock]&c You can't afford to add this block. Cost: &f" + cost));
                    return;
                }
                if (!vault.withdraw(player.getName(), cost)) {
                    player.sendMessage(color("&6[XantamLock]&c Payment failed. Cannot add block."));
                    return;
                }
            }
            lock.addPart(locString);
            player.sendMessage(color("&6[XantamLock]&a Block added to lock '&f" + lock.getName() + "&a'."));
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            lock.removePart(locString);
            player.sendMessage(color("&6[XantamLock]&c Block removed from lock '&f" + lock.getName() + "&c'."));
        }

        if (punchMode) {
            LockToolHandler.consumePunch(playerId);
            player.sendMessage(color("&6[XantamLock]&b Punch mode disabled."));
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
