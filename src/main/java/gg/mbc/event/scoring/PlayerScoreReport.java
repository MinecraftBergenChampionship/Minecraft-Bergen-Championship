package gg.mbc.event.scoring;

import gg.mbc.event.players.EventPlayer;

public class PlayerScoreReport {
    private final EventPlayer player;
    private int score = 0;
    private double scoreMultiplied = 0;

    PlayerScoreReport(EventPlayer player) {
        this.player = player;
    }

    public void addScore(int score) {
        this.score += score;
    }
    public void addMultipliedScore(double score) {
        scoreMultiplied += score;
    }
    public void addMultipliedScore(double score, double multiplier) {
        scoreMultiplied += (score * multiplier);
    }

    public int getScore() {
        return score;
    }

    public double getMultipliedScore() {
        return scoreMultiplied;
    }
}
