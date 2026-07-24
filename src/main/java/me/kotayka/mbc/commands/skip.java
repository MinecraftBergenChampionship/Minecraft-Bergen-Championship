package me.kotayka.mbc.commands;

import me.kotayka.mbc.Lobby;
import me.kotayka.mbc.MBC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class skip implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (!sender.isOp()) {
            sender.sendMessage("This is an admin only command!");
            return false;
        }

        if (MBC.getInstance().getMinigame().timeRemaining <= 0) return false;

        if (MBC.getInstance().logStats() && (args.length != 1 || !args[0].equalsIgnoreCase("confirm"))) {
            sender.sendMessage("Please do /skip confirm if event is ongoing!");
            return false;
        }

        if (MBC.getInstance().logStats() && args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            MBC.getInstance().getMinigame().timeRemaining = 1;
            return true;
        }

        if ((!MBC.getInstance().logStats() || !(MBC.getInstance().getMinigame() instanceof Lobby))) {
            MBC.getInstance().getMinigame().timeRemaining = 1;
            return true;
        }


        sender.sendMessage("This is a debug only command!");
        return false;
    }
}