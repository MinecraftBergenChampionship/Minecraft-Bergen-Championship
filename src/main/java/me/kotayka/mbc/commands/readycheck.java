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

        if (MBC.getInstance().getValidTeams().size() == 0) {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "There are no non-spectator teams.");
            return false;
        }

        MBC.getInstance().readyCheck = true;

        p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "Ensure that statlogging is on if this is an actual event!");
        MBC.announce("/ready has been enabled! When your team is fully ready, use /ready!");

        return true;
    }
}
