package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class minibeepstop implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (!(sender.isOp())) {
                sender.sendMessage("You do not have permission to execute this command!");
                return true;
            }
            MBC.getInstance().lobby.miniBeepEnd("Mini Beep has ended!");
        }
        return true;
    }
}
