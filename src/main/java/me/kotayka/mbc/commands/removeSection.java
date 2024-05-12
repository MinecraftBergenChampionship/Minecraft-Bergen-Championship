package me.kotayka.mbc.commands;

import me.kotayka.mbc.DecisionDome;
import me.kotayka.mbc.MBC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class removeSection implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED+"This command is admin restricted!");
            return false;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED+"Invalid arguments. Usage: /removeSection [gameName]. If the name has spaces, use an underscore (_).");
            sender.sendMessage(ChatColor.RED+"Please use this command before decision dome starts!");
        }

        if (MBC.getInstance().decisionDome == null) {
            sender.sendMessage("There is no active instance of Decision Dome.");
            return false;
        }

        DecisionDome dd = MBC.getInstance().decisionDome;
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < dd.gameNames.size(); i++) {
            String s = dd.gameNames.get(i).substring(0, 2);
            names.add(s.replace(" ", "_").toLowerCase());
        }

        if (!(names.contains(args[0].toLowerCase()))) {
            sender.sendMessage(ChatColor.RED+"Invalid game name!");
            return false;
        } else {
            dd.removeSection(args[0]);
            sender.sendMessage(ChatColor.GREEN+"Removing section for " + args[0] + ".");
            return true;
        }
    }
}
