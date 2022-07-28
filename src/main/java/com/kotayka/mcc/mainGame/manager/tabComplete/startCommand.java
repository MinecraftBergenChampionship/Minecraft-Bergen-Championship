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
<<<<<<< HEAD
            "SG"
=======
            "TEST",
            "Skybattle"
>>>>>>> 70395db9e14f699126824f0de8849cc4e403574d
    };
    
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> gameList = new ArrayList<String>(Arrays.asList(games));
        
        return gameList;
        
    }

}
