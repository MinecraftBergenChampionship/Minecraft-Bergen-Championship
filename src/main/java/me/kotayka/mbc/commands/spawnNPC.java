package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.NPCs.NPC;
import me.kotayka.mbc.NPCs.NPCManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spawnNPC implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            MBC.npcManager.showAll(MBC.npcManager.createNPC(((Player) sender).getPlayer(), ((Player) sender).getLocation()));
        }
        return true;
    }

}