package com.kotayka.mcc.Scoreboards;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreboardTeamManager {

    private final MCC mcc;
    private final ScoreboardManager manager;

    List<String> teamNames = new ArrayList<>(Arrays.asList("RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"));

    public ScoreboardTeamManager(MCC mcc, ScoreboardManager manager) {
        this.mcc = mcc;
        this.manager = manager;
    }

    public void newPlayer(String team, Player p) {
        for (ScoreboardPlayer p2 : manager.playerList) {
            p2.teams[teamNames.indexOf(team)].addEntry(p.getName());
        }
        for (ScoreboardPlayer p2 : manager.playerList) {
            manager.players.get(p.getUniqueId()).teams[teamNames.indexOf(manager.playerTeams.get(manager.players.get(p2.player.player.getUniqueId())).teamName)].addEntry(p2.player.ign);
        }
    }
}
