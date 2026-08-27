package gg.mbc.event.scoring;

import gg.mbc.event.games.GameType;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.teams.TeamType;

import java.util.Map;
import java.util.TreeMap;

public class GameScoreReport {
    private final GameType type;
    private final Map<EventPlayer, Integer> individualScores;
    private final Map<TeamType, Integer> teamScores;

    GameScoreReport(GameType game) {
        this.type = game;
        this.individualScores = new TreeMap<>();
        this.teamScores = new TreeMap<>();
    }

    public GameType getGameType() {
        return type;
    }

}
