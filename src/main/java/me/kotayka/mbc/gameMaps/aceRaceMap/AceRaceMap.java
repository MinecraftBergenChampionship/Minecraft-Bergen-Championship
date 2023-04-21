package me.kotayka.mbc.gameMaps.aceRaceMap;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AceRaceMap {
    public World world = Bukkit.getWorld("AceRace");;
    public List<Location> respawns;
    public List<Location> checkpoints;
    public int mapLength;

    public void loadCheckpoints(Location[] respawns, Location[] checkpoints) {
        this.respawns = new ArrayList<>(Arrays.asList(respawns));
        this.checkpoints = new ArrayList<>(Arrays.asList(checkpoints));

        mapLength = checkpoints.length;
    }

    public abstract void checkFinished(PlayerMoveEvent e);

    public abstract void checkDeath(PlayerMoveEvent e);

}
