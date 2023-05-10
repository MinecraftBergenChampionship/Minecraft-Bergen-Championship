package me.kotayka.mbc.gameMaps.aceRaceMap;

import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import me.kotayka.mbc.gamePlayers.GamePlayer;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerMoveEvent;

public class Biomes extends AceRaceMap {
    Location[] respawns = {
            new Location(getWorld(), 3,26, 150, 90, 0),  // default
            new Location(getWorld(), -59, 26, 140, 125, 0),
            new Location(getWorld(), -119, 27, 77, 135, 0),
            new Location(getWorld(), -138, 26, -58, -160, 0),
            new Location(getWorld(), -49, 33, -144, -90, 0),
            new Location(getWorld(), 57, 26, -137, -90, 0),
            new Location(getWorld(), 103, 26, -109, -44, 0),
            new Location(getWorld(), 138, 26, -62, -10, 0),
            new Location(getWorld(), 136, 26, 67, 35, 0),
            new Location(getWorld(), 95, 25, 119, 65, 0)
    };

    Location[] checkpoints = {
            new Location(getWorld(), 3, 26, 150),    // default
            new Location(getWorld(), -22, 28, 150),
            new Location(getWorld(), -109, 32, 103),
            new Location(getWorld(), -150, 28, -25),
            new Location(getWorld(), -77, 54, -129),
            new Location(getWorld(), 7, 24, -144),
            new Location(getWorld(), 102, 30, -110),
            new Location(getWorld(), 119, 34, -93),
            new Location(getWorld(), 150, 26, 25),
            new Location(getWorld(), 125, 26, 84)
    };

    public Biomes(AceRace ar) {
        super(ar);
        mapName = "Biomes";
        loadCheckpoints(respawns, checkpoints);
    }

    public void checkDeath(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getY() < 0 || (e.getPlayer().getLocation().getBlock().getType() == Material.LAVA)) {
            int checkpoint = ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).checkpoint;
            e.getPlayer().teleport(ACE_RACE.map.getRespawns().get((checkpoint == 0) ? ACE_RACE.map.mapLength-1 : checkpoint-1));
            e.getPlayer().setFireTicks(0);
        }
    }

    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;

        short y = 27;
        short z = 145;

        // main strip
        for (; y <= 28; y++) {
            for (; z <= 155; z++) {
                getWorld().getBlockAt(0, y, z).setType(block);
            }
        }
        short x = 3;
        y = 27;
        // sides
        for (; x >= 1; x--) {
            for (; y <= 28; y++) {
                getWorld().getBlockAt(x, y, 156).setType(block);
                getWorld().getBlockAt(x, y, 144).setType(block);
            }
        }
    }
}