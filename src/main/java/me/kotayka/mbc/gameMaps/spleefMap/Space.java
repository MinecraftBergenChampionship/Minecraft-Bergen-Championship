package me.kotayka.mbc.gameMaps.spleefMap;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Space extends SpleefMap {
    public Space() {
        super("Space", 80);
    }

    @Override
    public void resetMap() {
        // paste map
        int copy_from_x = 169;
        int copy_from_z = -227;
        for (int paste_to_x = -31; paste_to_x <= 24; paste_to_x++) {
            for (int paste_to_z = -27; paste_to_z <= 30; paste_to_z++) {
                for (int y = 90; y<= 120; y++) {
                    Block paste = getWorld().getBlockAt(copy_from_x, y, copy_from_z);
                    if (paste.getType().equals(Material.AIR) || paste.getType().equals(Material.GOLD_BLOCK)) continue;

                    getWorld().getBlockAt(paste_to_x, y, paste_to_z).setType(paste.getType());
                }
                copy_from_z++;
            }
            copy_from_z = -227;
            copy_from_x++;
        }
    }

    @Override
    public void deleteMap() {
        for (int paste_to_x = -31; paste_to_x <= 24; paste_to_x++) {
            for (int paste_to_z = -27; paste_to_z <= 30; paste_to_z++) {
                for (int y = 90; y<= 120; y++) {
                    getWorld().getBlockAt(paste_to_x, y, paste_to_z).setType(Material.AIR);
                }
            }
        }
    }


    @Override
    public void Border(int timeRemaining) {

    }
}
