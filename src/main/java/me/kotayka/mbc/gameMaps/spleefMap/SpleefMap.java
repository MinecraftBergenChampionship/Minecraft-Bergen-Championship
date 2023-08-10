package me.kotayka.mbc.gameMaps.spleefMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import org.bukkit.Bukkit;

public abstract class SpleefMap extends MBCMap {
    private final String name;
    private final int deathY;

    public SpleefMap(String name, int deathY) {
        super(Bukkit.getWorld("spleef"));
        this.name = name;
        this.deathY = deathY;
    }

    public String Name() {
        return name;
    }

    /**
     * Paste the map represented by the class onto the playing area at (0,0)
     * Reset any map specific variables
     */
    public abstract void resetMap();

    /**
     * Erode border over time
     */
    public abstract void Border(int timeRemaining);

    public int getDeathY() { return deathY; }
}
