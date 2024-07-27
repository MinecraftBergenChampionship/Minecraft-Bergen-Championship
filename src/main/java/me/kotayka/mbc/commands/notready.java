package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class notready implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (!MBC.getInstance().readyCheck) {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "It is not time to unready yet!");
            return true;
        }

        MBC.getInstance().readyCheck = false;
        Participant par = Participant.getParticipant(p);
        MBC.getInstance().unready(par.getTeam(), p);

        MBC.announce("A team is NOT READY! The ready check has been disabled.");
        return true;
    }
}
