package com.kotayka.mcc.BSABM.commands;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class checkBuild implements CommandExecutor {
    private final MCC mcc;

    public checkBuild(MCC mcc) {
        this.mcc = mcc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && mcc.game.stage.equals("BSABM")) {
            mcc.bsabm.mapUpdateForCommand(((Player) sender).getLocation(), (Player) sender);
        }
        return true;
    }
}