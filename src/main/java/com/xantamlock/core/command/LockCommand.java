package com.xantamlock.core.command;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.config.ConfigManager;
import com.xantamlock.core.database.LockStorage;
import com.xantamlock.core.integration.VaultIntegration;
import com.xantamlock.core.lock.*;
import com.xantamlock.core.tool.LockToolHandler;
import com.xantamlock.core.tool.VisualiserHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LockCommand implements CommandExecutor, TabCompleter {

    private static final List<String> MAIN_SUBCOMMANDS = Arrays.asList("create", "use", "list", "show", "edit", "punch", "tool", "visualise");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(color("&6[XantamLock]&b Use /lock create <name> to create a new lock."));
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "punch" -> handlePunch(player);
            case "tool" -> handleTool(player);
            case "create" -> handleCreate(player, args);
            case "list" -> handleList(player);
            case "use" -> handleUse(player, args);
            case "show" -> handleShow(player, args);
            case "edit" -> handleEdit(player, args);
            case "visualise" -> handleVisualise(player, args);
            default -> {
                player.sendMessage(color("&6[XantamLock]&b Unknown subcommand."));
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) return partialMatch(args[0], MAIN_SUBCOMMANDS);

        switch (args[0].toLowerCase()) {
            case "use", "show" -> {
                if (args.length == 2) return partialMatch(args[1], getLockNames(player));
            }
            case "edit" -> {
                if (args.length == 2) return partialMatch(args[1], Collections.singletonList("name"));
                if (args.length == 3) return partialMatch(args[2], getLockNames(player));
            }
            case "visualise" -> {
                if (args.length == 2) return partialMatch(args[1], Arrays.asList("on", "off"));
            }
        }
        return Collections.emptyList();
    }

    private List<String> getLockNames(Player player) {
        List<String> names = new ArrayList<>();
        for (Lock lock : LockManager.getLocks(player.getUniqueId())) names.add(lock.getName());
        return names;
    }

    private List<String> partialMatch(String arg, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) if (option.toLowerCase().startsWith(arg.toLowerCase())) matches.add(option);
        return matches;
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(color("&6[XantamLock]&b Usage: /lock create <name>"));
            return true;
        }

        String name = args[1];
        UUID uuid = player.getUniqueId();

        if (LockManager.hasLockWithName(uuid, name)) {
            player.sendMessage(color("&6[XantamLock]&c You already have a lock named '&f" + name + "&c'."));
            return true;
        }

        double cost = ConfigManager.getLockCreateCost();
        VaultIntegration vault = XantamLock.getInstance().getVaultIntegration();
        if (cost > 0 && vault.isEnabled()) {
            if (!vault.hasEnough(player.getName(), cost)) {
                player.sendMessage(color("&6[XantamLock]&c You can't afford to create a lock. Cost: &f" + cost));
                return true;
            }
            if (!vault.withdraw(player.getName(), cost)) {
                player.sendMessage(color("&6[XantamLock]&c Payment failed. Lock not created."));
                return true;
            }
        }

        Lock lock = LockManager.createLock(uuid, name);
        LockFocusTracker.setFocusedLock(uuid, lock.getId());
        player.sendMessage(color("&6[XantamLock]&a Lock '&f" + name + "&a' created and set as active."));
        return true;
    }

    private boolean handleList(Player player) {
        List<Lock> locks = LockManager.getLocks(player.getUniqueId());
        if (locks.isEmpty()) {
            player.sendMessage(color("&6[XantamLock]&b You have no locks."));
            return true;
        }
        player.sendMessage(color("&6[XantamLock]&b Your Locks:"));
        for (Lock lock : locks) {
            TextComponent line = Component.text(" - ", NamedTextColor.GRAY)
                    .append(Component.text(lock.getName(), NamedTextColor.DARK_AQUA))
                    .append(Component.text(" ["))
                    .append(Component.text("Use", NamedTextColor.DARK_AQUA)
                            .clickEvent(ClickEvent.runCommand("/lock use " + lock.getName()))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to use this lock"))))
                    .append(Component.text("]", NamedTextColor.GRAY));
            player.sendMessage(line);
        }
        return true;
    }

    private boolean handleUse(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(color("&6[XantamLock]&b Usage: /lock use <name|this|that>"));
            return true;
        }

        String target = args[1].toLowerCase();
        if (target.equals("this") || target.equals("that")) {
            Block targetBlock = player.getTargetBlockExact(5);
            if (targetBlock == null) {
                player.sendMessage(color("&6[XantamLock]&c No valid target block found."));
                return true;
            }
            String location = targetBlock.getWorld().getName() + "," + targetBlock.getX() + "," + targetBlock.getY() + "," + targetBlock.getZ();
            for (Lock lock : LockManager.getLocks(player.getUniqueId())) {
                if (lock.getParts().contains(location)) {
                    LockManager.setFocusedLock(player.getUniqueId(), lock.getId());
                    player.sendMessage(color("&6[XantamLock]&a Now using lock '&3" + lock.getName() + "&a'."));
                    return true;
                }
            }
            player.sendMessage(color("&6[XantamLock]&c That block is not part of any lock you own."));
            return true;
        }

        Lock lock = LockManager.getLockByName(player.getUniqueId(), target);
        if (lock == null) {
            player.sendMessage(color("&6[XantamLock]&c Lock '&f" + target + "&c' not found."));
            return true;
        }

        LockManager.setFocusedLock(player.getUniqueId(), lock.getId());
        player.sendMessage(color("&6[XantamLock]&a Now using lock '&3" + lock.getName() + "&a'."));
        return true;
    }

    private boolean handleShow(Player player, String[] args) {
        Lock lock;
        if (args.length >= 2) {
            lock = LockManager.getLockByName(player.getUniqueId(), args[1]);
        } else {
            String focused = LockFocusTracker.getFocusedLockId(player.getUniqueId());
            if (focused == null) {
                player.sendMessage(color("&6[XantamLock]&b You have no focused lock."));
                return true;
            }
            lock = LockManager.getLockById(player.getUniqueId(), focused);
        }

        if (lock == null) {
            player.sendMessage(color("&6[XantamLock]&c Lock not found."));
            return true;
        }

        player.sendMessage(color("&6[XantamLock]&b Lock Info:"));
        player.sendMessage(color("&bName: &3" + lock.getName()));
        player.sendMessage(color("&bMode: &3" + lock.getMode()));
        player.sendMessage(color("&bParts linked: &3" + lock.getParts().size()));
        return true;
    }

    private boolean handleEdit(Player player, String[] args) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("name")) {
            player.sendMessage(color("&6[XantamLock]&b Usage: /lock edit name <old> <new>"));
            return true;
        }

        String oldName = args[2];
        String newName = args[3];
        Lock lock = LockManager.getLockByName(player.getUniqueId(), oldName);
        if (lock == null) {
            player.sendMessage(color("&6[XantamLock]&c Lock '&f" + oldName + "&c' not found."));
            return true;
        }
        if (LockManager.hasLockWithName(player.getUniqueId(), newName)) {
            player.sendMessage(color("&6[XantamLock]&c A lock with that new name already exists."));
            return true;
        }

        lock.setName(newName);
        LockStorage.saveLock(lock);
        player.sendMessage(color("&6[XantamLock]&a Renamed lock '&f" + oldName + "&a' to '&f" + newName + "&a'."));
        return true;
    }

    private boolean handlePunch(Player player) {
        if (LockManager.getFocusedLock(player.getUniqueId()) == null) {
            player.sendMessage(color("&6[XantamLock]&b Focus a lock first using /lock use or /lock list."));
            return true;
        }

        LockToolHandler.enablePunch(player.getUniqueId());
        player.sendMessage(color("&6[XantamLock]&a Punch mode enabled. Left-click to add, right-click to remove one entity."));
        return true;
    }

    private boolean handleTool(Player player) {
        if (LockManager.getFocusedLock(player.getUniqueId()) == null) {
            player.sendMessage(color("&6[XantamLock]&b Focus a lock first using /lock use or /lock list."));
            return true;
        }

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Locking Tool");
        meta.setLore(Arrays.asList(
                ChatColor.RED + "A tool used to lock entities.",
                ChatColor.RED + "Left click to lock, right click to remove"
        ));
        stick.setItemMeta(meta);

        player.getInventory().addItem(stick);
        LockToolHandler.enableTool(player.getUniqueId());
        player.sendMessage(color("&6[XantamLock]&a Locking tool given."));
        return true;
    }

    private boolean handleVisualise(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(color("&6[XantamLock]&b Usage: /lock visualise [on|off]"));
            return true;
        }

        boolean on = args[1].equalsIgnoreCase("on");
        VisualiserHandler.toggleVisual(player, on);
        player.sendMessage(color("&6[XantamLock]&a Lock visualisation " + (on ? "enabled" : "disabled") + "."));
        return true;
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
