package me.kotayka.mbc.gameMaps.aceRaceMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class semoiB extends AceRaceMap {
    Location[] respawns = {
            new Location(getWorld(), -2158, 13, -2303, 90, 0),  // default
            new Location(getWorld(), -2281, 13, -2241, 30, 0),
            new Location(getWorld(), -2306, 13, -2184, 0, 0),
            new Location(getWorld(), -2279, 12, -2069, -30, 0),
            new Location(getWorld(), -2260, 19, -2044, -45, 0),
            new Location(getWorld(), -2169, 10, -2010, -90, 0),
            new Location(getWorld(), -2038, 12, -2083, -135, 0),
            new Location(getWorld(), -2013, 13, -2131, -150, 0),
            new Location(getWorld(), -2048, 18, -2256, 135, 0),
            new Location(getWorld(), -2131, 13, -2302, 90, 0)
    };

    Location[] checkpoints = {
            new Location(getWorld(), -2158, 13, -2303),    // /tp default
            new Location(getWorld(), -2246, 18, -2269),
            new Location(getWorld(), -2297, 23, -2215),
            new Location(getWorld(), -2298, 16, -2094),
            new Location(getWorld(), -2260, 19, -2044),
            new Location(getWorld(), -2200, 13, -2010),
            new Location(getWorld(), -2054, 22, -2058),
            new Location(getWorld(), -2027, 12, -2100),
            new Location(getWorld(), -2035, 14, -2228),
            new Location(getWorld(), -2093, 13, -2286)
    };

    public semoiB() {
        super(0, new Location(Bukkit.getWorld("AceRace"), -2160, 31, -2271), "Lava");
        mapName = "semoiB";
        loadCheckpoints(respawns, checkpoints);
    }

    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 14; y <= 16; y++) {
            for (int z = -2309; z <= -2299; z++) {
                getWorld().getBlockAt(-2160, y, z).setType(block); //main
                getWorld().getBlockAt(-2152, y, z).setType(block); //back
            }
        }

        // sides
        for (int x = -2152; x >= -2160; x--) {
            for (int y = 14; y <= 16; y++) {
                getWorld().getBlockAt(x, y, -2298).setType(block);
                getWorld().getBlockAt(x, y, -2310).setType(block);
            }
        }
    }
}