package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class playerCommand implements CommandExecutor {

    private final Players players;

    public playerCommand(Players players) {
        this.players = players;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (players.players.contains(sender)) {
                players.players.remove(sender);
                int p = 0;
                for (int i = 0; i < players.participants.size(); i++) {
                    if (players.participants.get(i).player == sender) {
                        p = i;
                    }
                }
                players.participants.remove(p);
                sender.sendMessage("You are now a spectator");
            }
            else {
                players.addPlayer((Player) sender);
                sender.sendMessage("You are now a player");
            }
        }
        return true;
    }
}