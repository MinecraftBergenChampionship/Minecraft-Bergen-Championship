package me.kotayka.mbc.gameMaps.quickfireMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Quickfire;

import org.bukkit.entity.*;
import org.bukkit.*;

public class SnowGlobe extends QuickfireMap {
    private World world = Bukkit.getWorld("Quickfire");
    private Location TEAM_ONE_SPAWN = new Location(world, -532, 1, -500);
    private Location TEAM_TWO_SPAWN = new Location(world, -468, 1, -500);
    private int timeUntilGlowing = 90;
    private Location SPAWN = new Location(Bukkit.getWorld("Quickfire"), -500, 70, -500, -90, 90);

    public SnowGlobe(Quickfire qf) {
        super(qf);
        loadWorld(TEAM_ONE_SPAWN, TEAM_TWO_SPAWN, SPAWN, timeUntilGlowing);
    }

    public void resetBarriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        // TODO make this not dumb as shi
        for (int y = 1; y <=4; y++) {
            // second place team
            world.getBlockAt(-530, y, -496).setType(m);
            world.getBlockAt(-531, y, -496).setType(m);
            world.getBlockAt(-532, y, -496).setType(m);
            world.getBlockAt(-533, y, -496).setType(m);
            world.getBlockAt(-534, y, -496).setType(m);

            world.getBlockAt(-529, y, -497).setType(m);
            world.getBlockAt(-530, y, -497).setType(m);
            world.getBlockAt(-534, y, -497).setType(m);
            world.getBlockAt(-535, y, -497).setType(m);

            world.getBlockAt(-528, y, -498).setType(m);
            world.getBlockAt(-529, y, -498).setType(m);
            world.getBlockAt(-535, y, -498).setType(m);
            world.getBlockAt(-536, y, -498).setType(m);

            world.getBlockAt(-528, y, -499).setType(m);
            world.getBlockAt(-536, y, -499).setType(m);

            world.getBlockAt(-528, y, -500).setType(m);
            world.getBlockAt(-536, y, -500).setType(m);

            world.getBlockAt(-528, y, -501).setType(m);
            world.getBlockAt(-536, y, -501).setType(m);

            world.getBlockAt(-528, y, -502).setType(m);
            world.getBlockAt(-529, y, -502).setType(m);
            world.getBlockAt(-535, y, -502).setType(m);
            world.getBlockAt(-536, y, -502).setType(m);

            world.getBlockAt(-529, y, -503).setType(m);
            world.getBlockAt(-530, y, -503).setType(m);
            world.getBlockAt(-534, y, -503).setType(m);
            world.getBlockAt(-535, y, -503).setType(m);

            world.getBlockAt(-530, y, -504).setType(m);
            world.getBlockAt(-531, y, -504).setType(m);
            world.getBlockAt(-532, y, -504).setType(m);
            world.getBlockAt(-533, y, -504).setType(m);
            world.getBlockAt(-534, y, -504).setType(m);

            // first place team
            world.getBlockAt(-466, y, -496).setType(m);
            world.getBlockAt(-467, y, -496).setType(m);
            world.getBlockAt(-468, y, -496).setType(m);
            world.getBlockAt(-469, y, -496).setType(m);
            world.getBlockAt(-470, y, -496).setType(m);

            world.getBlockAt(-465, y, -497).setType(m);
            world.getBlockAt(-466, y, -497).setType(m);
            world.getBlockAt(-470, y, -497).setType(m);
            world.getBlockAt(-471, y, -497).setType(m);

            world.getBlockAt(-464, y, -498).setType(m);
            world.getBlockAt(-465, y, -498).setType(m);
            world.getBlockAt(-471, y, -498).setType(m);
            world.getBlockAt(-472, y, -498).setType(m);

            world.getBlockAt(-464, y, -499).setType(m);
            world.getBlockAt(-472, y, -499).setType(m);

            world.getBlockAt(-464, y, -500).setType(m);
            world.getBlockAt(-472, y, -500).setType(m);

            world.getBlockAt(-464, y, -501).setType(m);
            world.getBlockAt(-472, y, -501).setType(m);

            world.getBlockAt(-464, y, -502).setType(m);
            world.getBlockAt(-465, y, -502).setType(m);
            world.getBlockAt(-471, y, -502).setType(m);
            world.getBlockAt(-472, y, -502).setType(m);

            world.getBlockAt(-465, y, -503).setType(m);
            world.getBlockAt(-466, y, -503).setType(m);
            world.getBlockAt(-470, y, -503).setType(m);
            world.getBlockAt(-471, y, -503).setType(m);

            world.getBlockAt(-466, y, -504).setType(m);
            world.getBlockAt(-467, y, -504).setType(m);
            world.getBlockAt(-468, y, -504).setType(m);
            world.getBlockAt(-469, y, -504).setType(m);
            world.getBlockAt(-470, y, -504).setType(m);
        }

