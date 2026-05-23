package me.kotayka.mbc.gameMaps.quickfireMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Quickfire;

import org.bukkit.entity.*;
import org.bukkit.*;

public class West extends QuickfireMap {
    private World world = Bukkit.getWorld("Quickfire");
    private Location TEAM_ONE_SPAWN = new Location(world, -530, 0, 500);
    private Location TEAM_TWO_SPAWN = new Location(world, -470, 0, 500);
    private int timeUntilGlowing = 90;
    private Location TEAM_ONE_INTRO = new Location(Bukkit.getWorld("Quickfire"), -535.5, 3.5, 495.5, -60, 15);
    private Location TEAM_TWO_INTRO = new Location(Bukkit.getWorld("Quickfire"), -463.5, 3.5, 504.5, 120, 15);
    
    private Location SPAWN = new Location(Bukkit.getWorld("Quickfire"), -500, 60, 500, -90, 90);

    public West(Quickfire qf) {
        super(qf);
        loadWorld(TEAM_ONE_SPAWN, TEAM_TWO_SPAWN, TEAM_ONE_INTRO, TEAM_TWO_INTRO, SPAWN, timeUntilGlowing);
        super.mapName = "West";
        super.creatorName = "bigkirbypuff_";
    }

    public void resetBarriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        // TODO make this not dumb as shi
        for (int y = 0; y <=4; y++) {
            // second place team

            for (int x = -474; x <= -466; x++) {
                for (int z = 496; z<= 504; z++) {
                    if (x == -474 || x == -466 || z == 496 || z == 504 || y == 4) world.getBlockAt(x, y, z).setType(m);
                }
            }

            world.getBlockAt(-473, y, 503).setType(m);
            world.getBlockAt(-467, y, 497).setType(m);
            world.getBlockAt(-467, y, 503).setType(m);
            world.getBlockAt(-473, y, 497).setType(m);
            

            // first place team
            for (int x = -534; x <= -526; x++) {
                for (int z = 496; z<= 504; z++) {
                    if (x == -534 || x == -526 || z == 496 || z == 504 || y == 4) world.getBlockAt(x, y, z).setType(m);
                }
            }

            world.getBlockAt(-533, y, 497).setType(m);
            world.getBlockAt(-527, y, 503).setType(m);
            world.getBlockAt(-533, y, 503).setType(m);
            world.getBlockAt(-527, y, -497).setType(m);
        }

    }

    public void changeColor(MBCTeam firstPlace, MBCTeam secondPlace) {
        Material first = firstPlace.getConcrete().getType();
        Material second = secondPlace.getConcrete().getType();
    
        // spawn area floor
        for (int z = 497; z <= 503; z++) {
            for (int  x= -473 ; x <= -467; x++) {
                if (world.getBlockAt(x, -1, z).getType().equals(Material.RED_SAND)) continue;
                else world.getBlockAt(x, -1, z).setType(second);
            }

            for (int  x= -533 ; x <= -527; x++) {
                if (world.getBlockAt(x, -1, z).getType().equals(Material.RED_SAND)) continue;
                else world.getBlockAt(x, -1, z).setType(first);
            }
        }
        Material firstGlass = firstPlace.getGlass().getType();
        Material secondGlass = secondPlace.getGlass().getType();
        Material firstWool = firstPlace.getColoredWool().getType();
        Material secondWool = secondPlace.getColoredWool().getType();
        for (int y = 7; y <= 14; y++) {
            for (int z = 491; z <= 499; z++) {
                for (int  x= -483 ; x <= -476; x++) {
                    if (world.getBlockAt(x, y, z).getType().name().contains("GLASS")) {
                        world.getBlockAt(x, y, z).setType(secondGlass);
                    }
                }
            }
            for (int z = 501; z <= 509; z++) {
                for (int  x= -524 ; x <= -517; x++) {
                    if (world.getBlockAt(x, y, z).getType().name().contains("GLASS")) {
                        world.getBlockAt(x, y, z).setType(firstGlass);
                    }
                }
            }
        }
    }
}
