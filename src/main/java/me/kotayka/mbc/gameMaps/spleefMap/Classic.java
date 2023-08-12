package me.kotayka.mbc.gameMaps.spleefMap;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Classic extends SpleefMap {
    List<Block> firstLayerDecayingBlocks = new ArrayList<>();
    List<Block> secondLayerDecayingBlocks = new ArrayList<>();
    List<Block> thirdLayerDecayingBlocks = new ArrayList<>();
    List<Block> fourthLayerDecayingBlocks = new ArrayList<>();

    public Classic() {
        super("Classic", 60);
    }

    @Override
    public void resetMap() {
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
        for (int paste_to_x = -24; paste_to_x <= 25; paste_to_x++) {
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
    public void Border(int timeRemaining) {
        switch (timeRemaining) {
            case 180 -> Bukkit.broadcastMessage(ChatColor.RED + "First layer is starting to erode!");
            case 150 -> Bukkit.broadcastMessage(ChatColor.RED + "Second layer is starting to erode!");
            case 90 -> Bukkit.broadcastMessage(ChatColor.RED + "Third layer is starting to erode!");
            case 60 -> Bukkit.broadcastMessage(ChatColor.RED + "Last layer is starting to erode!");
        }

        if (timeRemaining <= 180 && !firstLayerDecayingBlocks.isEmpty()) {
            erodeLayer(1);
        }

        if (timeRemaining <= 150 && !secondLayerDecayingBlocks.isEmpty()) {
            erodeLayer(2);
        }

        if (timeRemaining <= 90 && !thirdLayerDecayingBlocks.isEmpty()) {
            erodeLayer(3);
        }

        if (timeRemaining <= 60 && !fourthLayerDecayingBlocks.isEmpty()) {
            erodeLayer(4);
        }
    }

    private void erodeLayer(int layer) {
        switch (layer) {
            case 1 -> erodeLayerRandom(firstLayerDecayingBlocks);
            case 2 -> erodeLayerRandom(secondLayerDecayingBlocks);
            case 3 -> erodeLayerRandom(thirdLayerDecayingBlocks);
            case 4 -> erodeLayerRandom(fourthLayerDecayingBlocks);
        }
    }

    private void erodeLayerRandom(List<Block> decaying) {
        int rand = (int) (Math.random()*decaying.size());
        Block b = decaying.get(rand);
        // TODO: optimize this, creating a new runnable each time seems irresponsible
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (b.getType()) {
                    case LIGHT_GRAY_CONCRETE, WHITE_CONCRETE -> b.setType(Material.YELLOW_CONCRETE);
                    case YELLOW_CONCRETE -> b.setType(Material.ORANGE_CONCRETE);
                    case ORANGE_CONCRETE -> b.setType(Material.RED_CONCRETE);
                    case RED_CONCRETE -> {
                        b.setType(Material.AIR);
                        decaying.remove(b);
                        this.cancel();
                    }
                    case AIR -> this.cancel();
                }
            }
        }.runTaskTimer(MBC.getInstance().plugin, 20, 20);
    }
}