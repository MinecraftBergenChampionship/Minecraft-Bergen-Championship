package gg.mbc.commands;

import gg.mbc.event.MBCEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class debug implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!(player.isOp())) {
            player.sendMessage(Component.text("You must be admin to use this command.", NamedTextColor.RED));
            return false;
        }

        if (args.length == 0) {
            player.sendMessage("Please supply an appropriate debug criteria.");
            return false;
        }

        if (args.length > 1) {
            player.sendMessage(Component.text("Usage: /debug [criteria]", NamedTextColor.RED));
            return false;
        }

        if (args[0].equalsIgnoreCase("teams") || args[0].equalsIgnoreCase("team")) {
            MBCEvent.getInstance().debugTeams();
            return true;
        }

        player.sendMessage(Component.text("Unknown debug criteria", NamedTextColor.RED));
        return false;
    }
}
