package me.kotayka.mbc.gameMaps.oneshotMaps;

import me.kotayka.mbc.partygames.OneShot;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Meltdown extends OneShotMap {
    
    public Meltdown(OneShot oneshot) {
        super(oneshot);
        spawnpoints = new Location[31];
        addSpawnpoints();
        DEATH_Y = -20;
    }

    private void addSpawnpoints() {
        spawnpoints[0] = new Location(Bukkit.getWorld("Party"), -2048, -2, -1996);
        spawnpoints[1] = new Location(Bukkit.getWorld("Party"), -2026, -2, -1996);
        spawnpoints[2] = new Location(Bukkit.getWorld("Party"), -2004, -2, -1996);
        spawnpoints[3] = new Location(Bukkit.getWorld("Party"), -2006, -2, -1952);
        spawnpoints[4] = new Location(Bukkit.getWorld("Party"), -2046, -2, -1952);
        spawnpoints[5] = new Location(Bukkit.getWorld("Party"), -2048, -12, -1986);
        spawnpoints[6] = new Location(Bukkit.getWorld("Party"), -2026, -12, -1996);
        spawnpoints[7] = new Location(Bukkit.getWorld("Party"), -2004, -12, -1982);
        spawnpoints[8] = new Location(Bukkit.getWorld("Party"), -2004, -4, -1940);
        spawnpoints[9] = new Location(Bukkit.getWorld("Party"), -2004, -5, -1907);
        spawnpoints[10] = new Location(Bukkit.getWorld("Party"), -2034, -2, -1907);
        spawnpoints[11] = new Location(Bukkit.getWorld("Party"), -2035, -6, -1921);
        spawnpoints[12] = new Location(Bukkit.getWorld("Party"), -2048, 0, -1913);
        spawnpoints[13] = new Location(Bukkit.getWorld("Party"), -2049, -6, -1933);
        spawnpoints[14] = new Location(Bukkit.getWorld("Party"), -2055, -2, -1933);
        spawnpoints[15] = new Location(Bukkit.getWorld("Party"), -2060, -6, -1920);
        spawnpoints[16] = new Location(Bukkit.getWorld("Party"), -2074, -6, -1916);
        spawnpoints[17] = new Location(Bukkit.getWorld("Party"), -2069, -2, -1940);
        spawnpoints[18] = new Location(Bukkit.getWorld("Party"), -2092, -1, -1936);
        spawnpoints[19] = new Location(Bukkit.getWorld("Party"), -2099, -2, -1917);
        spawnpoints[20] = new Location(Bukkit.getWorld("Party"), -2060, -4, -1952);
        spawnpoints[21] = new Location(Bukkit.getWorld("Party"), -2093, -5, -1961);
        spawnpoints[22] = new Location(Bukkit.getWorld("Party"), -2099, -2, -1971);
        spawnpoints[23] = new Location(Bukkit.getWorld("Party"), -2087, 0, -1996);
        spawnpoints[24] = new Location(Bukkit.getWorld("Party"), -2067, -6, -1990);
        spawnpoints[25] = new Location(Bukkit.getWorld("Party"), -2074, 0, -1973);
        spawnpoints[26] = new Location(Bukkit.getWorld("Party"), -2054, -4, -1959);
        spawnpoints[27] = new Location(Bukkit.getWorld("Party"), -2062, -6, -1970);
        spawnpoints[28] = new Location(Bukkit.getWorld("Party"), -2048, -2, -1945);
        spawnpoints[29] = new Location(Bukkit.getWorld("Party"), -2042, -6, -1938);
        spawnpoints[30] = new Location(Bukkit.getWorld("Party"), -2054, -4, -1954);
    }
}
