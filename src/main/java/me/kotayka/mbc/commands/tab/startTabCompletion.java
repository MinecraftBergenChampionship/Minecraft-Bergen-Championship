package me.kotayka.mbc.commands.tab;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class startTabCompletion implements TabCompleter {
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return MBC.gameNameList;
        }

        return new ArrayList<>();
    }
}
