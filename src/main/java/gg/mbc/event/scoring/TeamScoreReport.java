package gg.mbc.event.scoring;

import gg.mbc.event.teams.EventTeam;

public class TeamScoreReport {
    private final EventTeam team;
    private int placement;
    private int scoreRaw = 0;
    private double scoreMultiplied = 0;

    TeamScoreReport(EventTeam team) {
        this.team = team;
        this.placement = team.type().sortID;
    }

    TeamScoreReport(EventTeam team, int placement) {
        this.team = team;
        this.placement = placement;
    }

    public void addScoreUnMultiplied(int score) {
        this.scoreRaw += score;
    }
    public void addMultipliedScore(double score) {
        scoreMultiplied += score;
    }
    public void addMultipliedScore(double score, double multiplier) {
        scoreMultiplied += (score * multiplier);
    }

    public int getRawScore() {
        return scoreRaw;
    }

    public double getMultipliedScore() {
        return scoreMultiplied;
    }
}
