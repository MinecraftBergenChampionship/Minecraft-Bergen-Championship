package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class despawnNPCs implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            // this should be unnecessary since has op permission but i added it anyway
            if (!sender.isOp()) { sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!"); return false; }
            MBC.npcManager.removeAllNPCs();
            sender.sendMessage(ChatColor.GREEN + " NPCs successfully removed.");
        }
        return true;
    }

}