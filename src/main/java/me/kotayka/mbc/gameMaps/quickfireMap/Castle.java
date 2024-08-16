package me.kotayka.mbc.gameMaps.quickfireMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.Quickfire;

import org.bukkit.entity.*;
import org.bukkit.*;

public class Castle extends QuickfireMap {
    private World world = Bukkit.getWorld("Quickfire");
    private Location TEAM_ONE_SPAWN = new Location(world, 19.5, -60, 0);
    private Location TEAM_TWO_SPAWN = new Location(world, -19.5,  -60, 0);
    private Location SPAWN = new Location(Bukkit.getWorld("Quickfire"), 1.5, -35, 0.5, -90, 90);

    public Castle(Quickfire qf) {
        super(qf);
        loadWorld(TEAM_ONE_SPAWN, TEAM_TWO_SPAWN, SPAWN);
    }

    public void resetBarriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        // TODO make this not dumb as shi
        for (int y = -60; y <=-59; y++) {
            // second place team
            world.getBlockAt(-18, y, -4).setType(m);
            world.getBlockAt(-19, y, -4).setType(m);
            world.getBlockAt(-20, y, -4).setType(m);

            world.getBlockAt(-21, y, -3).setType(m);

            world.getBlockAt(-21, y, 3).setType(m);
            world.getBlockAt(-21, y, -2).setType(m);
            world.getBlockAt(21, y, -2).setType(m);
            world.getBlockAt(21, y, -3).setType(m);

            world.getBlockAt(-17, y, -3).setType(m);

            world.getBlockAt(-16, y, -2).setType(m);
            world.getBlockAt(-22, y, -2).setType(m);

            world.getBlockAt(-23, y, -1).setType(m);
            world.getBlockAt(-23, y, 0).setType(m);
            world.getBlockAt(-23, y, 1).setType(m);

            world.getBlockAt(-15, y, -1).setType(m);
            world.getBlockAt(-15, y, 0).setType(m);
            world.getBlockAt(-15, y, 1).setType(m);

            world.getBlockAt(-22, y, 2).setType(m);
            world.getBlockAt(-16, y, 2).setType(m);

            world.getBlockAt(-17, y, 3).setType(m);
            //world.getBlockAt(-21, y, -2).setType(m);

            world.getBlockAt(-18, y, 4).setType(m);
            world.getBlockAt(-19, y, 4).setType(m);
            world.getBlockAt(-20, y, 4).setType(m);

            // first place team
            world.getBlockAt(18, y, -4).setType(m);
            world.getBlockAt(19, y, -4).setType(m);
            world.getBlockAt(20, y, -4).setType(m);

            world.getBlockAt(21, y, 3).setType(m);
            world.getBlockAt(17, y, -3).setType(m);

            world.getBlockAt(16, y, -2).setType(m);
            world.getBlockAt(22, y, -2).setType(m);

            world.getBlockAt(23, y, -1).setType(m);
            world.getBlockAt(23, y, 0).setType(m);
            world.getBlockAt(23, y, 1).setType(m);

            world.getBlockAt(15, y, -1).setType(m);
            world.getBlockAt(15, y, 0).setType(m);
            world.getBlockAt(15, y, 1).setType(m);

            world.getBlockAt(22, y, 2).setType(m);
            world.getBlockAt(16, y, 2).setType(m);

            world.getBlockAt(17, y, 3).setType(m);
            //world.getBlockAt(21, y, -2).setType(m);

            world.getBlockAt(18, y, 4).setType(m);
            world.getBlockAt(19, y, 4).setType(m);
            world.getBlockAt(20, y, 4).setType(m);
        }
    }
}
