package me.kotayka.mbc.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mbchelp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        String msg =
                ChatColor.GREEN + "----------[MBC]----------\n" + ChatColor.RESET +
                "/placement: get your individual score. Aliases: /place, /score, /standing\n" +
                "\n/individual: get the top 8 scores and your score. Aliases: /indiv, /leaderboard, /lb\n" +
                "\n/scores: get placement and top scores. Aliases: /places, /placements\n, /standings\n" +
                "\n/ping <player>: get player's ping; get your own ping with no arguments\n" +
                "\n/scoring <game>: get scoring for the specified game.\n" +
                "\n/checkbuild: checks the Build Mart build built in the plot the player used the command in.\n" +
                ChatColor.GREEN + "-------------------------";
        sender.sendMessage(msg);
        return true;
    }
}
