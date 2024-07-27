package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TeamScoreSorter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FinaleGame extends Minigame {
    protected World world;
    protected MBCTeam firstPlace = null;
    protected MBCTeam secondPlace = null;
    // Score: { Team one points , Team two points }
    protected int[] score = new int[2];
    private final String[] INTRODUCTION;
    private int introLine = 0;

    public FinaleGame(String name, String[] intro) {
        super(name);
        initLogger();

        if (MBC.getInstance().getValidTeams().size() == 1) {
            // only for debug
            firstPlace = getValidTeams().get(0);
            secondPlace = MBC.getInstance().spectator;
        } else {
            List<MBCTeam> teams = getValidTeams();
            teams.sort(new TeamScoreSorter());
            Collections.reverse(teams);
            firstPlace = teams.get(0);
            secondPlace = teams.get(1);
        }
        this.INTRODUCTION = intro;
    }

    public FinaleGame(String name, @NotNull MBCTeam firstPlace, @NotNull MBCTeam secondPlace, String[] intro) {
        super(name);
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
        this.INTRODUCTION = intro;
    }

    @Override
    public void start() {

    }

    @Override
    public void loadPlayers() {

    }

    @Override
    public void events() {

    }

    @Override
    public void createScoreboard(Participant p) {
        newObjective(p);
        String winsOne = "";
        String winsTwo = "";
        switch (score[0]) {
            case 0 -> winsOne = "① ① ③ " + firstPlace.teamNameFormat();
            case 1 -> winsOne = "② ① ③ " + firstPlace.teamNameFormat();
            case 2 -> winsOne = "② ② ③ " + firstPlace.teamNameFormat();
            case 3 -> winsOne = "② ② ④ " + firstPlace.teamNameFormat();
        }
        switch (score[1]) {
            case 0 -> winsTwo = "① ① ③ " + secondPlace.teamNameFormat();
            case 1 -> winsTwo = "② ① ③ " + secondPlace.teamNameFormat();
            case 2 -> winsTwo = "② ② ③ " + secondPlace.teamNameFormat();
            case 3 -> winsTwo = "② ② ④ " + secondPlace.teamNameFormat();
        }
        createLine(22, ChatColor.AQUA.toString()+ChatColor.BOLD+"Final Game: " + ChatColor.RESET+name(), p);
        createLine(1, winsOne, p);
        createLine(0, winsTwo, p);
    }

    public void Introduction() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(INTRODUCTION[introLine++]);
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }
}
