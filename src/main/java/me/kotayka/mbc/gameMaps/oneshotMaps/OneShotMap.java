package me.kotayka.mbc.gameMaps.oneshotMaps;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.partygames.OneShot;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public abstract class OneShotMap extends MBCMap {
    private OneShot oneshot;

    public Location[] spawnpoints;
    public double DEATH_Y;

    protected OneShotMap(OneShot oneshot) {
        super(Bukkit.getWorld("Party"));
        this.oneshot = oneshot;
    }

}
