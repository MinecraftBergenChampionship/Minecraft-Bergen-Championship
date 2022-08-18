package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ready implements CommandExecutor {
    private final Players players;
    private final MCC mcc;

    public ready(Players players, MCC mcc) {
        this.players = players;
        this.mcc = mcc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && mcc.game.stage == "Waiting") {
            for (Participant p : players.participants) {
                if (p.player.getUniqueId() == ((Player) sender).getUniqueId()) {
                    if (p.team.equals("Spectator")) {
                        sender.sendMessage("You are not on a team");
                        return true;
                    }
                    else {
                        Bukkit.broadcastMessage(p.team);
                        if (mcc.startGame.teamReadyMap.get(p.team)) {
                            mcc.scoreboardManager.removeTeam(p.team);
                            mcc.startGame.numOfTeamsReady--;
                        }
                        else {
                            mcc.scoreboardManager.addTeam(p.team);
                            mcc.startGame.numOfTeamsReady++;
                            if (mcc.startGame.numOfTeamsReady >= mcc.startGame.numOfteams) {
                                mcc.game.start();
                            }
                        }
                        mcc.startGame.teamReadyMap.put(p.team, !mcc.startGame.teamReadyMap.get(p.team));
                    }
                }
            }
        }
        return true;
    }
}
