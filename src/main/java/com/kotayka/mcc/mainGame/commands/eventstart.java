package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class eventstart implements CommandExecutor {
    private final MCC mcc;

    public eventstart(MCC mcc) {
        this.mcc = mcc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            mcc.startGame.numOfteams= Integer.parseInt(args[0]);
            if (mcc.startGame.numOfTeamsReady >= mcc.startGame.numOfteams) {
                mcc.game.start();
            }
        }
        return true;
    }
}
