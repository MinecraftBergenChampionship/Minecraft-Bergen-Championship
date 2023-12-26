package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;

public class SpleefPlayer extends GamePlayer {
    private Participant lastDamager = null;
    private int kills = 0;
    private int resetTime = -1;
    private int placement = -1;

    public SpleefPlayer(Participant p) {
        super(p);
    }

    public int getKills() { return kills; }
    public int getPlacement() { return placement; }
    public void incrementKills() { kills++; }
    public void resetKills() { kills = 0; }
    public Participant getLastDamager() { return lastDamager; }
    public void setLastDamager(Participant p) { lastDamager = p; }
    public void resetKiller() { lastDamager = null; }
    public void setPlacement(int p) { placement = p; }
    public int getResetTime() { return resetTime; }
    public void setResetTime(int time) { resetTime = time; }
}
