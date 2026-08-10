package gg.mbc.commands;

import gg.mbc.event.MBCEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class changeteam implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }

        if (!(sender.isOp())) {
            p.sendMessage("Please let an admin know to put you on a team.");
            return false;
        }
        if (args.length == 0) {
            p.sendMessage(Component.text("Please provide a Team Name", NamedTextColor.RED));
            return false;
        }
        if (args.length == 1) {
            boolean success = MBCEvent.getInstance().getTeamManager().changeTeam(p, args[0]);
            if (!success) {
                p.sendMessage(Component.text("Please provide a valid Team Name", NamedTextColor.RED));
                return false;
            }
            return true;
        }
        if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().equalsIgnoreCase(args[1])) {
                    boolean success = MBCEvent.getInstance().getTeamManager().changeTeam(p, args[0]);
                    if (!success) {
                        p.sendMessage(Component.text("Please provide a valid Team Name", NamedTextColor.RED));
                        return false;
                    }
                    return true;
                }
            }
            p.sendMessage(Component.text("Please provide a valid Player Name", NamedTextColor.RED));
        }
        return false;
    }
}
