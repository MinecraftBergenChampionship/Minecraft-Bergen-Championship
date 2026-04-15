package me.kotayka.mbc.gameMaps.sgMaps;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;

public class SG4 extends SurvivalGamesMap {
    int[][] spawns = {{-12,-15},{0,-20},{12,-15},{17,-3},{12,9},{0,14},{-12,9},{-17,-3}};

    Location[] middleChests = {
            new Location(getWorld(), 2, 32, -2),
            new Location(getWorld(), 2, 32, -4),
            new Location(getWorld(), 1, 32,-1),
            new Location(getWorld(), 1, 32,-5),
            new Location(getWorld(), -1, 32,-1),
            new Location(getWorld(), -1, 32,-5),
            new Location(getWorld(), -2, 32,-2),
            new Location(getWorld(), -2, 32,4),
            new Location(getWorld(), 0, 32,-8),
            new Location(getWorld(), 0, 32,2),
            new Location(getWorld(), 5, 32,-3),
            new Location(getWorld(), -5, 32,-3),
    };

    public SG4() {
        super.spawns = this.spawns;
        super.middleChests = this.middleChests;
        super.spawnY = 31;
        super.mapName = "SG4";
        super.creatorName = "Team Nectar and Team Vareide";
        super.type = "Cornucopia";
        super.CENTER = new Location(getWorld(), 0, 10, 0);
        super.hasElevationBorder = false;
        //super.airdrops = false;

        resetBorder();
    }

    @Override
    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;

        getWorld().getBlockAt(15, 31, -2).setType(block);
        getWorld().getBlockAt(15, 31, -3).setType(block);
        getWorld().getBlockAt(15, 31, -4).setType(block);
        getWorld().getBlockAt(16, 31, -5).setType(block);
        getWorld().getBlockAt(17, 31, -5).setType(block);
        getWorld().getBlockAt(18, 31, -5).setType(block);
        getWorld().getBlockAt(19, 31, -4).setType(block);
        getWorld().getBlockAt(19, 31, -3).setType(block);
        getWorld().getBlockAt(19, 31, -2).setType(block);
        getWorld().getBlockAt(18, 31, -1).setType(block);
        getWorld().getBlockAt(17, 31, -1).setType(block);
        getWorld().getBlockAt(16, 31, -1).setType(block);

        getWorld().getBlockAt(11, 31, -13).setType(block);
        getWorld().getBlockAt(12, 31, -13).setType(block);
        getWorld().getBlockAt(13, 31, -13).setType(block);
        getWorld().getBlockAt(14, 31, -14).setType(block);
        getWorld().getBlockAt(14, 31, -15).setType(block);
        getWorld().getBlockAt(14, 31, -16).setType(block);
        getWorld().getBlockAt(13, 31, -17).setType(block);
        getWorld().getBlockAt(12, 31, -17).setType(block);
        getWorld().getBlockAt(11, 31, -17).setType(block);
        getWorld().getBlockAt(10, 31, -16).setType(block);
        getWorld().getBlockAt(10, 31, -15).setType(block);
        getWorld().getBlockAt(10, 31, -14).setType(block);

        getWorld().getBlockAt(-1, 31, -18).setType(block);
        getWorld().getBlockAt(0, 31, -18).setType(block);
        getWorld().getBlockAt(1, 31, -18).setType(block);
        getWorld().getBlockAt(2, 31, -19).setType(block);
        getWorld().getBlockAt(2, 31, -20).setType(block);
        getWorld().getBlockAt(2, 31, -21).setType(block);
        getWorld().getBlockAt(1, 31, -22).setType(block);
        getWorld().getBlockAt(0, 31, -22).setType(block);
        getWorld().getBlockAt(-1, 31, -22).setType(block);
        getWorld().getBlockAt(-2, 31, -21).setType(block);
        getWorld().getBlockAt(-2, 31, -20).setType(block);
        getWorld().getBlockAt(-2, 31, -19).setType(block);

