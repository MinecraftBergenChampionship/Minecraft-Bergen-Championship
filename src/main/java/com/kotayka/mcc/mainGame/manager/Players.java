package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class Players {
    public List<Player> players = new ArrayList<Player>();
    public List<Participant> partipants = new ArrayList<>();
    public final MCC mcc;

    public Players(MCC mcc) {
        this.mcc = mcc;
    }

    public void getOnlinePlayers() {
        for(Player p : Bukkit.getOnlinePlayers()){
            players.add(p);
            Participant x = new Participant(p);
            partipants.add(x);
            mcc.createScoreboard(x);
            mcc.roundScores.put(p.getName(), 0);
        }
    }

    public void addPlayer(Player p) {
        players.add(p);
        Participant x = new Participant(p);
        partipants.add(x);
        mcc.createScoreboard(x);
        mcc.roundScores.put(p.getName(), 0);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

}
