package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Players {
    public List<Player> players = new ArrayList<Player>();
    public List<Participant> partipants = new ArrayList<>();
    public final MCC mcc;
    public List<Player> spectators = new ArrayList<>();

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
            loadScoreboardVars(p.getUniqueId());
            mcc.createTeams(x);
        }
    }

    public void addPlayer(Player p) {
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};
        List<String> team = new ArrayList<>(Arrays.asList(teamNames));

        players.add(p);
        Participant x = new Participant(p);
        partipants.add(x);
        mcc.createScoreboard(x);
        mcc.roundScores.put(p.getName(), 0);
        loadScoreboardVars(p.getUniqueId());
        mcc.createTeams(x);

        for (Participant player : partipants) {
            mcc.teams.get(player.ign)[team.indexOf(player.team)].addEntry(p.getName());
        }
    }

    public void loadScoreboardVars(UUID uuid) {
        mcc.maps.put(uuid, "Starting");
        mcc.roundNums.put(uuid, 0);
        mcc.time.put(uuid, 120);
        mcc.previousStandings.put(uuid, new Integer[]{0, 0, 0, 0, 0, 0});
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

}
