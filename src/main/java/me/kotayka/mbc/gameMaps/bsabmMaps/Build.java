package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class Build {
    private Block[][][] blocks = new Block[6][7][7];
    private final String NAME;
    private final World WORLD;

    public Build(Location diamondBlock) {
        WORLD = diamondBlock.getWorld();

        Block sign = WORLD.getBlockAt((int) diamondBlock.getX(), (int) diamondBlock.getY()+2, (int) (diamondBlock.getZ()-5));

        if (sign.getType().equals(Material.OAK_WALL_SIGN)) {
            NAME = ((Sign) sign.getState()).getLine(0);
        }
        else {
            Bukkit.broadcastMessage("[Debug] No build name! Sign (XYZ):" + ChatColor.LIGHT_PURPLE+"X: "+sign.getLocation().getX()+", Y: "+sign.getLocation().getY()+", Z: "+sign.getLocation().getZ());
            NAME ="Undefined";
        }

        Location startBlock = new Location(WORLD, (int) diamondBlock.getX()+3, (int) diamondBlock.getY()+1, (int) diamondBlock.getZ()+3);

        for (int y = startBlock.getBlockY(); y <= startBlock.getBlockY()+5; y++) {
            for (int x = startBlock.getBlockX(); x >= startBlock.getBlockX()-6; x--) {
                for (int z = startBlock.getBlockZ(); z >= startBlock.getBlockZ()-6; z--) {
                    blocks[y- startBlock.getBlockY()][startBlock.getBlockX()-x][startBlock.getBlockZ()-z] = WORLD.getBlockAt(x,y,z);
                }
            }
        }

        //Location endBlock = new Location(world, startBlock.getBlockX()+6, startBlock.getBlockY()+5, startBlock.getBlockZ()+6);
    }

    public void placeFirstLayer(Location midBlock) {
        placeLayer(midBlock, 0);
    }

    public void placeLayer(Location midBlock, int y) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                Block copyFrom = blocks[y][x][z];
                Block pasteTo = midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z);
                pasteTo.setType(copyFrom.getType());
                pasteTo.setBlockData(copyFrom.getBlockData());
            }
        }
    }

    public void placeCompleteBuild(Location midBlock) {
        for (int y = 0; y < 6; y++) {
            placeLayer(midBlock, y);
        }
    }

    public void setAir(Location midBlock) {
        for (int y = 1; y < 6; y++) {
            setLayerAir(midBlock, y);
        }
    }

    private void setLayerAir(Location midBlock, int y) {
       for (int x = 0; x < 7; x++) {
           for (int z = 0; z < 7; z++) {
               midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z).setType(Material.AIR);
           }
       }
    }

    /*
    public double getPercentageComplete(Location midBlock) {
        for (int y = startBlock.getBlockY(); y <= startBlock.getBlockY()+5; y++) {
            for (int x = startBlock.getBlockX(); x >= startBlock.getBlockX()-6; x--) {
                for (int z = startBlock.getBlockZ(); z >= startBlock.getBlockZ()-6; z--) {
                    blocks[y- startBlock.getBlockY()][startBlock.getBlockX()-x][startBlock.getBlockZ()-z] = WORLD.getBlockAt(x,y,z);
                }
            }
        }
    }
     */

    public boolean checkBuild(Location midBlock) {
        for (int y = 1; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    if (!midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z).getType().equals(b.getType())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tells the sender the first incorrect block found.
     * @param sender Player that executed the command.
     * @param midBlock Location of the middle of the block.
     */
    public void checkBuildCommand(Player sender, Location midBlock) {
        sender.sendMessage(ChatColor.GREEN+"[checkbuild] Searching plot...");
        for (int y = 1; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    int checkX = midBlock.getBlockX()+3-x;
                    int checkY = midBlock.getBlockY()+y;
                    int checkZ = midBlock.getBlockZ()+3-z;
                    Block check = midBlock.getWorld().getBlockAt(checkX, checkY, checkZ);
                    if (!check.getType().equals(b.getType())) {
                        sender.sendMessage(
                                String.format("%s[checkbuild] %sFound block %s which should be %s at (%d, %d, %d)",
                                        ChatColor.GREEN, ChatColor.RESET, check.getType(), b.getType(), checkX, checkY, checkZ)
                        );
                        return;
                    }
                }
            }
        }
    }

    public String getName() {
        return NAME;
    }
}