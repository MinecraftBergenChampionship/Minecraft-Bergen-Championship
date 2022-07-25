package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class teamManager {
    public String[] teamNamesFull = {"Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets"};
    public final List<Participant> players;
    public final List<String> teamNames;
    public final MCC mcc;

    public Map roundScores = new HashMap();
    public Map totalScores = new HashMap();
    public teamManager(List<Participant> players, List<String> teamNames, MCC mcc) {
        this.players = players;
        this.teamNames = teamNames;
        this.mcc = mcc;
    }
}
