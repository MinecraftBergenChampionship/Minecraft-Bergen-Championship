package com.kotayka.mcc.mainGame.manager.tabComplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class startCommand implements TabCompleter {

    public String[] games = {
            "TGTTOS",
            "SG",
            "TEST",
            "Skybattle",
            "BSABM",
    };
    
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> gameList = new ArrayList<String>(Arrays.asList(games));
        
        return gameList;
        
    }

}
