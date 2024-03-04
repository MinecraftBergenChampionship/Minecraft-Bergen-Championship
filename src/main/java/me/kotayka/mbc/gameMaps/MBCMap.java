package me.kotayka.mbc.gameMaps;

import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;

public abstract class MBCMap {
    private final World world;
    private final Location INTRO_LOCATION; // may be null if map doesn't have one
    public String mapName;

    protected MBCMap(World world, @Nullable Location INTRO_LOCATION) {
        this.INTRO_LOCATION = INTRO_LOCATION;
        this.world = world;
    }

    protected MBCMap(World world) {
        this.world = world;
        INTRO_LOCATION = null;
    }

    /**
     * @requires INTRO_LOCATION != null
     * @return Location of intro, if all players are warped to some region
     */
    public Location getIntroLocation() {
        return INTRO_LOCATION;
    }

    public World getWorld() {
        return world;
    }
}
