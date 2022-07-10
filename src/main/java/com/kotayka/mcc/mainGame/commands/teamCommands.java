package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.teamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class teamCommands implements CommandExecutor {

    private final teamManager team;

    public teamCommands(teamManager team) {
        this.team = team;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals(args[0])) {
                if (team.teamNames.contains(args[1])) {
                    for (Participant participant : team.players) {
                        if (participant.ign == p.getName()) {
                            team.teams.get(team.teamNames.indexOf(args[1])).removePlayer(p);
                            participant.team = args[1];
                            switch (args[1]) {
                                case "RedRabbits":
                                    participant.fullName = "Red Rabbits";
                                    break;
                                case "YellowYaks":
                                    participant.fullName = "Yellow Yaks";
                                    break;
                                case "GreenGuardians":
                                    participant.fullName = "Green Guardians";
                                    break;
                                case "BlueBats":
                                    participant.fullName = "Blue Bats";
                                    break;
                                case "PurplePandas":
                                    participant.fullName = "Purple Pandas";
                                    break;
                                case "PinkPiglets":
                                    participant.fullName = "Pink Piglets";
                                    break;
                            }
                        }
                    }
                    team.teams.get(team.teamNames.indexOf(args[1])).addPlayer(p);
                }
            }
        }
        return true;
    }
}
