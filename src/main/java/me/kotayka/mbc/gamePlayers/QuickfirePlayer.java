package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;

public class QuickfirePlayer extends GamePlayer {
    private int kills = 0;
    private int damageDealt = 0;
    private int damageTaken = 0;
    private int deaths = 0;

    public QuickfirePlayer(Participant p) {
        super(p);
    }

    public int getKills() { return kills; }
    public void incrementKills() { kills++; }
    public void resetKills() { kills = 0; }
    public int getDamageDealt() { return damageDealt; }
    public void incrementDamageDealt() { damageDealt++; }
    public void resetDamageDealt() { damageDealt = 0; }
    public int getDamageTaken() { return damageTaken; }
    public void incrementDamageTaken() { damageTaken++; }
    public void resetDamageTaken() { damageTaken = 0; }
    public int getDeaths() { return deaths; }
    public void incrementDeaths() { deaths++; }
    public void resetDeaths() { deaths = 0; }
}
