package me.kotayka.mbc.gameMaps;

import org.bukkit.World;

public abstract class MBCMap {
    private final World world;
    public String mapName;

    protected MBCMap(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }
}
