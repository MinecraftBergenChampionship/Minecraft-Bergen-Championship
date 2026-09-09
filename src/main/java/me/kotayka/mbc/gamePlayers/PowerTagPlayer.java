package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class PowerTagPlayer extends GamePlayer {
    private int kills = 0;
    private int survivals = 0;
    private int hideRounds = 0;
    private int timeSurvived = 0;
    private List<PowerTagPlayer> playersFound = new ArrayList<>();
    public Map<PowerTagPlayer, Location> tensedList = new HashMap<>();

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

    public void addPlayerKiller(PowerTagPlayer p) {
        playersFound.add(p);
    }

    public List<PowerTagPlayer> getPlayersFound() {
        return playersFound;
    }

    public void addTensed(PowerTagPlayer tenser, Location l) {
        tensedList.put(tenser, l);
    }

    public void removeTensed(PowerTagPlayer tenser) {
        tensedList.remove(tenser);
    }

    
}
