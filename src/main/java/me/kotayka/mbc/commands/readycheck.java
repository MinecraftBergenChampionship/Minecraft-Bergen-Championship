package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class readycheck implements CommandExecutor {

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

        MBC.getInstance().readyCheck = true;

        StringBuilder readyString = new StringBuilder();
        readyString.append(ChatColor.GREEN + "\n+=+=+=+=+=+=+=+=+=+=" + MBC.MBC_STRING_PREFIX + "+=+=+=+=+=+=+=+=+=+=\n")
                .append("\n /ready has been enabled! When your team is fully ready, use /ready!\n")
                .append(ChatColor.GREEN + "\n+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+\n");
        Bukkit.broadcastMessage(readyString.toString());

        return true;
    }
}
