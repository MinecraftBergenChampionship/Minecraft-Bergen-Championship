package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Players {
    public List<Player> players = new ArrayList<Player>();
    public List<Participant> participants = new ArrayList<>();
    public final MCC mcc;
    public List<Player> spectators = new ArrayList<>();

    public Players(MCC mcc) {
        this.mcc = mcc;
    }

    public void getOnlinePlayers() {
        for(Player p : Bukkit.getOnlinePlayers()){
            players.add(p);
            Participant x = new Participant(p);
            participants.add(x);
        }
    }

    public void addPlayer(Player p) {
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};
        List<String> team = new ArrayList<>(Arrays.asList(teamNames));
        players.add(p);
        Participant x = new Participant(p);
        participants.add(x);
        mcc.roundScores.put(p.getName(), 0);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

}
