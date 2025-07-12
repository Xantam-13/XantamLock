// ... all existing imports ...
import com.xantamlock.core.database.LockStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

private static final List<String> MAIN_SUBCOMMANDS = Arrays.asList(
        "create", "use", "list", "show", "edit", "punch", "tool", "visualise", "access"
);

@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage(ChatColor.RED + "Only players may use this command.");
        return true;
    }

    if (args.length == 0) {
        player.sendMessage(color("&6[XantamLock]&b Use /lock create <name> to create a new lock."));
        return true;
    }

    switch (args[0].toLowerCase()) {
        case "punch": return handlePunch(player);
        case "tool": return handleTool(player);
        case "create": return handleCreate(player, args);
        case "list": return handleList(player);
        case "use": return handleUse(player, args);
        case "show": return handleShow(player, args);
        case "edit": return handleEdit(player, args);
        case "visualise": return handleVisualise(player, args);
        case "access": return handleAccess(player, args);
        default:
            player.sendMessage(color("&6[XantamLock]&b Unknown subcommand."));
            return true;
    }
}

// ... existing methods unchanged ...

private boolean handleAccess(Player player, String[] args) {
    if (args.length < 5) {
        player.sendMessage(color("&6[XantamLock]&b Usage: /lock access [add|remove] [player|faction] <target> <lockname>"));
        return true;
    }

    String action = args[1].toLowerCase();
    String type = args[2].toLowerCase();
    String target = args[3];
    String lockName = args[4];

    Lock lock = LockManager.getLockByName(player.getUniqueId(), lockName);
    if (lock == null) {
        player.sendMessage(color("&6[XantamLock]&c You do not own a lock named '&f" + lockName + "&c'."));
        return true;
    }

    if (type.equals("player")) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(color("&6[XantamLock]&c Player '&f" + target + "&c' not found."));
            return true;
        }
        String uuid = targetPlayer.getUniqueId().toString();

        if (action.equals("add")) {
            lock.addTrustedPlayer(uuid);
            player.sendMessage(color("&6[XantamLock]&a Player '&f" + target + "&a' trusted on lock '&f" + lock.getName() + "&a'."));
        } else if (action.equals("remove")) {
            lock.removeTrustedPlayer(uuid);
            player.sendMessage(color("&6[XantamLock]&e Player '&f" + target + "&e' untrusted from lock '&f" + lock.getName() + "&e'."));
        } else {
            player.sendMessage(color("&6[XantamLock]&c Invalid action. Use add/remove."));
            return true;
        }

    } else if (type.equals("faction")) {
        if (action.equals("add")) {
            lock.addTrustedFaction(target);
            player.sendMessage(color("&6[XantamLock]&a Faction '&f" + target + "&a' trusted on lock '&f" + lock.getName() + "&a'."));
        } else if (action.equals("remove")) {
            lock.removeTrustedFaction(target);
            player.sendMessage(color("&6[XantamLock]&e Faction '&f" + target + "&e' untrusted from lock '&f" + lock.getName() + "&e'."));
        } else {
            player.sendMessage(color("&6[XantamLock]&c Invalid action. Use add/remove."));
            return true;
        }
    } else {
        player.sendMessage(color("&6[XantamLock]&c Invalid type. Use player or faction."));
        return true;
    }

    LockStorage.saveLock(lock);
    return true;
}
