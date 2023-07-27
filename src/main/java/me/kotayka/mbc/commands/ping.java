package me.kotayka.mbc.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ping implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
           Player player = null;
           if (args.length == 0) {
               player = (Player) sender;
           } else if (args.length == 1) {
               for (Player p : Bukkit.getOnlinePlayers()) {
                   if (p.getName().equals(args[0])) {
                        player = p;
                        break;
                   }
               }
               if (player == null) {
                   sender.sendMessage(ChatColor.RED+"Could not find that player!");
                   return true;
               }
           } else {
               sender.sendMessage(ChatColor.RED+"Invalid arguments: " + ChatColor.YELLOW + "/ping [player]");
                return true;
           }

           sender.sendMessage(ChatColor.GREEN+player.getName()+"'s ping: " +ChatColor.RESET+ player.getPing() + "ms");

        } else {
            return false;
        }
        return true;
    }
}