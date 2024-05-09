package me.kotayka.mbc.commands;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class scoring implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) { return false; }
        if (args.length == 1) {
            if (!MBC.gameNameList.contains(args[0])) {
                sender.sendMessage("[" + ChatColor.GREEN + "scoring" + ChatColor.RESET + "] Invalid game name!");
                return true;
            }
            String game = args[0];
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            sender.sendMessage("\n");
            sender.sendMessage(MBC.getScoring(game));
            sender.sendMessage("\n");
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            return true;
        } else if (args.length == 0) {
            if (!(MBC.getInstance().getMinigame() instanceof Game)) {
                sender.sendMessage("[" + ChatColor.GREEN + "scoring" + ChatColor.RESET + "] Usage: /scoring <game> or use during a game.");
                return true;
            }
            String curr = MBC.getInstance().getGame().name();
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            sender.sendMessage("\n");
            sender.sendMessage(MBC.getScoring(curr));
            sender.sendMessage("\n");
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            return true;
        } else {
            sender.sendMessage("[" + ChatColor.GREEN + "scoring" + ChatColor.RESET + "] Usage: /scoring <game> or use during a game.");
            return true;
        }
    }
}