        for (int z = -503; z <= -497; z++) {
            for (int x = -471; x <= -465; x++) {
                world.getBlockAt(x, 4, z).setType(m);
            }
            for (int x = -535; x <= -529; x++) {
                world.getBlockAt(x, 4, z).setType(m);
            }
        }
    }

    public void changeColor(MBCTeam firstPlace, MBCTeam secondPlace) {
        Material first = firstPlace.getConcrete().getType();
        Material second = secondPlace.getConcrete().getType();
    
            // spawn area floor
            for (int z = -503; z <= -497; z++) {
                for (int  x= -471 ; x <= -465; x++) {
                    if (world.getBlockAt(x, 0, z).getType().equals(Material.SNOW) ||
                        world.getBlockAt(x, 0, z).getType().equals(Material.SNOW_BLOCK) || 
                        world.getBlockAt(x, 0, z).getType().equals(Material.DIRT_PATH) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.GRAVEL) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.COARSE_DIRT)) continue;
                    else {
                        world.getBlockAt(x, 0, z).setType(second);
                    }
                }

                for (int  x= -535 ; x <= -529; x++) {
                    if (world.getBlockAt(x, 0, z).getType().equals(Material.SNOW) ||
                        world.getBlockAt(x, 0, z).getType().equals(Material.SNOW_BLOCK) || 
                        world.getBlockAt(x, 0, z).getType().equals(Material.DIRT_PATH) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.GRAVEL) ||  
                        world.getBlockAt(x, 0, z).getType().equals(Material.COARSE_DIRT)) continue;
                    else {
                        world.getBlockAt(x, 0, z).setType(first);
                    }
                }
            }

            // concrete in presents
            for (int  x= -494; x <= -492; x++) {
                for (int y = 2; y <= 5; y++) {
                    for (int z = -493; z <= -491; z++) {
                        if (world.getBlockAt(x, y, z).getType().equals(Material.SMOOTH_QUARTZ)) continue;
                        else world.getBlockAt(x, y, z).setType(second);
                    }
                }
            }
            for (int  x= -493; x <= -491; x++) {
                for (int y = 2; y <= 4; y++) {
                    for (int z = -509; z <= -505; z++) {
                        if (world.getBlockAt(x, y, z).getType().equals(Material.SMOOTH_QUARTZ)) continue;
                        else world.getBlockAt(x, y, z).setType(second);
                    }
                }
            }
            for (int  x= -508; x <= -506; x++) {
                for (int y = 2; y <= 5; y++) {
                    for (int z = -509; z <= -507; z++) {
                        if (world.getBlockAt(x, y, z).getType().equals(Material.SMOOTH_QUARTZ)) continue;
                        else world.getBlockAt(x, y, z).setType(first);
                    }
                }
            }
            for (int  x= -509; x <= -507; x++) {
                for (int y = 2; y <= 4; y++) {
                    for (int z = -495; z <= -491; z++) {
                        if (world.getBlockAt(x, y, z).getType().equals(Material.SMOOTH_QUARTZ)) continue;
                        else world.getBlockAt(x, y, z).setType(first);
                    }
                }
            }

            // concrete in candy canes
            for (int y = 1; y <= 7; y++) {
                for (int z = -505; z <= -495; z++) {
                    for (int  x= -481; x <= -472; x++) {
                        if (world.getBlockAt(x, y, z).getType().equals(Material.RED_CONCRETE) ||
                        world.getBlockAt(x, y, z).getType().equals(Material.YELLOW_CONCRETE) || 
                        world.getBlockAt(x, y, z).getType().equals(Material.GREEN_CONCRETE) ||  
                        world.getBlockAt(x, y, z).getType().equals(Material.BLUE_CONCRETE) ||  
                        world.getBlockAt(x, y, z).getType().equals(Material.PURPLE_CONCRETE) ||
                        world.getBlockAt(x, y, z).getType().equals(Material.PINK_CONCRETE)) world.getBlockAt(x, y, z).setType(second);
                        else continue;
                    }
                    
                    for (int  x= -528; x <= -519; x++) {
                        if (world.getBlockAt(x, y, z).getType().equals(Material.RED_CONCRETE) ||
                        world.getBlockAt(x, y, z).getType().equals(Material.YELLOW_CONCRETE) || 
                        world.getBlockAt(x, y, z).getType().equals(Material.GREEN_CONCRETE) ||  
                        world.getBlockAt(x, y, z).getType().equals(Material.BLUE_CONCRETE) ||  
                        world.getBlockAt(x, y, z).getType().equals(Material.PURPLE_CONCRETE) ||
                        world.getBlockAt(x, y, z).getType().equals(Material.PINK_CONCRETE)) world.getBlockAt(x, y, z).setType(first);
                        else continue;
                    }
                }
            }
            
    }
}
