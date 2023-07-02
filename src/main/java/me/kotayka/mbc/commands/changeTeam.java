package me.kotayka.mbc.commands;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.MBCTeam;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class changeTeam implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 0) {
                p.sendMessage(ChatColor.RED + "Please Provide a Team Name");
                return false;
            }
            if (args.length == 1) {
                MBCTeam team = MBCTeam.getTeam(args[0]);
                if (team == null) {
                    p.sendMessage(ChatColor.RED + "Please Provide a valid team name");
                }
                Participant.getParticipant(p).changeTeam(team);
            }
            if (args.length == 2) {
                Participant x = Participant.getParticipant(args[1]);
                MBCTeam team = MBCTeam.getTeam(args[0]);
                if (x == null) {
                    p.sendMessage(ChatColor.RED + "Please Provide a valid player name");
                }
                if (team == null) {
                    p.sendMessage(ChatColor.RED + "Please Provide a valid team name");
                }
                x.changeTeam(MBCTeam.getTeam(args[0]));
            }
        }
        return true;
    }

}
