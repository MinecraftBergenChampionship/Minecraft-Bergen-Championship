package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class startevent implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (!p.isOp()) {
            p.sendMessage(ChatColor.RED + "This is an admin restricted command!");
            return false;
        }

        // check if ready check has even begun
        if (!MBC.getInstance().readyCheck) {
            p.sendMessage("Use /readycheck first, once all teams are online!");
            p.sendMessage("You must run readycheck again if a team responded notready.");
            return false;
        }

        // check we're good
        if (MBC.getInstance().ready.size() != MBC.getInstance().getValidTeams().size()) {
            p.sendMessage("At least one team is not ready!");
            p.sendMessage("Ready size: " + MBC.getInstance().ready.size());
            p.sendMessage("All teams: " + MBC.getInstance().getValidTeams().size());
            return false;
        }

        MBC.getInstance().startEvent();
        return true;
    }
}
