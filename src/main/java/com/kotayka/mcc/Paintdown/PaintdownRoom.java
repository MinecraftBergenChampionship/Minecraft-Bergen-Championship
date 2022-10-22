package com.kotayka.mcc.Paintdown;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
 * Representation for Paintdown Rooms to make refilling coin boxes easier.
 * Future implements: Melting room, adding paint on walls, and de-painting
 */
public class PaintdownRoom {
    // Paintdown
    private final Paintdown paintdown;

    // List of coin locations
    // Empty list for non-REGULAR typed Rooms
    //public ArrayList<Location> coinLocations;


    // Room Type
    RoomType type;

    public PaintdownRoom(Paintdown paintdown, int X_one, int X_two, int Z_one, int Z_two, RoomType type) {
        this.paintdown = paintdown;

        this.type = type;



        /*
        if (type.equals(RoomType.REGULAR)) {
            scatterCoinCrates(X_one, X_two, Z_one, Z_two);
        }*/
    }


    /*
     * Randomly scatter coin crates around rooms of type 'REGULAR'
     * This function also populates the room's `coinLocations` arraylist.
     */

    /* this doesn't work very well, benching
    private void scatterCoinCrates(int X_one, int X_two, int Z_one, int Z_two) {
        Bukkit.broadcastMessage("Coin Crates Checkpoint");
        int x_max = Math.max(X_one, X_two);
        int x_min = Math.min(X_one, X_two);
        int z_max = Math.max(Z_one, Z_two);
        int z_min = Math.min(Z_one, Z_two);

        int timesRan = 0;
        // Each regular room has 5 coin crates
        for (int i = 0; i < 5; i++) {
            int randomX = (int) (Math.random() * x_max + x_min);
            int randomZ = (int) (Math.random() * z_max + z_min);
            int randomY = (int) (Math.random() * -4); // 0 to -4
            Location loc = new Location(paintdown.world, randomX, randomY, randomZ);
            Bukkit.broadcastMessage("timesRan: " + ++timesRan);
            Bukkit.broadcastMessage("coinLocations.size() == " + coinLocations.size());
            /*
             * Eligible spots are:
             * Block(x,y,z) == AIR
             * Block(x,y+1,z) == AIR
             * Block(x,y-1,z) != AIR
             */
    /*
            if (loc.getBlock().getType() == Material.AIR
                    && paintdown.world.getBlockAt((int) loc.getX(), (int) loc.getY() + 1, (int) loc.getZ()).getType() == Material.AIR
                    && paintdown.world.getBlockAt((int) loc.getX(), (int) loc.getY() - 1, (int) loc.getZ()).getType() != Material.AIR) {
                Bukkit.broadcastMessage("Coin Checkpoint 2");
                loc.getBlock().setType(Material.LODESTONE);
                coinLocations.add(loc);
                Bukkit.broadcastMessage("Coin Checkpoint 3");
            } else {
                // Repeat until we have 5 coin crates
                // not sure if this is bad practice since technically the runtime is unbounded buuuut idc
                i--;
            }
        }
    } */

    /* Reset Coin Crates */
    /*
    public static void resetCoinCrates() {
        for (Location l : coinLocations) {
            l.getBlock().setType(Material.AIR);
        }
    } */
}
