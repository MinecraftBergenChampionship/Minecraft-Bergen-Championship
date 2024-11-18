package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class PowerTagPlayer extends GamePlayer {
    private int kills = 0;
    private int survivals = 0;
    private int hideRounds = 0;
    private int timeSurvived = 0;

    public PowerTagPlayer(Participant p) {
        super(p);
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void incrementKills() {
        this.kills++;
    }

    public int getSurvivals() {
        return survivals;
    }

    public void setSurvivals(int survivals) {
        this.survivals = survivals;
    }

    public void incrementSurvivals() {
        this.survivals++;
    }

    public int getHideRounds() {
        return hideRounds;
    }

    public void setHideRounds(int hideRounds) {
        this.hideRounds = hideRounds;
    }

    public void incrementHideRounds() {
        this.hideRounds++;
    }

    public int getTimeSurvived() {
        return timeSurvived;
    }

    public void setTimeSurvived(int timeSurvived) {
        this.timeSurvived = timeSurvived;
    }

    public void incrementTimeSurvived(int extraTime) {
        this.timeSurvived+=extraTime;
    }
}
