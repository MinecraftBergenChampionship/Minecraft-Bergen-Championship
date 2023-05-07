package me.kotayka.mbc.gameMaps.aceRaceMap;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.Map;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AceRaceMap extends Map {
    private List<Location> respawns;
    private List<Location> checkpoints;
    public int mapLength;

    protected AceRaceMap() {
        super(Bukkit.getWorld("AceRace"));
    }

    public void loadCheckpoints(Location[] respawns, Location[] checkpoints) {
        this.respawns = new ArrayList<>(Arrays.asList(respawns));
        this.checkpoints = new ArrayList<>(Arrays.asList(checkpoints));

        mapLength = checkpoints.length;
    }

    public abstract void checkFinished(PlayerMoveEvent e);

    public abstract void checkDeath(PlayerMoveEvent e);

    public List<Location> getRespawns() {
        return respawns;
    }

    public List<Location> getCheckpoints() {
        return checkpoints;
    }

}
