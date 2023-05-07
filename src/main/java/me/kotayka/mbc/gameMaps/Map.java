package me.kotayka.mbc.gameMaps;

import org.bukkit.World;

public abstract class Map {
    private final World world;

    protected Map(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }
}
