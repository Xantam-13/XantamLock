package com.xantamlock.core.command;

import com.xantamlock.core.lock.Lock;
import com.xantamlock.core.lock.LockManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class LockTabCompleter implements TabCompleter {

    private static final List<String> MAIN_SUBCOMMANDS = Arrays.asList(
            "create", "use", "list", "show", "edit", "punch", "tool", "visualise"
    );

    private static final List<String> EDIT_ARGS = Arrays.asList("name");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            return partialMatch(args[0], MAIN_SUBCOMMANDS);
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "use":
            case "show":
                if (args.length == 2) {
                    return partialMatch(args[1], getLockNames(player));
                }
                break;

            case "edit":
                if (args.length == 2) {
                    return partialMatch(args[1], EDIT_ARGS);
                } else if (args.length == 3) {
                    return partialMatch(args[2], getLockNames(player));
                }
                break;

            case "visualise":
                if (args.length == 2) {
                    return partialMatch(args[1], Arrays.asList("on", "off"));
                }
                break;
        }

        return Collections.emptyList();
    }

    private List<String> getLockNames(Player player) {
        List<String> names = new ArrayList<>();
        for (Lock lock : LockManager.getLocks(player.getUniqueId())) {
            names.add(lock.getName());
        }
        return names;
    }

    private List<String> partialMatch(String arg, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(arg.toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
    }
}
