package me.kotayka.mbc.gameMaps.spleefMap;

import org.bukkit.Bukkit;
import org.bukkit.World;

public abstract class SpleefMap {
    private final String name;
    private final int deathY;
    private final World world = Bukkit.getWorld("spleef");
    private final String mapType;


    public SpleefMap(String name, int deathY, String mapType) {
        this.name = name;
        this.deathY = deathY;
        this.mapType = mapType;
    }

    public World getWorld() { return world; }

    public String Name() {
        return name;
    }

    public String getMapType() {
        return mapType;
    }

    /**
     * Paste the map represented by the class onto the playing area at (0,0)
     * Reset any map specific variables
     */
    public abstract void resetMap();

    /*
     * set entire map to air to prepare for next map
     */
    public abstract void deleteMap();

    /**
     * Erode border over time
     */
    public abstract void Border(int timeRemaining);

    public int getDeathY() { return deathY; }
}
