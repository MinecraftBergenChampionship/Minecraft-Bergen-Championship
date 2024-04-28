package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class despawnNPCs implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            MBC.npcManager.removeAllNPCs();
        }
        return true;
    }

}