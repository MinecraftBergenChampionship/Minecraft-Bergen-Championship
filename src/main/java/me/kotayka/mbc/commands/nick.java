package me.kotayka.mbc.commands;

import me.kotayka.mbc.Participant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class nick implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        if (!(sender.isOp())) {
            sender.sendMessage("Please let an admin know to let you on a team.");
            return false;
        }

        sender.sendMessage("Please note this command currently does not work and I don't have time to fix it right now. :p");

        if (args.length != 3) {
            sender.sendMessage("Usage: /nick <nickname> <player>");
            return false;
        }

        String nick = args[1];
        String player = args[2];


        Participant x = Participant.getParticipant(player);
        if (x == null) {
            sender.sendMessage("Please make valid name!");
            return false;
        }

        x.getPlayer().setDisplayName(nick);
        return true;
    }
}
