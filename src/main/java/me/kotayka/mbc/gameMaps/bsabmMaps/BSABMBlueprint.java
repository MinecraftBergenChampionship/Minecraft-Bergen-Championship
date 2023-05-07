package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class BSABMBlueprint {

    private Block[][][] blocks = new Block[6][7][7];
    private final String name;
    private final World world;

    public BSABMBlueprint(Location diamondBlock) {
        world = diamondBlock.getWorld();

        Block sign = world.getBlockAt((int) diamondBlock.getX(), (int) diamondBlock.getY()+2, (int) (diamondBlock.getZ()-5));

        if (sign.getType().equals(Material.OAK_WALL_SIGN)) {
            name = ((Sign) sign.getState()).getLine(0);
        }
        else {
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE+"X: "+sign.getLocation().getX()+", Y: "+sign.getLocation().getY()+", Z: "+sign.getLocation().getZ());
            name="Undefined";
        }

        Location startBlock = new Location(world, (int) diamondBlock.getX()+3, (int) diamondBlock.getY()+1, (int) diamondBlock.getZ()+3);

        for (int y = startBlock.getBlockY(); y <= startBlock.getBlockY()+5; y++) {
            for (int x = startBlock.getBlockX(); x <= startBlock.getBlockX()-6; x--) {
                for (int z = startBlock.getBlockZ(); z <= startBlock.getBlockZ()-6; z--) {
                    blocks[y- startBlock.getBlockY()][startBlock.getBlockX()-x][startBlock.getBlockZ()-z] = world.getBlockAt(x,y,z);
                }
            }
        }

        Location endBlock = new Location(world, startBlock.getBlockX()+6, startBlock.getBlockY()+5, startBlock.getBlockZ()+6);
    }

    public void placeFirstLayer(Location midBlock) {
        placeLayer(midBlock, 0);
    }

    public void placeLayer(Location midBlock, int y) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                Block b = blocks[y][x][z];
                midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-x).setType(b.getType());
            }
        }
    }

    public void placeCompleteBuild(Location midBlock) {
        for (int y = 0; y < 6; y++) {
            placeLayer(midBlock, y);
        }
    }

    public boolean checkBuild(Location midBlock) {
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    if (!midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-x).getType().equals(b.getType())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String getName() {
        return name;
    }
}