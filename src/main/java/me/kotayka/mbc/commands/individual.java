package me.kotayka.mbc.commands;

import me.kotayka.mbc.Lobby;
import me.kotayka.mbc.MBC;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class individual implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (MBC.getInstance().getMinigame() instanceof Lobby && p.getGameMode().equals(GameMode.SPECTATOR)) {
                p.sendMessage("Wait until the cutscene is over!");
                return false;
            }
            if (args.length == 1) {
                if (args[0].equals("all")) {
                    if (!(sender.isOp())) {
                        p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "This is an admin restricted command!");
                        return false;
                    }
                    else {
                        MBC.getInstance().getAllIndividual(p);
                        return true;
                    }
                }
                else {
                    p.sendMessage(ChatColor.RED + "ERROR: incorrect argument.");
                }
            }
            MBC.getInstance().getTopIndividual(p);
            return true;
        }
        return false;
    }
}
