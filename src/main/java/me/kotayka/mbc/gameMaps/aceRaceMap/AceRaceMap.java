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

/**
 * @implNote All AceRaceMaps should have first checkpoint and respawn at the very beginning of lap by default,
 *           or else the current code will not track laps correctly. The first checkpoint / finish line should be
 *           marked by carpet.
 */
public abstract class AceRaceMap extends Map {
    public String mapName;
    public World world = Bukkit.getWorld("AceRace");
    public List<Location> respawns;
    public List<Location> checkpoints;
    public int mapLength;

    protected AceRaceMap() {
        super(Bukkit.getWorld("AceRace"));
    }

    public void loadCheckpoints(Location[] respawns, Location[] checkpoints) {
        this.respawns = new ArrayList<>(Arrays.asList(respawns));
        this.checkpoints = new ArrayList<>(Arrays.asList(checkpoints));

        mapLength = checkpoints.length;
    }

    public abstract void checkDeath(PlayerMoveEvent e);

    public List<Location> getRespawns() {
        return respawns;
    }

    public List<Location> getCheckpoints() {
        return checkpoints;
    }

}
