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
                                    // prevent double adding to same team
                                    if (participant.teamNameFull.equals("Red Rabbits")) return false;
                                    mcc.teams.get("Red Rabbits").add(participant);
                                    participant.teamNameFull = "Red Rabbits";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.RED+"Red Rabbits.");
                                    break;
                                case "YellowYaks":
                                    if (participant.teamNameFull.equals("Yellow Yaks")) return false;
                                    mcc.teams.get("Yellow Yaks").add(participant);
                                    participant.teamNameFull = "Yellow Yaks";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.YELLOW+"Yellow Yaks.");
                                    break;
                                case "GreenGuardians":
                                    if (participant.teamNameFull.equals("Green Guardians")) return false;
                                    mcc.teams.get("Green Guardians").add(participant);
                                    participant.teamNameFull = "Green Guardians";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.GREEN+"Green Guardians.");
                                    break;
                                case "BlueBats":
                                    if (participant.teamNameFull.equals("Blue Bats")) return false;
                                    mcc.teams.get("Blue Bats").add(participant);
                                    participant.teamNameFull = "Blue Bats";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.BLUE+"Blue Bats.");
                                    break;
                                case "PurplePandas":
                                    if (participant.teamNameFull.equals("Purple Pandas")) return false;
                                    mcc.teams.get("Purple Pandas").add(participant);
                                    participant.teamNameFull = "Purple Pandas";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.DARK_PURPLE+"Purple Pandas.");
                                    break;
                                case "PinkPiglets":
                                    if (participant.teamNameFull.equals("Pink Piglets")) return false;
                                    mcc.teams.get("Pink Piglets").add(participant);
                                    participant.teamNameFull = "Pink Piglets";
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
