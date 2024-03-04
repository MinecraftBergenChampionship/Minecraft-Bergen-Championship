package me.kotayka.mbc.gameMaps.aceRaceMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @implSpec All AceRaceMaps should have first checkpoint and respawn at the very beginning of lap by default,
 *           or else the current code will not track laps correctly. The first checkpoint / finish line should be
 *           marked by carpet.
 */
public abstract class AceRaceMap extends MBCMap {
    public World world = AceRace.world;
    public List<Location> respawns;
    public List<Location> checkpoints;
    public int mapLength;

    public List<String> deathObjects;

    private final int deathY;

    public AceRaceMap(int deathY, Location intro, String... deathObject) {
        super(Bukkit.getWorld("AceRace"), intro);
        this.deathY = deathY;
        deathObjects=new ArrayList<>(Arrays.asList(deathObject));
    }

    public void loadCheckpoints(Location[] respawns, Location[] checkpoints) {
        this.respawns = new ArrayList<>(Arrays.asList(respawns));
        this.checkpoints = new ArrayList<>(Arrays.asList(checkpoints));

        mapLength = checkpoints.length;
    }

    public boolean checkDeath(Location l) {
        if (l.getY() < deathY) {
            return true;
        }

        for (String death : deathObjects) {
            switch (death) {
                case "Lava":
                    if (l.getBlock().getType() == Material.LAVA) return true;
                    break;
            }
        }

        return false;
    }

    public List<Location> getRespawns() {
        return respawns;
    }

    /**
     * Either place barriers or remove barriers at start of race
     * @param barriers true if setting barriers, false if removing barriers
     */
    public abstract void setBarriers(boolean barriers);

    public List<Location> getCheckpoints() {
        return checkpoints;
    }

}