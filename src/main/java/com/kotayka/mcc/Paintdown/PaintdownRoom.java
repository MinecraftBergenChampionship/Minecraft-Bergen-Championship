package com.kotayka.mcc.Paintdown;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/*
 * Representation for Paintdown Rooms to make refilling coin boxes easier.
 * Future implements: Melting room, adding paint on walls, and de-painting
 */
public class PaintdownRoom {
    // Paintdown
    private final Paintdown paintdown;

    // Room bounds, (X1,Z1) and (X2, Z2)
    // Used for identifying whether players are still in the room.
    private final int X_ONE;
    private final int X_TWO;
    private final int Z_ONE;
    private final int Z_TWO;

    // List of coin locations
    // Empty list for non-REGULAR typed Rooms
    public ArrayList<Location> coinLocations;


    // Room Type
    RoomType type;

    public PaintdownRoom(Paintdown paintdown, int X_one, int X_two, int Z_one, int Z_two, RoomType type) {
        this.paintdown = paintdown;

        this.X_ONE = X_one;
        this.X_TWO = X_two;
        this.Z_ONE = Z_one;
        this.Z_TWO = Z_two;

        this.type = type;

        if (type.equals(RoomType.REGULAR)) {
            scatterCoinCrates(X_ONE, X_TWO, Z_ONE, Z_TWO);
        }
    }

    /*
     * Randomly scatter coin crates around rooms of type 'REGULAR'
     * This function also populates the room's `coinLocations` arraylist.
     */
    private void scatterCoinCrates(int X_one, int X_two, int Z_one, int Z_two) {
        int x_max = Math.max(X_one, X_two);
        int x_min = Math.min(X_one, X_two);
        int z_max = Math.max(Z_one, Z_two);
        int z_min = Math.min(Z_one, Z_two);

        // Each regular room has 5 coin crates
        for (int i = 0; i < 5; i++) {
            int randomX = (int) (Math.random() * x_max + x_min);
            int randomZ = (int) (Math.random() * z_max + z_min);
            int randomY = (int) (Math.random() * -4); // 0 to -4
            Location loc = new Location(paintdown.world, randomX, randomY, randomZ);

            /*
             * Eligible spots are:
             * Block(x,y,z) == AIR
             * Block(x,y+1,z) == AIR
             * Block(x,y-1,z) != AIR
             */
            if (loc.getBlock().getType() == Material.AIR
                    && paintdown.world.getBlockAt((int) loc.getX(), (int) loc.getY() + 1, (int) loc.getZ()).getType() == Material.AIR
                    && paintdown.world.getBlockAt((int) loc.getX(), (int) loc.getY() - 1, (int) loc.getZ()).getType() != Material.AIR) {
                loc.getBlock().setType(Material.LODESTONE);
                coinLocations.add(loc);
            } else {
                // Repeat until we have 5 coin crates
                // not sure if this is bad practice since technically the runtime is unbounded buuuut idc
                i--;
            }
        }
    }
}
