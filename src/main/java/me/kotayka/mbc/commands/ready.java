package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ready implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (!MBC.getInstance().readyCheck) {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "It is not time to ready up yet!");
            return true;
        }

        Participant par = Participant.getParticipant(p);
        MBC.getInstance().ready(par.getTeam(), p);
        return true;
    }
}
