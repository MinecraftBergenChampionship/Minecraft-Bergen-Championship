package me.kotayka.mbc.gameMaps.spleefMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import me.kotayka.mbc.MBC;

public class Colosseum extends SpleefMap {
    Set<Block> firstLayerDecayingBlocks = new HashSet<>();
    Set<Block> secondLayerDecayingBlocks = new HashSet<>();
    Set<Block> thirdLayerDecayingBlocks = new HashSet<>();
    Set<Block> stonePillarDecayingBlocks = new HashSet<>();
    HashSet<Block> decayingAll = new HashSet<>();
    HashSet<Block> toRemove = new HashSet<>();
    int erosion = -1;
    int[] layerRadius = new int[]{6, 20, 20, 20};
    public Colosseum() {
        super("Colosseum", 60, "Regular", "BappleBusiness");
    }

    @Override
    public void resetMap() {
        if (erosion != -1) {
            MBC.getInstance().cancelEvent(erosion);
            erosion = -1;
        }

        decayingAll.clear();
        firstLayerDecayingBlocks.clear();
        secondLayerDecayingBlocks.clear();
        thirdLayerDecayingBlocks.clear();
        stonePillarDecayingBlocks.clear();
        toRemove.clear();
        // paste map

        int copy_from_x = -438;
        int copy_from_z = -434;
        for (int paste_to_x = -38; paste_to_x <= 37; paste_to_x++) {
            for (int paste_to_z = -34; paste_to_z <= 34; paste_to_z++) {
                int y_index = 0;
                for (int y = 66; y<= 119; y++) {
                    Block paste_from = getWorld().getBlockAt(copy_from_x, y, copy_from_z);
                    if (paste_from.getType().equals(Material.AIR) || paste_from.getType().equals(Material.GOLD_BLOCK)) continue;
                    Block paste_to = getWorld().getBlockAt(paste_to_x, y, paste_to_z);
                
                    paste_to.setType(paste_from.getType());
                    paste_to.setBlockData(paste_from.getBlockData());

                    if (copy_from_x > -408 && copy_from_x < -392 && copy_from_z > -408 && copy_from_z < -392) {
                        if (paste_from.getType().equals(Material.SMOOTH_STONE) || paste_from.getType().equals(Material.IRON_BARS) || paste_from.getType().equals(Material.CHAIN)) {
                            stonePillarDecayingBlocks.add(paste_to);
                        }
                    }

                    if (paste_from.getType().equals(Material.SANDSTONE)) firstLayerDecayingBlocks.add(paste_to);

                    if (paste_from.getType().equals(Material.DIRT) || paste_from.getType().equals(Material.COARSE_DIRT) || paste_from.getType().equals(Material.PODZOL) || paste_from.getType().equals(Material.DIRT_PATH)) {
                        if (y > 74 && y < 78) secondLayerDecayingBlocks.add(paste_to);
                        if (y > 67 && y < 71) thirdLayerDecayingBlocks.add(paste_to);
                    }
                    y_index++;
                }
                copy_from_z++;
            }
            copy_from_z = -434;
            copy_from_x++;
        }
    }

    @Override
    public void deleteMap() {
        for (int paste_to_x = -38; paste_to_x <= 37; paste_to_x++) {
            for (int paste_to_z = -34; paste_to_z <= 34; paste_to_z++) {
                for (int y = 66; y<= 119; y++) {
                    getWorld().getBlockAt(paste_to_x, y, paste_to_z).setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void Border(int timeRemaining) {
        switch (timeRemaining) {
            case 145 -> Bukkit.broadcastMessage(ChatColor.RED + "Stone Pillars are starting to erode!");
            case 110 -> Bukkit.broadcastMessage(ChatColor.RED + "First layer is starting to erode!");
            case 75 -> Bukkit.broadcastMessage(ChatColor.RED + "Second layer is starting to erode!");
            case 40 -> Bukkit.broadcastMessage(ChatColor.RED + "Last layer is starting to erode!");
        }

        if (timeRemaining <= 145 && timeRemaining % 2 == 0 && !stonePillarDecayingBlocks.isEmpty()) {
            erodeLayer(1);
        }

        if (timeRemaining <= 110 && timeRemaining % 2 == 0 && !firstLayerDecayingBlocks.isEmpty()) {
            erodeLayer(2);
        }

        if (timeRemaining <= 75 && timeRemaining % 2 == 0 && !secondLayerDecayingBlocks.isEmpty()) {
            erodeLayer(3);
        }

        if (timeRemaining <= 40 && timeRemaining % 2 == 0 && !thirdLayerDecayingBlocks.isEmpty()) {
            erodeLayer(4);
        }
    }

    private void erodeLayer(int layer) {
        switch (layer) {
            case 1 -> erodeLayer(stonePillarDecayingBlocks, 0);
            case 2 -> erodeLayer(firstLayerDecayingBlocks, 1);
            case 3 -> erodeLayer(secondLayerDecayingBlocks, 2);
            case 4 -> erodeLayer(thirdLayerDecayingBlocks, 3);
        }
    }

    private void erodeLayer(Set<Block> decay, int radiusNum) {
        int radius = layerRadius[radiusNum];
        if (erosion == -1) {
            erosion = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
                for (Block b : decayingAll) {
                    switch (b.getType()) {
                        case SANDSTONE, SMOOTH_STONE, DIRT, COARSE_DIRT, PODZOL, DIRT_PATH -> b.setType(Material.YELLOW_CONCRETE);
                        case YELLOW_CONCRETE -> b.setType(Material.ORANGE_CONCRETE);
                        case ORANGE_CONCRETE -> b.setType(Material.RED_CONCRETE);
                        case RED_CONCRETE, IRON_BARS, CHAIN -> {
                            b.setType(Material.AIR);
                            toRemove.add(b);
                        }
                        case AIR -> toRemove.add(b);
                    }
                }
                for (Block b : toRemove) {
                    decayingAll.remove(b);
                }
            }, 40, 40);
        }

        
        Set<Block> toRemove2 = new HashSet<>();
        for (Block b : decay) {
            if (!(b.getX() == radius || b.getZ() == radius || b.getX() == -1*radius || b.getZ() == radius*-1)) continue;
            decayingAll.add(b);
            toRemove2.add(b);
        }
        
        layerRadius[radiusNum]--;
        for (Block b : toRemove2) {
            decay.remove(b);
        }
    }
}
