package me.kotayka.mbc.commands;

import me.kotayka.mbc.DecisionDome;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class start implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!(sender.isOp())) {
                sender.sendMessage("You do not have permission to execute this command!");
                return true;
            }

            if (args.length != 1) {
                if (args[0].equals("DecisionDome")) {
                    Participant part = Participant.getParticipant(args[1]);
                    if (part == null) {
                        sender.sendMessage(ChatColor.RED + "Please Provide a valid player name in second argument for game DecisionDome");
                        return false;
                    }
                    else {
                        if (MBC.getInstance().decisionDome == null) {
                            MBC.getInstance().decisionDome = new DecisionDome(MBC.getInstance().gameNum > 1);
                        }
                    } MBC.getInstance().decisionDome.start(part);
                    return true;
                }
                else {
                    sender.sendMessage("Please provide 1 argument");
                return false;
                }
                
            }
            if (!MBC.gameNameList.contains(args[0])) {
                sender.sendMessage("Please provide a valid game");
            }

            MBC.getInstance().startGame(MBC.gameNameList.indexOf(args[0]));
        }
        return true;
    }

}