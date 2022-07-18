package com.kotayka.mcc.Skybattle.managers;

import org.bukkit.Location;
import org.bukkit.World;

/*
 * This class is literally only here
 * so I can override hashCode() to make things
 * work in a hash map.  idk if this is even
 * that efficient lol
 */
public class SkybattleLocation extends Location {
    World world;
    int x;
    int y;
    int z;

    public SkybattleLocation(World world, int x, int y, int z) {
        super(world, x, y, z);
    }

    /*
    @Override
    public boolean equals(World world, int x, int y, int z) {
        return this.world == world && this.x == x && this.y == y && this.z == z;
    }

    @Override
    public int hashCode() {

    }
    */

}
