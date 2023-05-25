package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.Team;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class start implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length != 1) {
                sender.sendMessage("Please provide 1 argument");
                return false;
            }
            if (!MBC.gameNameList.contains(args[0])) {
                sender.sendMessage("Please provide a valid game");
            }

            MBC.getInstance().startGame(MBC.gameNameList.indexOf(args[0]));
        }
        return true;
    }

}