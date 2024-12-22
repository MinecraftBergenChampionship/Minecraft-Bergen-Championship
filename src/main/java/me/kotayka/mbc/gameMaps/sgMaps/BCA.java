package me.kotayka.mbc.gameMaps.sgMaps;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;

public class BCA extends SurvivalGamesMap {
    int[][] spawns = {{0,-10},{8,-8},{10,0},{8,8},{0,10},{-8,8},{-10,0},{-8,-8}};

    Location[] middleChests = {
            new Location(getWorld(), -1, 2, 2),
            new Location(getWorld(), 1, 2, 2),
            new Location(getWorld(), 2, 2,1),
            new Location(getWorld(), 2, 2,-1),
            new Location(getWorld(), 1, 2,-2),
            new Location(getWorld(), -1, 2,-2),
            new Location(getWorld(), -2, 2,-1),
            new Location(getWorld(), -2, 2,1),
            new Location(getWorld(), 0, 3,1),
            new Location(getWorld(), 1, 3,0),
            new Location(getWorld(), 0, 3,-1),
            new Location(getWorld(), -1, 3,0),
    };

    public BCA() {
        super.spawns = this.spawns;
        super.middleChests = this.middleChests;
        super.spawnY = 2;
        super.mapName = "BCA";
        super.type = "Cornucopia";
        super.CENTER = new Location(getWorld(), 0, 10, 0);
        super.hasElevationBorder = false;
        //super.airdrops = false;

        resetBorder();
    }

    @Override
    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;

        for (int x = -1; x <= 1; x++) {
            // directly north / south
            getWorld().getBlockAt(x, 3, 8).setType(block);
            getWorld().getBlockAt(x, 3, -12).setType(block);
            getWorld().getBlockAt(x, 3, -8).setType(block);

            // east/west
            getWorld().getBlockAt(8, 3, x).setType(block);
            getWorld().getBlockAt(12, 3, x).setType(block);
            getWorld().getBlockAt(-8, 3, x).setType(block);
            getWorld().getBlockAt(-12, 3, x).setType(block);
        }

        for (int z = 9; z <= 11; z++) {
            // north/south
            getWorld().getBlockAt(2, 3, z).setType(block);
            getWorld().getBlockAt(2, 3, -z).setType(block);
            getWorld().getBlockAt(-2, 3,z).setType(block);
            getWorld().getBlockAt(-2, 3,-z).setType(block);

            // east/west
            getWorld().getBlockAt(z, 3, 2).setType(block);
            getWorld().getBlockAt(z, 3,-2).setType(block);
            getWorld().getBlockAt(-z, 3,-2).setType(block);
            getWorld().getBlockAt(-z, 3,2).setType(block);
        }

        for (int z = 7; z <= 9; z++) {
            getWorld().getBlockAt(10, 3, z).setType(block);
            getWorld().getBlockAt(6, 3, z).setType(block);
            getWorld().getBlockAt(-6, 3,z).setType(block);
            getWorld().getBlockAt(-10, 3,z).setType(block);

            getWorld().getBlockAt(10, 3, -z).setType(block);
            getWorld().getBlockAt(6, 3, -z).setType(block);
            getWorld().getBlockAt(-6, 3,-z).setType(block);
            getWorld().getBlockAt(-10, 3,-z).setType(block);

            getWorld().getBlockAt(z, 3, 10).setType(block);
            getWorld().getBlockAt(z, 3, 6).setType(block);
            getWorld().getBlockAt(z, 3,-6).setType(block);
            getWorld().getBlockAt(z, 3,-10).setType(block);

            getWorld().getBlockAt(-z, 3, 10).setType(block);
            getWorld().getBlockAt(-z, 3, 6).setType(block);
            getWorld().getBlockAt(-z, 3,-6).setType(block);
            getWorld().getBlockAt(-z, 3,-10).setType(block);
        }
    }

    @Override
    public boolean checkChest(Chest chest) {
        Location l = chest.getLocation();
        for (Location loc : middleChests) {
            if (l.equals(loc)) return false;
        }

        return l.getX() > -200 && l.getX() < 200
            && l.getZ() > -200 && l.getZ() < 200;
    }

    @Override
    public void resetBorder() {
        border.setCenter(0, 0);
        border.setSize(400);
    }

    @Override
    public void startBorder() {
        border.setSize(30, 420);
    }

    @Override
    public void Overtime() {
        border.setSize(8, 15);
    }

    // not implemented because super.hasElevationBorder is false.
    public void Border() {};
}
