package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class announcement implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player p = (Player) sender;
        if (!(sender.isOp())) {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "This is an admin restricted command!");
            return false;
        }

        StringBuilder str = new StringBuilder();
        str.append(ChatColor.GREEN + "\n+=+=+=+=+=+=+=+=+=+=" + MBC.MBC_STRING_PREFIX + "+=+=+=+=+=+=+=+=+=+=\n");
        for (int i = 1; i < args.length; i++) {
            str.append(args[i] + " ");
        }
        str.append(ChatColor.GREEN + "\n\n+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+\n");

        Bukkit.broadcastMessage(str.toString());

        return true;
    }
}
