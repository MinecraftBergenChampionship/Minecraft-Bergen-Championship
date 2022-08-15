package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.manager.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class start implements CommandExecutor {

    public final Game game;

    public start(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            game.startGame();
            sender.sendMessage(ChatColor.RED+args[0]+ChatColor.GREEN+" Started");
            game.changeGame(args[0]);
        }
        return true;
    }
}