        getWorld().getBlockAt(-13, 31, -13).setType(block);
        getWorld().getBlockAt(-12, 31, -13).setType(block);
        getWorld().getBlockAt(-11, 31, -13).setType(block);
        getWorld().getBlockAt(-10, 31, -14).setType(block);
        getWorld().getBlockAt(-10, 31, -15).setType(block);
        getWorld().getBlockAt(-10, 31, -16).setType(block);
        getWorld().getBlockAt(-11, 31, -17).setType(block);
        getWorld().getBlockAt(-12, 31, -17).setType(block);
        getWorld().getBlockAt(-13, 31, -17).setType(block);
        getWorld().getBlockAt(-14, 31, -16).setType(block);
        getWorld().getBlockAt(-14, 31, -15).setType(block);
        getWorld().getBlockAt(-14, 31, -14).setType(block);

        getWorld().getBlockAt(-18, 31, -1).setType(block);
        getWorld().getBlockAt(-17, 31, -1).setType(block);
        getWorld().getBlockAt(-16, 31, -1).setType(block);
        getWorld().getBlockAt(-15, 31, -2).setType(block);
        getWorld().getBlockAt(-15, 31, -3).setType(block);
        getWorld().getBlockAt(-15, 31, -4).setType(block);
        getWorld().getBlockAt(-16, 31, -5).setType(block);
        getWorld().getBlockAt(-17, 31, -5).setType(block);
        getWorld().getBlockAt(-18, 31, -5).setType(block);
        getWorld().getBlockAt(-19, 31, -4).setType(block);
        getWorld().getBlockAt(-19, 31, -3).setType(block);
        getWorld().getBlockAt(-19, 31, -2).setType(block);

        getWorld().getBlockAt(-13, 31, 11).setType(block);
        getWorld().getBlockAt(-12, 31, 11).setType(block);
        getWorld().getBlockAt(-11, 31, 11).setType(block);
        getWorld().getBlockAt(-10, 31, 10).setType(block);
        getWorld().getBlockAt(-10, 31, 9).setType(block);
        getWorld().getBlockAt(-10, 31, 8).setType(block);
        getWorld().getBlockAt(-11, 31, 7).setType(block);
        getWorld().getBlockAt(-12, 31, 7).setType(block);
        getWorld().getBlockAt(-13, 31, 7).setType(block);
        getWorld().getBlockAt(-14, 31, 8).setType(block);
        getWorld().getBlockAt(-14, 31, 9).setType(block);
        getWorld().getBlockAt(-14, 31, 10).setType(block);

        getWorld().getBlockAt(-1, 31, 16).setType(block);
        getWorld().getBlockAt(0, 31, 16).setType(block);
        getWorld().getBlockAt(1, 31, 16).setType(block);
        getWorld().getBlockAt(2, 31, 15).setType(block);
        getWorld().getBlockAt(2, 31, 14).setType(block);
        getWorld().getBlockAt(2, 31, 13).setType(block);
        getWorld().getBlockAt(1, 31, 12).setType(block);
        getWorld().getBlockAt(0, 31, 12).setType(block);
        getWorld().getBlockAt(-1, 31, 12).setType(block);
        getWorld().getBlockAt(-2, 31, 13).setType(block);
        getWorld().getBlockAt(-2, 31, 14).setType(block);
        getWorld().getBlockAt(-2, 31, 15).setType(block);

        getWorld().getBlockAt(11, 31, 11).setType(block);
        getWorld().getBlockAt(12, 31, 11).setType(block);
        getWorld().getBlockAt(13, 31, 11).setType(block);
        getWorld().getBlockAt(14, 31, 10).setType(block);
        getWorld().getBlockAt(14, 31, 9).setType(block);
        getWorld().getBlockAt(14, 31, 8).setType(block);
        getWorld().getBlockAt(13, 31, 7).setType(block);
        getWorld().getBlockAt(12, 31, 7).setType(block);
        getWorld().getBlockAt(11, 31, 7).setType(block);
        getWorld().getBlockAt(10, 31, 8).setType(block);
        getWorld().getBlockAt(10, 31, 9).setType(block);
        getWorld().getBlockAt(10, 31, 10).setType(block);
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
        border.setCenter(0, -3);
        border.setSize(400);
        border.setDamageBuffer(0);
        border.setDamageAmount(0.5);
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
