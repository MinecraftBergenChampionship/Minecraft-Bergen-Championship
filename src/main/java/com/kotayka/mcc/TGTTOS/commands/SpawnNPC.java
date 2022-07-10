package com.kotayka.mcc.TGTTOS.commands;

import com.kotayka.mcc.TGTTOS.TGTTOS;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.kotayka.mcc.TGTTOS.managers.NPCManager;

import java.io.IOException;

public class SpawnNPC implements CommandExecutor {

    public final NPCManager npcManager;
    public final TGTTOS tgttos;

    public SpawnNPC(NPCManager npcManager, TGTTOS tgttos) {
        this.npcManager = npcManager;
        this.tgttos = tgttos;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getServer().getConsoleSender().sendMessage("NPC Command Activated");
        if (sender instanceof Player && tgttos.enabled()) {
//            npcManager.spawnNPC((Player) sender);
        }
        return true;
    }
}
