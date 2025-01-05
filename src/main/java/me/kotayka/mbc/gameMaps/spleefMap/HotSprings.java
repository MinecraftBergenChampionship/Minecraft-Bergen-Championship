package me.kotayka.mbc.gameMaps.spleefMap;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class HotSprings extends SpleefMap {
    private List<List<Block>> blocks = new LinkedList<>();
    private Set<Block> decaying = new HashSet<>();
    private Set<Block> toRemove = new HashSet<>();
    private int maxErosionLayer = 0;
    int erosion = -1;
    public HotSprings() {
        super("Hot Springs", 90, "Regular");

        for (int i = 0; i < 12; i++) {
            blocks.add(new LinkedList<>());
        }
    }

    @Override
    public void resetMap() {
        if (erosion != -1) {
            MBC.getInstance().cancelEvent(erosion);
            erosion = -1;
        }
        // paste map
        int copy_from_x = -218;
        int copy_from_z = 183;
        for (int paste_to_x = -18; paste_to_x <= 17; paste_to_x++) {
            for (int paste_to_z = -17; paste_to_z <= 17; paste_to_z++) {
                int y_index = 0;
                for (int y = 98; y<= 109; y++) {
                    Block paste_from = getWorld().getBlockAt(copy_from_x, y, copy_from_z);
                    if (paste_from.getType().equals(Material.AIR) || paste_from.getType().equals(Material.GOLD_BLOCK)) continue;
                    Block paste_to = getWorld().getBlockAt(paste_to_x, y, paste_to_z);
                    paste_to.setType(paste_from.getType());
                    paste_to.setBlockData(paste_from.getBlockData());
                    blocks.get(y_index).add(paste_to);
                    y_index++;
                }
                copy_from_z++;
            }
            copy_from_z = 183;
            copy_from_x++;
        }
    }

    @Override
    public void deleteMap() {
        for (int paste_to_x = -18; paste_to_x <= 17; paste_to_x++) {
            for (int paste_to_z = -17; paste_to_z <= 17; paste_to_z++) {
                for (int y = 98; y<= 109; y++) {
                    getWorld().getBlockAt(paste_to_x, y, paste_to_z).setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void Border(int timeRemaining) {
        if ((timeRemaining <= 150 && timeRemaining % 30 == 0 || timeRemaining <= 105 && timeRemaining % 15 == 0) && maxErosionLayer < 12) {
            Bukkit.broadcastMessage(ChatColor.RED + "The decay is rising!");
            maxErosionLayer++;
        }

        if (timeRemaining <= 150) {
            erodeMap();
        }
    }

    private void erodeMap() {
        if (erosion == -1) {
            erosion = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
                for (Block b : decaying) {
                    switch (b.getType()) {
                        case YELLOW_CONCRETE -> b.setType(Material.ORANGE_CONCRETE);
                        case ORANGE_CONCRETE -> b.setType(Material.RED_CONCRETE);
                        case RED_CONCRETE -> {
                            b.setType(Material.AIR);
                            toRemove.add(b);
                        }
                        case AIR -> toRemove.add(b);
                        default -> b.setType(Material.YELLOW_CONCRETE);
                    }
                }
                for (Block b : toRemove) {
                    decaying.remove(b);
                }
            }, 40, 40);
        }
        for (int i = 0; i < 30 && blocks.size() > 1; i++) {
            int rand2 = (int) (Math.random() * maxErosionLayer); // which layer to erode
            int rand1 = (int) (Math.random() * blocks.get(rand2).size()); // which block to erode
            decaying.add(blocks.get(rand2).get(rand1));
        }
    }
}
