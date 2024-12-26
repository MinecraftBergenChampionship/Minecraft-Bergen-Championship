package me.kotayka.mbc.commands.tab;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class playMusicTabCompletion implements TabCompleter {
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            ArrayList<String> games = new ArrayList<>();
            for (String s : MBC.gameNameList) games.add(s);
            for (String s : MBC.partyGameNameList) games.add(s);
            games.remove("Party");
            return games;
        } else if (args.length == 2) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                playerNames.add(p.getName());
            }
            return playerNames;
        }
        List<String> x = new ArrayList<>();
        return x;
    }
}
