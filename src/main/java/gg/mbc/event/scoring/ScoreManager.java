package gg.mbc.event.scoring;

import gg.mbc.event.games.GameType;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.scoring.comparators.EventPlayerComparator;
import gg.mbc.event.scoring.comparators.EventTeamComparator;
import gg.mbc.event.teams.EventTeam;
import gg.mbc.event.teams.TeamType;
import gg.mbc.util.Tuple;

import java.util.*;

public class ScoreManager {
    private final Map<UUID, PlayerScoreReport> playerScores;
    private final Map<TeamType, TeamScoreReport> teamScores;
    private final List<Tuple<GameType, GameScoreReport>> gameScores;

    private final SortedMap<EventTeam, Double> teamLeaderboard;
    private final SortedMap<EventPlayer, Integer> individualLeaderboard;

    // Comparators
    private final EventTeamComparator teamComparator;
    private final EventPlayerComparator playerComparator;

    public ScoreManager(TeamManager tm) {
        gameScores = new ArrayList<>();
        playerScores = new HashMap<>();
        teamScores = new HashMap<>();

        teamComparator = new EventTeamComparator(this, tm);
        playerComparator = new EventPlayerComparator(this, tm, teamComparator);

                teamLeaderboard = new TreeMap<>(teamComparator);
        individualLeaderboard = new TreeMap<>(playerComparator);
    }

    public PlayerScoreReport getPlayerScore(EventPlayer player) {
        PlayerScoreReport res = playerScores.get(player.getPlayer().getUniqueId());
        if (res == null) {
            throw new RuntimeException("Unable to find Score Report for player " + player.getPlayer().getName());
        }
        return res;
    }

    public PlayerScoreReport getPlayerScore(UUID id) {
        PlayerScoreReport res = playerScores.get(id);
        if (res == null) {
            throw new RuntimeException("Unable to find Score Report for player with UUID " + id);
        }
        return res;
    }

    public TeamScoreReport getTeamScore(TeamType type) {
        TeamScoreReport res = teamScores.get(type);
        if (res == null) {
            throw new RuntimeException("Unable to find Score Report for " + type);
        }
        return res;
    }

    public void addGameScores(GameScoreReport report) {
        gameScores.add(new Tuple<>(report.getGameType(), report));
    }

    /**
     * Retrieve ScoreReport of a game based on when it was played, given by index
     * @param index 0-indexed register on when a game was played
     * @throws RuntimeException if invalid game index
     * @return Scores of the desired game
     */
    public GameScoreReport getGameScore(int index) {
        if (index < 0 || index > gameScores.size()) {
            throw new RuntimeException("Unable to get game");
        }

        return gameScores.get(index).second;
    }

    /**
     * Retrieve ScoreReport of a game based on GameType
     * @param game Type of game to fetch scores of
     * @throws RuntimeException if invalid game type
     * @return Scores of the desired game
     */
    public GameScoreReport getGameScore(GameType game) {
        for (Tuple<GameType, GameScoreReport> pair : gameScores) {
            if (pair.first == game) {
                return pair.second;
            }
        }
        throw new RuntimeException("Unable to get game");
    }
}
