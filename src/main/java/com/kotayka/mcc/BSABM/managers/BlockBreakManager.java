package com.kotayka.mcc.BSABM.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class BlockBreakManager {
    //Island Coords

    //Nether (Warped Wart Block, Blackstone, Nether Bricks, Nether Wart Blocks) - 11x11x7 grid
    int[][] netherIslands = {{20, 73, 69}, {20, 73, 85}, {-2, 73, 85}, {-2, 73, 69}};

    //Bricks (Stone Bricks,Bricks, Nether Bricks, Prismarine Bricks, Mossy Stone Bricks) - 11x11x7 grid
    int[][] brickIslands = {{-38, 73, 71}, {-50, 73, 71}, {-62, 73, 71}, {-62, 73, 59}, {-62, 73, 47}};

    //Ores (Emerald, Gold, Diamond, Quartz, Coal, Redstone, Iron) - 7x7x7 grid
    int[][] oreIslands = {{78, 81, 38}, {78, 81, 46}, {78, 81, 54}, {66, 81, 53}, {67, 81, 65}, {59, 81, 65}, {51, 81, 65}};

    //Sands (Red Sand, Sand, Red Sandstone, Sandstone) - 11x11x7 grid
    int[][] sandIslands = {{-86, 74, -36},{-98, 74,-36},{-98, 85,-46},{-74,85,-46}};

    //Stones (Granite, Stone, Diorite, Andestie) - 11x11x7 grid
    int[][] stoneIslands = {{-30,74,-74},{-30,74,-90},{-8,74,-74},{-8,74,-90}};

    //Logs (Oak, Birch, Spruce, Acacia, Jungle) - 9x9x20
    int[][] logsIsland = {{-71,89,32},{-83,89,32},{-97,89,18},{-83,89,4},{-71,89,4}};

    //Flowers (Lily of teh Valley, Allium, Cornflower, Orange Tulip, Dandelion, Pink Tulip, Red Tulip, Oxeye Daisy, Blue Orchid, White Tulip)
    int[][] flowerIslands = {{71,75,-18},{76,75,-18},{81,75,-18},{86,75,-18},{91,75,-18},{71,75,-6},{76,75,-6},{81,75,-6},{86,75,-6},{91,75,-6}};

    //Corals (Horn, Fire, Bubble, Brain, Tube)
    int[][] coralsIslands = {{33,75,-55},{38,75,-55},{43,75,-55},{48,75,-55},{53,75,-55}};

    //Fans (Horn, Fire, Bubble, Brain, Tube)
    int[][] fanIslands = {{33,75,-67},{38,75,-67},{43,75,-67},{48,75,-67},{53,75,-67}};

    // Concrete Order (Black, Light Blue, Blue, Purple, Pink, White, Red, Orange, Yellow, Lime)
    int[][] concreteIslands = {{-22,27,-85},{40, 27, -80},{77,27,-18},{77,27,10},{40,27,72},{14,27,77},{-48,27,72},{-85,27,10},{-85,27,-18},{-48,27,-80}};

    int[][] glassIslands = {{14,27,-85},{58, 27, -62},{59,27,-36},{59,27,28},{58,27,54},{-22,27,77},{-66,27,54},{-67,27,28},{-67,27,-36},{-66,27,-62}};

    public boolean checkMaterial(Material x, Location loc) {
        switch (x) {
            case WARPED_WART_BLOCK:
            case BLACKSTONE:
            case NETHER_BRICKS:
            case NETHER_WART_BLOCK:
                if (Nether(x, loc)) {
                    return true;
                }
                break;
            case STONE_BRICKS:
            case BRICKS:
            case CHISELED_STONE_BRICKS:
            case PRISMARINE_BRICKS:
            case MOSSY_STONE_BRICKS:
                if (Bricks(x, loc)) {
                    return true;
                }
                break;
            case OAK_LOG:
            case BIRCH_LOG:
            case SPRUCE_LOG:
            case ACACIA_LOG:
            case JUNGLE_LOG:
                if (Logs(x, loc)) {
                    return true;
                }
            case RED_SAND:
            case SAND:
            case RED_SANDSTONE:
            case SANDSTONE:
                if (Sands(x, loc)) {
                    return true;
                }
                break;
            case GRANITE:
            case STONE:
            case DIORITE:
            case ANDESITE:
                if (Stone(x, loc)) {
                    return true;
                }
                break;
            case EMERALD_BLOCK:
            case GOLD_BLOCK:
            case DIAMOND_BLOCK:
            case QUARTZ_BLOCK:
            case COAL_BLOCK:
            case REDSTONE_BLOCK:
            case IRON_BLOCK:
                if (Ores(x, loc)) {
                    return true;
                }
                break;
        }
        if (x.toString().endsWith("GLASS") || x.toString().endsWith("CONCRETE")) {
            if (ConcreteAndGlass(x, loc)) {
                return true;
            }
        }
        return false;
    }

    public boolean Nether(Material x, Location loc) {
        switch(x) {
            case WARPED_WART_BLOCK:
                if (netherIslands[0][0] <= loc.getX() && netherIslands[0][0]+10 >= loc.getX()) {
                    if (netherIslands[0][1]-8 <= loc.getY() && netherIslands[0][1] >= loc.getY()) {
                        if (netherIslands[0][2] <= loc.getZ() && netherIslands[0][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case BLACKSTONE:
                if (netherIslands[1][0] <= loc.getX() && netherIslands[1][0]+10 >= loc.getX()) {
                    if (netherIslands[1][1]-8 <= loc.getY() && netherIslands[1][1] >= loc.getY()) {
                        if (netherIslands[1][2] <= loc.getZ() && netherIslands[1][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case NETHER_BRICKS:
                if (netherIslands[2][0] <= loc.getX() && netherIslands[2][0]+10 >= loc.getX()) {
                    if (netherIslands[2][1]-8 <= loc.getY() && netherIslands[2][1] >= loc.getY()) {
                        if (netherIslands[2][2] <= loc.getZ() && netherIslands[2][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case NETHER_WART_BLOCK:
                if (netherIslands[3][0] <= loc.getX() && netherIslands[3][0]+10 >= loc.getX()) {
                    if (netherIslands[3][1]-8 <= loc.getY() && netherIslands[3][1] >= loc.getY()) {
                        if (netherIslands[3][2] <= loc.getZ() && netherIslands[3][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    public boolean Bricks(Material x, Location loc) {
        switch(x) {
            case STONE_BRICKS:
                if (brickIslands[0][0] <= loc.getX() && brickIslands[0][0]+10 >= loc.getX()) {
                    if (brickIslands[0][1]-8 <= loc.getY() && brickIslands[0][1] >= loc.getY()) {
                        if (brickIslands[0][2] <= loc.getZ() && brickIslands[0][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case BRICKS:
                if (brickIslands[1][0] <= loc.getX() && brickIslands[1][0]+10 >= loc.getX()) {
                    if (brickIslands[1][1]-8 <= loc.getY() && brickIslands[1][1] >= loc.getY()) {
                        if (brickIslands[1][2] <= loc.getZ() && brickIslands[1][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case CHISELED_STONE_BRICKS:
                if (brickIslands[2][0] <= loc.getX() && brickIslands[2][0]+10 >= loc.getX()) {
                    if (brickIslands[2][1]-8 <= loc.getY() && brickIslands[2][1] >= loc.getY()) {
                        if (brickIslands[2][2] <= loc.getZ() && brickIslands[2][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case PRISMARINE_BRICKS:
                if (brickIslands[3][0] <= loc.getX() && brickIslands[3][0]+10 >= loc.getX()) {
                    if (brickIslands[3][1]-8 <= loc.getY() && brickIslands[3][1] >= loc.getY()) {
                        if (brickIslands[3][2] <= loc.getZ() && brickIslands[3][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case MOSSY_STONE_BRICKS:
                if (brickIslands[4][0] <= loc.getX() && brickIslands[4][0]+10 >= loc.getX()) {
                    if (brickIslands[4][1]-8 <= loc.getY() && brickIslands[4][1] >= loc.getY()) {
                        if (brickIslands[4][2] <= loc.getZ() && brickIslands[4][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    public boolean Logs(Material x, Location loc) {
        switch(x) {
            case OAK_LOG:
                if (logsIsland[0][0] <= loc.getX() && logsIsland[0][0]+8 >= loc.getX()) {
                    if (logsIsland[0][1]-20 <= loc.getY() && logsIsland[0][1] >= loc.getY()) {
                        if (logsIsland[0][2] <= loc.getZ() && logsIsland[0][2]+8 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case BIRCH_LOG:
                if (logsIsland[1][0] <= loc.getX() && logsIsland[1][0]+8 >= loc.getX()) {
                    if (logsIsland[1][1]-20 <= loc.getY() && logsIsland[1][1] >= loc.getY()) {
                        if (logsIsland[1][2] <= loc.getZ() && logsIsland[1][2]+8 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case SPRUCE_LOG:
                if (logsIsland[2][0] <= loc.getX() && logsIsland[2][0]+8 >= loc.getX()) {
                    if (logsIsland[2][1]-20 <= loc.getY() && logsIsland[2][1] >= loc.getY()) {
                        if (logsIsland[2][2] <= loc.getZ() && logsIsland[2][2]+8 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case ACACIA_LOG:
                if (logsIsland[3][0] <= loc.getX() && logsIsland[3][0]+8 >= loc.getX()) {
                    if (logsIsland[3][1]-20 <= loc.getY() && logsIsland[3][1] >= loc.getY()) {
                        if (logsIsland[3][2] <= loc.getZ() && logsIsland[3][2]+8 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case JUNGLE_LOG:
                if (logsIsland[4][0] <= loc.getX() && logsIsland[4][0]+8 >= loc.getX()) {
                    if (logsIsland[4][1]-20 <= loc.getY() && logsIsland[4][1] >= loc.getY()) {
                        if (logsIsland[4][2] <= loc.getZ() && logsIsland[4][2]+8 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    public boolean Sands(Material x, Location loc) {
        switch(x) {
            case RED_SAND:
                if (sandIslands[0][0] <= loc.getX() && sandIslands[0][0]+10 >= loc.getX()) {
                    if (sandIslands[0][1]-8 <= loc.getY() && sandIslands[0][1] >= loc.getY()) {
                        if (sandIslands[0][2] <= loc.getZ() && sandIslands[0][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case SAND:
                if (sandIslands[1][0] <= loc.getX() && sandIslands[1][0]+10 >= loc.getX()) {
                    if (sandIslands[1][1]-8 <= loc.getY() && sandIslands[1][1] >= loc.getY()) {
                        if (sandIslands[1][2] <= loc.getZ() && sandIslands[1][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case RED_SANDSTONE:
                if (sandIslands[2][0] <= loc.getX() && sandIslands[2][0]+10 >= loc.getX()) {
                    if (sandIslands[2][1]-12 <= loc.getY() && sandIslands[2][1] >= loc.getY()) {
                        if (sandIslands[2][2] <= loc.getZ() && sandIslands[2][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case SANDSTONE:
                if (sandIslands[3][0] <= loc.getX() && sandIslands[3][0]+10 >= loc.getX()) {
                    if (sandIslands[3][1]-12 <= loc.getY() && sandIslands[3][1] >= loc.getY()) {
                        if (sandIslands[3][2] <= loc.getZ() && sandIslands[3][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    public boolean Stone(Material x, Location loc) {
        switch(x) {
            case GRANITE:
                if (stoneIslands[0][0] <= loc.getX() && stoneIslands[0][0]+10 >= loc.getX()) {
                    if (stoneIslands[0][1]-8 <= loc.getY() && stoneIslands[0][1] >= loc.getY()) {
                        if (stoneIslands[0][2] <= loc.getZ() && stoneIslands[0][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case STONE:
                if (stoneIslands[1][0] <= loc.getX() && stoneIslands[1][0]+10 >= loc.getX()) {
                    if (stoneIslands[1][1]-8 <= loc.getY() && stoneIslands[1][1] >= loc.getY()) {
                        if (stoneIslands[1][2] <= loc.getZ() && stoneIslands[1][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case DIORITE:
                if (stoneIslands[2][0] <= loc.getX() && stoneIslands[2][0]+10 >= loc.getX()) {
                    if (stoneIslands[2][1]-8 <= loc.getY() && stoneIslands[2][1] >= loc.getY()) {
                        if (stoneIslands[2][2] <= loc.getZ() && stoneIslands[2][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case ANDESITE:
                if (stoneIslands[3][0] <= loc.getX() && stoneIslands[3][0]+10 >= loc.getX()) {
                    if (stoneIslands[3][1]-8 <= loc.getY() && stoneIslands[3][1] >= loc.getY()) {
                        if (stoneIslands[3][2] <= loc.getZ() && stoneIslands[3][2]+10 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    public boolean Ores(Material x, Location loc) {
        switch(x) {
            case EMERALD_BLOCK:
                if (oreIslands[0][0] <= loc.getX() && oreIslands[0][0]+6 >= loc.getX()) {
                    if (oreIslands[0][1]-10 <= loc.getY() && oreIslands[0][1] >= loc.getY()) {
                        if (oreIslands[0][2] <= loc.getZ() && oreIslands[0][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case GOLD_BLOCK:
                if (oreIslands[1][0] <= loc.getX() && oreIslands[1][0]+6 >= loc.getX()) {
                    if (oreIslands[1][1]-10 <= loc.getY() && oreIslands[1][1] >= loc.getY()) {
                        if (oreIslands[1][2] <= loc.getZ() && oreIslands[1][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case DIAMOND_BLOCK:
                if (oreIslands[2][0] <= loc.getX() && oreIslands[2][0]+6 >= loc.getX()) {
                    if (oreIslands[2][1]-10 <= loc.getY() && oreIslands[2][1] >= loc.getY()) {
                        if (oreIslands[2][2] <= loc.getZ() && oreIslands[2][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case QUARTZ_BLOCK:
                if (oreIslands[3][0] <= loc.getX() && oreIslands[3][0]+6 >= loc.getX()) {
                    if (oreIslands[3][1]-10 <= loc.getY() && oreIslands[3][1] >= loc.getY()) {
                        if (oreIslands[3][2] <= loc.getZ() && oreIslands[3][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case COAL_BLOCK:
                if (oreIslands[4][0] <= loc.getX() && oreIslands[4][0]+6 >= loc.getX()) {
                    if (oreIslands[4][1]-10 <= loc.getY() && oreIslands[4][1] >= loc.getY()) {
                        if (oreIslands[4][2] <= loc.getZ() && oreIslands[4][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case REDSTONE_BLOCK:
                if (oreIslands[5][0] <= loc.getX() && oreIslands[5][0]+6 >= loc.getX()) {
                    if (oreIslands[5][1]-10 <= loc.getY() && oreIslands[5][1] >= loc.getY()) {
                        if (oreIslands[5][2] <= loc.getZ() && oreIslands[5][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
            case IRON_BLOCK:
                if (oreIslands[6][0] <= loc.getX() && oreIslands[6][0]+6 >= loc.getX()) {
                    if (oreIslands[6][1]-10 <= loc.getY() && oreIslands[6][1] >= loc.getY()) {
                        if (oreIslands[6][2] <= loc.getZ() && oreIslands[6][2]+6 >= loc.getZ()) {
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    public Boolean ConcreteAndGlass(Material x, Location loc) {
        List<String> orderOfColors  = new ArrayList<>(Arrays.asList("BLA", "LIG", "BLU", "PUR", "PIN", "WHI", "RED", "ORA", "YEL", "LIM"));

        int index = orderOfColors.indexOf(x.toString().substring(0, 3));
        if (x.toString().endsWith("GLASS")) {
            if (glassIslands[index][0] <= loc.getX() && glassIslands[index][0] + 8 >= loc.getX()) {
                if (glassIslands[index][1] - 20 <= loc.getY() && glassIslands[index][1] >= loc.getY()) {
                    if (glassIslands[index][2] <= loc.getZ() && glassIslands[index][2] + 8 >= loc.getZ()) {
                        return true;
                    }
                }
            }
        } else {
            if (concreteIslands[index][0] <= loc.getX() && concreteIslands[index][0] + 8 >= loc.getX()) {
                if (concreteIslands[index][1] - 20 <= loc.getY() && concreteIslands[index][1] >= loc.getY()) {
                    if (concreteIslands[index][2] <= loc.getZ() && concreteIslands[index][2] + 8 >= loc.getZ()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
