package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.manager.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ddstart  implements CommandExecutor {

    public final Game game;

    public ddstart(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Dodgebolt" + ChatColor.GREEN + " Started");
            game.startDD(args[0], args[1]);
        }
        return true;
    }
}