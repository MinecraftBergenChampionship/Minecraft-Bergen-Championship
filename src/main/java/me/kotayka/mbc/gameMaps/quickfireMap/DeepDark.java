package me.kotayka.mbc.gameMaps.quickfireMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Quickfire;

import org.bukkit.entity.*;
import org.bukkit.*;

public class DeepDark extends QuickfireMap {
    private World world = Bukkit.getWorld("Quickfire");
    private Location TEAM_ONE_SPAWN = new Location(world, 536, 1, -500);
    private Location TEAM_TWO_SPAWN = new Location(world, 464, 1, -500);
    private int timeUntilGlowing = 90;
    private Location SPAWN = new Location(Bukkit.getWorld("Quickfire"), 500, 55, -500, -90, 90);

    public DeepDark(Quickfire qf) {
        super(qf);
        loadWorld(TEAM_ONE_SPAWN, TEAM_TWO_SPAWN, SPAWN, timeUntilGlowing);
        super.mapName = "Deep Dark";
        super.creatorName = "bigkirbypuff_";
    }

    public void resetBarriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        // TODO make this not dumb as shi
        for (int y = 1; y <=4; y++) {
            // second place team

            for (int x = 533; x <= 539; x++) {
                for (int z = -503; z<= -497; z++) {
                    if (x == 533 || x == 539 || z == -503 || z == -497 || y == 4) world.getBlockAt(x, y, z).setType(m);
                }
            }

            world.getBlockAt(534, y, -498).setType(m);
            world.getBlockAt(538, y, -498).setType(m);
            world.getBlockAt(534, y, -502).setType(m);
            world.getBlockAt(538, y, -502).setType(m);
            

            // first place team
            for (int x = 461; x <= 467; x++) {
                for (int z = -503; z<= -497; z++) {
                    if (x == 461 || x == 467 || z == -503 || z == -497 || y == 4) world.getBlockAt(x, y, z).setType(m);
                }
            }

            world.getBlockAt(466, y, -498).setType(m);
            world.getBlockAt(462, y, -498).setType(m);
            world.getBlockAt(466, y, -502).setType(m);
            world.getBlockAt(462, y, -502).setType(m);
        }

    }

    public void changeColor(MBCTeam firstPlace, MBCTeam secondPlace) {
        Material first = firstPlace.getConcrete().getType();
        Material second = secondPlace.getConcrete().getType();
    
        // spawn area floor
        for (int z = -502; z <= -498; z++) {
            for (int  x= 462 ; x <= 466; x++) {
                if (world.getBlockAt(x, 0, z).getType().equals(Material.DEEPSLATE_TILES)) continue;
                else world.getBlockAt(x, 0, z).setType(second);
            }

            for (int  x= 534 ; x <= 538; x++) {
                if (world.getBlockAt(x, 0, z).getType().equals(Material.DEEPSLATE_TILES)) continue;
                else world.getBlockAt(x, 0, z).setType(first);
            }
        }

        // concrete in city
        for (int y = 2; y <= 4; y++) {
            world.getBlockAt(485, y, -497).setType(second);
            world.getBlockAt(485, y, -503).setType(second);

            world.getBlockAt(515, y, -497).setType(first);
            world.getBlockAt(515, y, -503).setType(first);
        }

        world.getBlockAt(485, 10, -496).setType(second);
        world.getBlockAt(485, 10, -504).setType(second);

        world.getBlockAt(492, 4, -505).setType(second);
        world.getBlockAt(494, 4, -505).setType(second);
        world.getBlockAt(492, 4, -495).setType(second);
        world.getBlockAt(494, 4, -495).setType(second);

        world.getBlockAt(515, 10, -496).setType(first);
        world.getBlockAt(515, 10, -504).setType(first);

        world.getBlockAt(508, 4, -505).setType(first);
        world.getBlockAt(506, 4, -505).setType(first);
        world.getBlockAt(508, 4, -495).setType(first);
        world.getBlockAt(506, 4, -495).setType(first);

        // glass
        Material firstGlass = firstPlace.getGlass().getType();
        Material secondGlass = secondPlace.getGlass().getType();
        for (int z = -516; z <= -484; z++) {
            if (z < -498 && z > -502) continue;
            else {
                world.getBlockAt(498, 9, z).setType(secondGlass);
                world.getBlockAt(502, 9, z).setType(firstGlass);
            }
        }
        world.getBlockAt(499, 9, -516).setType(secondGlass);
        world.getBlockAt(499, 9, -502).setType(secondGlass);
        world.getBlockAt(499, 9, -498).setType(secondGlass);
        world.getBlockAt(499, 9, -484).setType(secondGlass);
        
        world.getBlockAt(501, 9, -516).setType(firstGlass);
        world.getBlockAt(501, 9, -502).setType(firstGlass);
        world.getBlockAt(501, 9, -498).setType(firstGlass);
        world.getBlockAt(501, 9, -484).setType(firstGlass);
        
        // wool

        Material firstWool = firstPlace.getColoredWool().getType();
        Material secondWool = secondPlace.getColoredWool().getType();

        for (int z = -501; z <= -499; z++) {
            for (int  x= 484 ; x <= 499; x++) {
                world.getBlockAt(x, 0, z).setType(secondWool);
            }

            for (int  x= 501 ; x <= 516; x++) {
                world.getBlockAt(x, 0, z).setType(firstWool);
            }
        }   
    }
}
