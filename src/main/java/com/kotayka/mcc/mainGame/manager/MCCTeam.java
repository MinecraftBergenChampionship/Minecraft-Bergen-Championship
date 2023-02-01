package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;

public class MCCTeam {
    private final MCC mcc;

    // Enum representation of the team name.
    private Team teamName;

    // String representation of the team name.
    private String teamNameFull;

    // Icon used as a prefix for team members' names.
    private String icon;

    // ChatColor for when members of this team type in chat or for other graphics such as kills.
    private ChatColor chatColor;

    // Used for leather armor and fireworks.
    private Color color;

    private List<Participant> playersOnTeam = new ArrayList<>(MCC.PLAYERS_PER_TEAM);

    // Representations for score; assuming we will be rounding, so using `int`
    private int unmultipliedScore = 0;
    private int multipliedScore = 0;

    public MCCTeam(MCC mcc, Team teamName) {
        this.mcc = mcc;
        this.teamName = teamName;

        switch (teamName) {
            case RED_RABBITS -> {
                chatColor = ChatColor.RED;
                color = Color.RED;
                icon = ChatColor.WHITE + "Ⓡ ";
                teamNameFull = "Red Rabbits";
            }
            case YELLOW_YAKS -> {
                chatColor = ChatColor.YELLOW;
                color = Color.YELLOW;
                icon = ChatColor.WHITE + "Ⓨ ";
                teamNameFull = "Yellow Yaks";
            }
            case GREEN_GUARDIANS -> {
                chatColor = ChatColor.GREEN;
                color = Color.GREEN;
                icon = ChatColor.WHITE + "Ⓖ ";
                teamNameFull = "Green Guardians";
            }
            case BLUE_BATS -> {
                chatColor = ChatColor.BLUE;
                color = Color.BLUE;
                icon = ChatColor.WHITE + "Ⓑ ";
                teamNameFull = "Blue Bats";
            }
            case PURPLE_PANDAS -> {
                chatColor = ChatColor.DARK_PURPLE;
                color = Color.PURPLE;
                icon = ChatColor.WHITE + "Ⓤ ";
                teamNameFull = "Purple Pandas";
            }
            case PINK_PIGLETS -> {
                chatColor = ChatColor.LIGHT_PURPLE;
                color = Color.fromRGB(243, 139, 170);
                icon = ChatColor.WHITE + "Ⓟ ";
                teamNameFull = "Pink Piglets";
            }
            default -> {
                color = Color.WHITE;
                chatColor = ChatColor.WHITE;
                teamNameFull = "Spectators";
            }
        }
    }

    public int Size() { return playersOnTeam.size(); }
    public String getIcon() { return icon; }
    public Color getColor() { return color; }
    public String getTeamName() { return teamNameFull; }
    public ChatColor getChatColor() { return chatColor; }

    // RETURNS THE TEAM ENUM CONSTANT ASSOCIATED WITH THIS TEAM,
    // NOT TO BE CONFUSED WITH THE ACTUAL MCCTeam OBJECT
    public Team getTeam() { return teamName; }

    public void announceTeamDeath() {
        Bukkit.broadcastMessage(icon + chatColor + teamNameFull + ChatColor.WHITE + " have been eliminated!");
    }

    public List<Participant> getPlayers() {
        return playersOnTeam;
    }

}
