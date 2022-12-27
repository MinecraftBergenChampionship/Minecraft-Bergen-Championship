package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.teamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class teamCommands implements CommandExecutor {

    private final teamManager team;
    private final MCC mcc;

    private final List<String> teamNames = Arrays.asList(
            "RedRabbits", "YellowYaks", "GreenGuardians",
            "BlueBats", "PurplePandas", "PinkPiglets"
    );
    public teamCommands(teamManager team, MCC mcc) {
        this.team = team;
        this.mcc = mcc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals(args[0])) {
                if (teamNames.contains(args[1])) {
                    for (Participant participant : mcc.players.participants) {
                        if (participant.ign.equals(p.getName())) {
                            participant.setTeam(args[1]);
                            Participant.participantsOnATeam.add(participant);
                            mcc.scoreboardManager.addPlayer(participant);
                        }
                    }
                    if (!mcc.startGame.teamReadyMap.containsKey(args[1])) {
                        mcc.startGame.teamReadyMap.put(args[1], false);
                    }
                }
            }
        }
        return true;
    }
}
