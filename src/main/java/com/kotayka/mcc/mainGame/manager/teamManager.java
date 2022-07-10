package com.kotayka.mcc.mainGame.manager;

import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class teamManager {
    public String[] teamNamesFull = {"Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets"};
    public final List<Participant> players;
    public final List<org.bukkit.scoreboard.Team> teams;
    public final List<String> teamNames;

    public Map roundScores = new HashMap();
    public teamManager(List<Participant> players, List<Team> teams, List<String> teamNames) {
        this.players = players;
        this.teams = teams;
        this.teamNames = teamNames;
    }
}
