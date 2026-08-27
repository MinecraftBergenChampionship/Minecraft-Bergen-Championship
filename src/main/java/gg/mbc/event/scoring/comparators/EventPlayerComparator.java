package gg.mbc.event.scoring.comparators;

import gg.mbc.event.MBCEvent;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.scoring.PlayerScoreReport;
import gg.mbc.event.scoring.ScoreManager;

import java.util.Comparator;

public class EventPlayerComparator implements Comparator<EventPlayer> {
    private final ScoreManager scoreManager;
    private final TeamManager teamManager;
    private EventTeamComparator teamComparator;

    public EventPlayerComparator(ScoreManager scoreManager, TeamManager teamManager, EventTeamComparator comparator) {
        this.scoreManager = scoreManager;
        this.teamManager = teamManager;
        this.teamComparator = comparator;
    }

    /**
     * The following sequence is used to compare player scores.
     * - Compare individual scores.
     * - If equal, compare multiplied scores.
     * - If equal, compare (multiplied) team scores.
     * - If equal, compare unmultiplied team scores.
     * - Otherwise, default to
     * This sequence is to avoid randomness with placement for voting comeback mechanics.
     * @param player1 Player to compare scores
     * @param player2 Player to compare scores
     * @return A positive number if player1 has a greater score.
     *         A negative number if player2 has a greater score.
     *         Zero otherwise.
     */
    @Override
    public int compare(EventPlayer player1, EventPlayer player2) {
        PlayerScoreReport playerScores1 = scoreManager.getPlayerScore(player1);
        PlayerScoreReport playerScores2 = scoreManager.getPlayerScore(player2);
        int scores1 = playerScores1.getScore();
        int scores2 = playerScores2.getScore();
        if (scores1 != scores2) {
            return Integer.compare(scores1, scores2);
        }

        double multipliedScores1 = playerScores1.getMultipliedScore();
        double multipliedScores2 = playerScores2.getMultipliedScore();
        if (multipliedScores1 != multipliedScores2) {
            return Double.compare(multipliedScores1, multipliedScores2);
        }

        return teamComparator.compare(teamManager.getTeam(player1.getPlayer().getUniqueId()), teamManager.getTeam(player2.getPlayer().getUniqueId()));
    }
}
