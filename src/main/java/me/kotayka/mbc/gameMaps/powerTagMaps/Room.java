package me.kotayka.mbc.gameMaps.powerTagMaps;

import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Chain;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.ItemStack;

public class Room {
    private Block[][][] blocks = new Block[21][29][29];
    private final World WORLD;
    public final int diaX;
    public final int diaY;
    public final int diaZ;

    public Room(Location diamondBlock) {
        WORLD = diamondBlock.getWorld();
        diaX = (int)diamondBlock.getX();
        diaY = (int)diamondBlock.getY();
        diaZ = (int)diamondBlock.getZ();

        for (int y = diamondBlock.getBlockY()+1; y <= diamondBlock.getBlockY()+21; y++) {
            for (int x = diamondBlock.getBlockX(); x >= diamondBlock.getBlockX()-28; x--) {
                for (int z = diamondBlock.getBlockZ(); z <= diamondBlock.getBlockZ()+28; z++) {
                    blocks[y- diamondBlock.getBlockY()-1][diamondBlock.getBlockX()-x][z-diamondBlock.getBlockZ()] = WORLD.getBlockAt(x,y,z);

                }
            }
        }

    }

    public void placeFirstLayer(Location diamondBlock) {
        placeLayer(diamondBlock, 0);
    }

    public void placeLayer(Location diamondBlock, int y) {
        for (int x = 0; x < 29; x++) {
            for (int z = 0; z < 29; z++) {
                Block copyFrom = WORLD.getBlockAt(diaX-x, diaY+y+1, diaZ+z);
                //Block copyFrom = blocks[y][x][z];
                Block pasteTo = diamondBlock.getWorld().getBlockAt(diamondBlock.getBlockX()-x, diamondBlock.getBlockY()+y+1, diamondBlock.getBlockZ()+z);

                pasteTo.setType(copyFrom.getType());
                pasteTo.setBlockData(copyFrom.getBlockData());
                
                BlockState sourceState = copyFrom.getState();
                BlockState targetState = pasteTo.getState();
                targetState.setType(sourceState.getType());
                targetState.setBlockData(sourceState.getBlockData());

                if(sourceState instanceof Banner && targetState instanceof Banner) {
                    Banner sourceBanner = (Banner) sourceState;
                    Banner targetBanner = (Banner) targetState;
                    Bukkit.broadcastMessage("banner at "+ (diamondBlock.getBlockX()-x) + ", " + (diamondBlock.getBlockY()+y+1) + ", " + (diamondBlock.getBlockZ()+z));
                    targetBanner.setBaseColor(sourceBanner.getBaseColor());
                    targetBanner.setPatterns(sourceBanner.getPatterns());
                    targetBanner.setBlockData(sourceBanner.getBlockData());
                    targetBanner.update();
                } 
                else if(sourceState instanceof Skull && targetState instanceof Skull) {
                    Skull sourceSkull = (Skull) sourceState;
                    Skull targetSkull = (Skull) targetState;
                    Bukkit.broadcastMessage("skull at "+ (diamondBlock.getBlockX()-x) + ", " + (diamondBlock.getBlockY()+y+1) + ", " + (diamondBlock.getBlockZ()+z));
                    if (sourceSkull.getOwningPlayer() != null) targetSkull.setOwningPlayer(sourceSkull.getOwningPlayer());
                    targetSkull.setBlockData(sourceSkull.getBlockData());
                    targetSkull.update();
                }
                
            }
        }
    }

    public void placeCompleteBuild(Location diamondBlock) {
        for (int y = 0; y < 21; y++) {
            placeLayer(diamondBlock, y);
        }
    }

    public void setAir(Location diamondBlock) {
        for (int y = 0; y < 21; y++) {
            setLayerAir(diamondBlock, y);
        }
    }

    private void setLayerAir(Location diamondBlock, int y) {
       for (int x = 0; x < 29; x++) {
           for (int z = 0; z < 29; z++) {
                diamondBlock.getWorld().getBlockAt(diamondBlock.getBlockX()-x, diamondBlock.getBlockY()+y+1, diamondBlock.getBlockZ()+z).setType(Material.AIR);
           }
       }
    }
}