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
            Game game = MBC.getInstance().getGame(MBC.gameNameList.get(MBC.gameNameList.indexOf(args[0])));
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            sender.sendMessage("\n");
            sender.sendMessage(game.getScoring());
            sender.sendMessage("\n");
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            return true;
        } else if (args.length == 0) {
            if (!(MBC.getInstance().getMinigame() instanceof Game)) {
                sender.sendMessage("[" + ChatColor.GREEN + "scoring" + ChatColor.RESET + "] Usage: /scoring <game> or use during a game.");
                return true;
            }
            Game curr = MBC.getInstance().getGame();
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            sender.sendMessage("\n");
            sender.sendMessage(curr.getScoring());
            sender.sendMessage("\n");
            sender.sendMessage(ChatColor.GREEN + "---------------------------------------");
            return true;
        } else {
            sender.sendMessage("[" + ChatColor.GREEN + "scoring" + ChatColor.RESET + "] Usage: /scoring <game> or use during a game.");
            return true;
        }
    }
}
