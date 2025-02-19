package me.kotayka.mbc.commands;

import me.kotayka.mbc.Lobby;
import me.kotayka.mbc.MBC;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class gameresults implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (MBC.getInstance().getMinigame() instanceof Lobby && p.getGameMode().equals(GameMode.SPECTATOR)) {
                p.sendMessage("Wait until the cutscene is over!");
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED+"Usage: /gamenum [number (0-5)]");
                return false;
            }

            int n;
            try {
                n = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED+"Could not parse provided amount " + args[0] + ".");
                return false;
            }

            if (!(n > 0 && n <= MBC.GAME_COUNT)) {
                sender.sendMessage(ChatColor.RED+"Invalid number! Use a number 1-6 inclusive!");
                return false;
            }

                MBC.getInstance().gameResults(n, p);
                return true;
        }
        return false;
    }
}
