package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class teamManager {
    public final List<Participant> players;
    public final MCC mcc;

    public Map roundScores = new HashMap();
    public Map totalScores = new HashMap();
    public teamManager(List<Participant> players, MCC mcc) {
        this.players = players;
        this.mcc = mcc;
    }
}
