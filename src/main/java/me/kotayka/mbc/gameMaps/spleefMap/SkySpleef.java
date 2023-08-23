package me.kotayka.mbc.gameMaps.spleefMap;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class SkySpleef extends SpleefMap {

    public SkySpleef() {
        super("Sky Spleef", 90);
    }

    @Override
    public void resetMap() {
        // paste map
        int copy_from_x = -214;
        int copy_from_z = -217;
        for (int paste_to_x = -14; paste_to_x <= 16; paste_to_x++) {
            for (int paste_to_z = -17; paste_to_z <= 17; paste_to_z++) {
                for (int y = 100; y<= 118; y++) {
                    Block paste_from = getWorld().getBlockAt(copy_from_x, y, copy_from_z);
                    if (paste_from.getType().equals(Material.AIR) || paste_from.getType().equals(Material.GOLD_BLOCK)) continue;
                    Block paste_to = getWorld().getBlockAt(paste_to_x, y, paste_to_z);
                    paste_to.setType(paste_from.getType());
                    paste_to.setBlockData(paste_from.getBlockData());
                }
                copy_from_z++;
            }
            copy_from_z = -217;
            copy_from_x++;
        }
    }

    @Override
    public void deleteMap() {
        for (int paste_to_x = -14; paste_to_x <= 16; paste_to_x++) {
            for (int paste_to_z = -17; paste_to_z <= 17; paste_to_z++) {
                for (int y = 100; y<= 118; y++) {
                    getWorld().getBlockAt(paste_to_x, y, paste_to_z).setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void Border(int timeRemaining) {

    }
}
