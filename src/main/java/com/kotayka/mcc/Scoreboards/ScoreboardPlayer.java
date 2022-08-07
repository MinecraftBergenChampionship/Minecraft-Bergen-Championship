package com.kotayka.mcc.Scoreboards;

import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardPlayer {
    public int roundScore = 0;
    public int gameScore = 0;
    public Map<String, Objective> objectiveMap = new HashMap<>();
    public Team[] teams;
    public Map<Objective, Map<Integer, String>> lines = new HashMap();
    public Objective currentObj = null;
    public Object[][] teamLines = new Object[][]{{"RedRabbits",-1},{"YellowYaks",-1},{"GreenGuardians",-1},{"BlueBats",-1},{"PurplePandas",-1},{"PinkPiglets",-1}};
    public final Scoreboard board;
    public final Participant player;

    public ScoreboardPlayer(Scoreboard board, Participant player) {
        this.board = board;
        this.player = player;
    }
}
