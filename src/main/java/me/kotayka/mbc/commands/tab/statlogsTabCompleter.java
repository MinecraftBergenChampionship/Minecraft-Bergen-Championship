package me.kotayka.mbc.commands.tab;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class statlogsTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("set", "get", "directory");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("true", "false");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            return MBC.gameNameList;
        } else if (args.length == 3 && args[0].equalsIgnoreCase("get")) {
            return Arrays.asList("team", "individual");
        }
        return null;
    }
}
