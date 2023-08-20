package me.kotayka.mbc.commands.tab;

import me.kotayka.mbc.MBC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class teamscoreTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("set", "add", "subtract");
        } else if (args.length == 2) {
            return MBC.teamNames;
        }

        return null;
    }
}
