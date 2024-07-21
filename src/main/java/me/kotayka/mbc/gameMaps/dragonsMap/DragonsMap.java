package me.kotayka.mbc.gameMaps.dragonsMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.partygames.Dragons;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public abstract class DragonsMap extends MBCMap {
    private Dragons dragons;

    public int maxX, minX, maxY, minY, maxZ, minZ;
    public Location SPAWN;
    public Location CENTER_OF_BUILD;
    public Location DRAGON_SPAWN;
    public double DEATH_Y;

    protected DragonsMap(Dragons dragons) {
        super(Bukkit.getWorld("Dragons"));
        this.dragons = dragons;
    }

    public abstract void resetMap();
}
