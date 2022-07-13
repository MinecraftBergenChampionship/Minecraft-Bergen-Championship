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
                    for (Participant participant : team.players) {
                        if (participant.ign == p.getName()) {
                            participant.team = args[1];
                            switch (args[1]) {
                                case "RedRabbits":
                                    participant.fullName = "Red Rabbits";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.RED+"Red Rabbits");
                                    break;
                                case "YellowYaks":
                                    participant.fullName = "Yellow Yaks";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.YELLOW+"Yellow Yaks");
                                    break;
                                case "GreenGuardians":
                                    participant.fullName = "Green Guardians";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.GREEN+"Green Guardians");
                                    break;
                                case "BlueBats":
                                    participant.fullName = "Blue Bats";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.BLUE+"Blue Bats");
                                    break;
                                case "PurplePandas":
                                    participant.fullName = "Purple Pandas";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.DARK_PURPLE+"Purple Pandas");
                                    break;
                                case "PinkPiglets":
                                    participant.fullName = "Pink Piglets";
                                    Bukkit.broadcastMessage(ChatColor.GOLD+args[0]+ChatColor.WHITE+" has joined the "+ChatColor.LIGHT_PURPLE+"Pink Piglets");
                                    break;
                            }
                            Bukkit.broadcastMessage(participant.fullName);
                        }
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        mcc.teams.get(player.getName())[team.teamNames.indexOf(args[1])].addEntry(p.getName());
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (mcc.scoreboards.containsKey(player.getName())) {
                        mcc.scoreboards.get(player.getName());
                    }
                }
            }
        }
        return true;
    }
}
