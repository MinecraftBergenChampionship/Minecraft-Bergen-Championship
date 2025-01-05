package me.kotayka.mbc.gameMaps.spleefMap;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class Classic extends SpleefMap {
    Set<Block> firstLayerDecayingBlocks = new HashSet<>();
    Set<Block> secondLayerDecayingBlocks = new HashSet<>();
    Set<Block> thirdLayerDecayingBlocks = new HashSet<>();
    Set<Block> fourthLayerDecayingBlocks = new HashSet<>();
    HashSet<Block> decayingAll = new HashSet<>();
    HashSet<Block> toRemove = new HashSet<>();
    int erosion = -1;
    int[] layerRadius = new int[]{20, 20, 20, 25};

    public Classic() {
        super("Classic", 60, "Regular");
    }

    @Override
    public void resetMap() {
        if (erosion != -1) {
            MBC.getInstance().cancelEvent(erosion);
            erosion = -1;
        }

        layerRadius[0] = layerRadius[1] = layerRadius[2] = 20;
        layerRadius[3] = 25;

        decayingAll.clear();
        firstLayerDecayingBlocks.clear();
        secondLayerDecayingBlocks.clear();
        thirdLayerDecayingBlocks.clear();
        fourthLayerDecayingBlocks.clear();
        toRemove.clear();

        int copy_from_x = 180;
        int copy_from_z = 180;

        // first 3 layers
        for (int paste_to_x = -20; paste_to_x <= 20; paste_to_x++) {
            for (int paste_to_z = -20; paste_to_z <= 20; paste_to_z++) {
                Block paste_1 = getWorld().getBlockAt(copy_from_x, 100, copy_from_z);
                if (!(paste_1.getType().equals(Material.AIR))) { // first three layers are identical
                    Block one = getWorld().getBlockAt(paste_to_x, 100, paste_to_z);
                    Block two = getWorld().getBlockAt(paste_to_x, 88, paste_to_z);
                    Block three = getWorld().getBlockAt(paste_to_x, 76, paste_to_z);

                    one.setType(Material.WHITE_CONCRETE);
                    two.setType(Material.LIGHT_GRAY_CONCRETE);
                    three.setType(Material.WHITE_CONCRETE);

                    firstLayerDecayingBlocks.add(one);
                    secondLayerDecayingBlocks.add(two);
                    thirdLayerDecayingBlocks.add(three);
                }
                copy_from_z++;
            }
            copy_from_x++;
            copy_from_z = 180;
        }

        // last layer
        copy_from_x = 175;
        copy_from_z = 175;
        for (int paste_to_x = -25; paste_to_x <= 25; paste_to_x++) {
            for (int paste_to_z = -25; paste_to_z <= 25; paste_to_z++) {
                Block b = getWorld().getBlockAt(copy_from_x, 64, copy_from_z);
                if (!(b.getType().equals(Material.AIR))) {
                    Block paste_to = getWorld().getBlockAt(paste_to_x, 64, paste_to_z);
                    paste_to.setType(Material.LIGHT_GRAY_CONCRETE);
                    fourthLayerDecayingBlocks.add(paste_to);
                }
                copy_from_z++;
            }
            copy_from_x++;
            copy_from_z = 175;
        }
    }

    @Override
    public void deleteMap() {
        for (int paste_to_x = -20; paste_to_x <= 20; paste_to_x++) {
            for (int paste_to_z = -20; paste_to_z <= 20; paste_to_z++) {
                getWorld().getBlockAt(paste_to_x, 100, paste_to_z).setType(Material.AIR);
                getWorld().getBlockAt(paste_to_x, 88, paste_to_z).setType(Material.AIR);
                getWorld().getBlockAt(paste_to_x, 76, paste_to_z).setType(Material.AIR);
            }
        }
        for (int paste_to_x = -25; paste_to_x <= 25; paste_to_x++) {
            for (int paste_to_z = -25; paste_to_z <= 25; paste_to_z++) {
                getWorld().getBlockAt(paste_to_x, 64, paste_to_z).setType(Material.AIR);
            }
        }
    }

    @Override
    public void Border(int timeRemaining) {
        switch (timeRemaining) {
            case 150 -> Bukkit.broadcastMessage(ChatColor.RED + "First layer is starting to erode!");
            case 120 -> Bukkit.broadcastMessage(ChatColor.RED + "Second layer is starting to erode!");
            case 90 -> Bukkit.broadcastMessage(ChatColor.RED + "Third layer is starting to erode!");
            case 60 -> Bukkit.broadcastMessage(ChatColor.RED + "Last layer is starting to erode!");
        }

        if (timeRemaining <= 150 && timeRemaining % 2 == 0 && !firstLayerDecayingBlocks.isEmpty()) {
            erodeLayer(1);
        }

        if (timeRemaining <= 120 && timeRemaining % 2 == 0 && !secondLayerDecayingBlocks.isEmpty()) {
            erodeLayer(2);
        }

        if (timeRemaining <= 90 && timeRemaining % 2 == 0 && !thirdLayerDecayingBlocks.isEmpty()) {
            erodeLayer(3);
        }

        if (timeRemaining <= 60 && timeRemaining % 2 == 0 && !fourthLayerDecayingBlocks.isEmpty()) {
            erodeLayer(4);
        }
    }

    private void erodeLayer(int layer) {
        switch (layer) {
            case 1 -> erodeLayer(firstLayerDecayingBlocks, 0);
            case 2 -> erodeLayer(secondLayerDecayingBlocks, 1);
            case 3 -> erodeLayer(thirdLayerDecayingBlocks, 2);
            case 4 -> erodeLayer(fourthLayerDecayingBlocks, 3);
        }
    }

    private void erodeLayer(Set<Block> decay, int radiusNum){
        int radius = layerRadius[radiusNum];
        if (erosion == -1) {
            erosion = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
                for (Block b : decayingAll) {
                    switch (b.getType()) {
                        case LIGHT_GRAY_CONCRETE, WHITE_CONCRETE -> b.setType(Material.YELLOW_CONCRETE);
                        case YELLOW_CONCRETE -> b.setType(Material.ORANGE_CONCRETE);
                        case ORANGE_CONCRETE -> b.setType(Material.RED_CONCRETE);
                        case RED_CONCRETE -> {
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

    /*
    private void erodeLayerRandom(List<Block> decaying) {
        if (erosion == -1) {
            int rand = (int) (Math.random() * decaying.size());
            decayingAll.add(decaying.get(rand));
            erosion = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
                for (Block b : decayingAll) {
                    switch (b.getType()) {
                        case LIGHT_GRAY_CONCRETE, WHITE_CONCRETE -> b.setType(Material.YELLOW_CONCRETE);
                        case YELLOW_CONCRETE -> b.setType(Material.ORANGE_CONCRETE);
                        case ORANGE_CONCRETE -> b.setType(Material.RED_CONCRETE);
                        case RED_CONCRETE -> {
                            b.setType(Material.AIR);
                            toRemove.add(b);
                        }
                        case AIR -> toRemove.add(b);
                    }
                }
                for (Block b : toRemove) {
                    decayingAll.remove(b);
                }
            }, 20, 20);
        } else {
            int rand = (int) (Math.random() * decaying.size());
            Block b = decaying.get(rand);
            decaying.remove(b);
            decayingAll.add(b);
            int rand2 = (int) (Math.random()*decaying.size());
            Block b2 = decaying.get(rand2);
            decaying.remove(b2);
            decayingAll.add(b2);
            int rand3 = (int) (Math.random()*decaying.size());
            Block b3 = decaying.get(rand3);
            decaying.remove(b3);
            decayingAll.add(b3);
        }
    }
     */
}