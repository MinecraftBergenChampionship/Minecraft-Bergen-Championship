package me.kotayka.mbc.gameMaps.quickfireMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Quickfire;

import org.bukkit.entity.*;
import org.bukkit.*;

public class Mansion extends QuickfireMap {
    private World world = Bukkit.getWorld("Quickfire");
    private Location TEAM_ONE_SPAWN = new Location(world, 530, 1, 500);
    private Location TEAM_TWO_SPAWN = new Location(world, 470, 1, 500);
    private int timeUntilGlowing = 90;
    private Location SPAWN = new Location(Bukkit.getWorld("Quickfire"), 500, 60, 500, -90, 90);

    public Mansion(Quickfire qf) {
        super(qf);
        loadWorld(TEAM_ONE_SPAWN, TEAM_TWO_SPAWN, SPAWN, timeUntilGlowing);
    }

    public void resetBarriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        // TODO make this not dumb as shi
        for (int y = 1; y <=4; y++) {
            // second place team
            world.getBlockAt(528, y, 496).setType(m);
            world.getBlockAt(529, y, 496).setType(m);
            world.getBlockAt(530, y, 496).setType(m);
            world.getBlockAt(531, y, 496).setType(m);
            world.getBlockAt(532, y, 496).setType(m);

            world.getBlockAt(527, y, 497).setType(m);
            world.getBlockAt(528, y, 497).setType(m);
            world.getBlockAt(532, y, 497).setType(m);
            world.getBlockAt(533, y, 497).setType(m);

            world.getBlockAt(526, y, 498).setType(m);
            world.getBlockAt(527, y, 498).setType(m);
            world.getBlockAt(533, y, 498).setType(m);
            world.getBlockAt(534, y, 498).setType(m);

            world.getBlockAt(526, y, 499).setType(m);
            world.getBlockAt(534, y, 499).setType(m);

            world.getBlockAt(526, y, 500).setType(m);
            world.getBlockAt(534, y, 500).setType(m);

            world.getBlockAt(526, y, 501).setType(m);
            world.getBlockAt(534, y, 501).setType(m);

            world.getBlockAt(526, y, 502).setType(m);
            world.getBlockAt(527, y, 502).setType(m);
            world.getBlockAt(533, y, 502).setType(m);
            world.getBlockAt(534, y, 502).setType(m);

            world.getBlockAt(527, y, 503).setType(m);
            world.getBlockAt(528, y, 503).setType(m);
            world.getBlockAt(532, y, 503).setType(m);
            world.getBlockAt(533, y, 503).setType(m);

            world.getBlockAt(528, y, 504).setType(m);
            world.getBlockAt(529, y, 504).setType(m);
            world.getBlockAt(530, y, 504).setType(m);
            world.getBlockAt(531, y, 504).setType(m);
            world.getBlockAt(532, y, 504).setType(m);

            // first place team
            world.getBlockAt(468, y, 496).setType(m);
            world.getBlockAt(469, y, 496).setType(m);
            world.getBlockAt(470, y, 496).setType(m);
            world.getBlockAt(471, y, 496).setType(m);
            world.getBlockAt(472, y, 496).setType(m);

            world.getBlockAt(467, y, 497).setType(m);
            world.getBlockAt(468, y, 497).setType(m);
            world.getBlockAt(472, y, 497).setType(m);
            world.getBlockAt(473, y, 497).setType(m);

            world.getBlockAt(466, y, 498).setType(m);
            world.getBlockAt(467, y, 498).setType(m);
            world.getBlockAt(473, y, 498).setType(m);
            world.getBlockAt(474, y, 498).setType(m);

            world.getBlockAt(466, y, 499).setType(m);
            world.getBlockAt(474, y, 499).setType(m);

            world.getBlockAt(466, y, 500).setType(m);
            world.getBlockAt(474, y, 500).setType(m);

            world.getBlockAt(466, y, 501).setType(m);
            world.getBlockAt(474, y, 501).setType(m);

            world.getBlockAt(466, y, 502).setType(m);
            world.getBlockAt(467, y, 502).setType(m);
            world.getBlockAt(473, y, 502).setType(m);
            world.getBlockAt(474, y, 502).setType(m);

            world.getBlockAt(467, y, 503).setType(m);
            world.getBlockAt(468, y, 503).setType(m);
            world.getBlockAt(472, y, 503).setType(m);
            world.getBlockAt(473, y, 503).setType(m);

            world.getBlockAt(468, y, 504).setType(m);
            world.getBlockAt(469, y, 504).setType(m);
            world.getBlockAt(470, y, 504).setType(m);
            world.getBlockAt(471, y, 504).setType(m);
            world.getBlockAt(472, y, 504).setType(m);
        }

