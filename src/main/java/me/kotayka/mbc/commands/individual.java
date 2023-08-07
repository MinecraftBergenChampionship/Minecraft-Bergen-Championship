package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class individual implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            MBC.getInstance().getTopIndividual(p);
            return true;
        }
        return false;
    }
}
