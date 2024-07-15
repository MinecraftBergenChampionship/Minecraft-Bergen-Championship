package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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

        if (MBC.getInstance().getValidTeams().isEmpty()) {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "There are no non-spectator teams.");
            return false;
        }

        MBC.getInstance().ready.clear();
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            MBC.getInstance().ready.put(t, Boolean.FALSE);
        }
        MBC.getInstance().readyCheck = true;


        MBC.announce("/ready has been enabled! When your team is fully ready, use /ready!");
        p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "Ensure statlogging is on if this is an actual event!");

        return true;
    }
}
