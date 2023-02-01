package com.kotayka.mcc.mainGame.manager.tabComplete;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class tCommands implements TabCompleter {
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                playerNames.add(p.getName());
            }
            return playerNames;
        }
        else if (args.length == 2) {
            String[] t = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};
            List<String> teams = new ArrayList<>(Arrays.asList(t));
            return teams;
        }
        List<String> x = new ArrayList<>();
        return x;
    }
}
