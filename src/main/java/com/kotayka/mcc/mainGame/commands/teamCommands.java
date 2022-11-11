package com.kotayka.mcc.mainGame.commands;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.teamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class teamCommands implements CommandExecutor {

    private final teamManager team;
    private final MCC mcc;

    public teamCommands(teamManager team, MCC mcc) {
        this.team = team;
        this.mcc = mcc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals(args[0])) {
                if (team.teamNames.contains(args[1])) {
                    for (Participant participant : mcc.players.participants) {
                        if (participant.ign.equals(p.getName())) {
                            participant.team = args[1];
                            switch (args[1]) {
                                case "RedRabbits":
                                    participant.fullName = "Red Rabbits";
                                    mcc.teams.get("Red Rabbits").add(participant);
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.RED+"Red Rabbits.");
                                    break;
                                case "YellowYaks":
                                    mcc.teams.get("Yellow Yaks").add(participant);
                                    participant.fullName = "Yellow Yaks";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.YELLOW+"Yellow Yaks.");
                                    break;
                                case "GreenGuardians":
                                    mcc.teams.get("Green Guardians").add(participant);
                                    participant.fullName = "Green Guardians";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.GREEN+"Green Guardians.");
                                    break;
                                case "BlueBats":
                                    mcc.teams.get("Blue Bats").add(participant);
                                    participant.fullName = "Blue Bats";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.BLUE+"Blue Bats.");
                                    break;
                                case "PurplePandas":
                                    mcc.teams.get("Purple Pandas").add(participant);
                                    participant.fullName = "Purple Pandas";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.DARK_PURPLE+"Purple Pandas.");
                                    break;
                                case "PinkPiglets":
                                    mcc.teams.get("Pink Piglets").add(participant);
                                    participant.fullName = "Pink Piglets";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.LIGHT_PURPLE+"Pink Piglets.");
                                    break;
                            }
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