        for (int z = 497; z <= 503; z++) {
            for (int x = 467; x <= 473; x++) {
                world.getBlockAt(x, 4, z).setType(m);
            }
            for (int x = 527; x <= 533; x++) {
                world.getBlockAt(x, 4, z).setType(m);
            }
        }
    }

    public void changeColor(MBCTeam firstPlace, MBCTeam secondPlace) {
        Material first = firstPlace.getConcrete().getType();
        Material second = secondPlace.getConcrete().getType();
    
            // spawn area floor
            for (int z = 497; z <= 503; z++) {
                for (int  x= 467 ; x <= 473; x++) {
                    if (world.getBlockAt(x, 0, z).getType().equals(Material.GRASS_BLOCK) ||
                        world.getBlockAt(x, 0, z).getType().equals(Material.PODZOL) || 
                        world.getBlockAt(x, 0, z).getType().equals(Material.DIRT_PATH) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.GRAVEL) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.COARSE_DIRT)) continue;
                    else {
                        world.getBlockAt(x, 0, z).setType(second);
                    }
                }

                for (int  x= 527 ; x <= 533; x++) {
                    if (world.getBlockAt(x, 0, z).getType().equals(Material.GRASS_BLOCK) ||
                        world.getBlockAt(x, 0, z).getType().equals(Material.PODZOL) || 
                        world.getBlockAt(x, 0, z).getType().equals(Material.DIRT_PATH) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.GRAVEL) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.COARSE_DIRT)) continue;
                    else {
                        world.getBlockAt(x, 0, z).setType(first);
                    }
                }
            }
    
            // concrete in foundation of mansion
            world.getBlockAt(514, 1, 515).setType(first);
            world.getBlockAt(515, 1, 513).setType(first);
            world.getBlockAt(515, 1, 511).setType(first);
            world.getBlockAt(514, 1, 509).setType(first);

            world.getBlockAt(512, 1, 521).setType(first);
            world.getBlockAt(510, 1, 521).setType(first);
            world.getBlockAt(505, 1, 521).setType(first);
            world.getBlockAt(503, 1, 521).setType(first);

            world.getBlockAt(502, 1, 520).setType(first);
            world.getBlockAt(502, 1, 518).setType(first);
            world.getBlockAt(502, 1, 516).setType(first);

            world.getBlockAt(502, 1, 508).setType(first);
            world.getBlockAt(502, 1, 506).setType(first);
            world.getBlockAt(502, 1, 504).setType(first);

            world.getBlockAt(512, 1, 503).setType(first);
            world.getBlockAt(510, 1, 503).setType(first);
            world.getBlockAt(505, 1, 503).setType(first);
            world.getBlockAt(503, 1, 503).setType(first);

            world.getBlockAt(486, 1, 485).setType(second);
            world.getBlockAt(485, 1, 487).setType(second);
            world.getBlockAt(485, 1, 489).setType(second);
            world.getBlockAt(486, 1, 491).setType(second);

            world.getBlockAt(488, 1, 479).setType(second);
            world.getBlockAt(490, 1, 479).setType(second);
            world.getBlockAt(495, 1, 479).setType(second);
            world.getBlockAt(497, 1, 479).setType(second);

            world.getBlockAt(498, 1, 480).setType(second);
            world.getBlockAt(498, 1, 482).setType(second);
            world.getBlockAt(498, 1, 484).setType(second);

            world.getBlockAt(498, 1, 492).setType(second);
            world.getBlockAt(498, 1, 494).setType(second);
            world.getBlockAt(498, 1, 496).setType(second);

            world.getBlockAt(488, 1, 497).setType(second);
            world.getBlockAt(490, 1, 497).setType(second);
            world.getBlockAt(495, 1, 497).setType(second);
            world.getBlockAt(497, 1, 497).setType(second);

            // glass panes in mansion windows
        Material firstGlass = firstPlace.getGlass().getType();
        Material secondGlass = secondPlace.getGlass().getType();

            world.getBlockAt(505, 5, 507).setType(firstGlass);
            world.getBlockAt(509, 4, 507).setType(firstGlass);
            world.getBlockAt(509, 5, 517).setType(firstGlass);
            world.getBlockAt(505, 4, 517).setType(firstGlass);
            world.getBlockAt(509, 10, 513).setType(firstGlass);
            world.getBlockAt(510, 10, 512).setType(firstGlass);
            world.getBlockAt(509, 10, 511).setType(firstGlass);

            world.getBlockAt(495, 5, 493).setType(secondGlass);
            world.getBlockAt(491, 4, 493).setType(secondGlass);
            world.getBlockAt(491, 5, 483).setType(secondGlass);
            world.getBlockAt(495, 4, 483).setType(secondGlass);
            world.getBlockAt(491, 10, 487).setType(secondGlass);
            world.getBlockAt(490, 10, 488).setType(secondGlass);
            world.getBlockAt(491, 10, 489).setType(secondGlass);
    }
}
