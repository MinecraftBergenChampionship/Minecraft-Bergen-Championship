package gg.mbc.event.scoring.comparators;

import gg.mbc.event.MBCEvent;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.scoring.ScoreManager;
import gg.mbc.event.scoring.TeamScoreReport;
import gg.mbc.event.teams.EventTeam;

import java.util.Comparator;

public class EventTeamComparator implements Comparator<EventTeam> {
    private final ScoreManager scoreManager;
    private final TeamManager teamManager;

    public EventTeamComparator(ScoreManager scm, TeamManager tm) {
        this.scoreManager = scm;
        this.teamManager = tm;
    }

    /**
     * The following sequence is used to compare team scores.
     * - Compare multiplied team scores.
     * - If equal, compare unmultiplied team scores.
     * - Otherwise, go by sorting ID (color precedence)
     * @param team1 Team to compare scores
     * @param team2 Team to compare scores
     * @return A positive number if player1 has a greater score.
     *         A negative number if player2 has a greater score.
     *         Zero otherwise.
     */
    @Override
    public int compare(EventTeam team1, EventTeam team2) {
        TeamScoreReport team1Scores = scoreManager.getTeamScore(team1.type());
        TeamScoreReport team2Scores = scoreManager.getTeamScore(team2.type());
        double scores1 = team1Scores.getMultipliedScore();
        double scores2 = team2Scores.getMultipliedScore();
        if (scores1 != scores2) {
            return Double.compare(scores1, scores2);
        }

        int rawScores1 = team1Scores.getRawScore();
        int rawScores2 = team2Scores.getRawScore();
        if (rawScores1 != rawScores2) {
            return Integer.compare(rawScores1, rawScores2);
        }

        return Integer.compare(team1.type().sortID, team2.type().sortID);
    }
}
