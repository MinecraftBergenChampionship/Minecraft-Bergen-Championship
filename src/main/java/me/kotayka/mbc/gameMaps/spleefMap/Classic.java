package me.kotayka.mbc.gameMaps.spleefMap;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Classic extends SpleefMap {
    public Classic() {
        super("Classic", 60);
    }

    @Override
    public void pasteMap() {
        int copy_from_x = 180;
        int copy_from_z = 180;

        // first 3 layers
        for (int paste_to_x = -20; paste_to_x <= 20; paste_to_x++) {
            for (int paste_to_z = -20; paste_to_z <= 20; paste_to_z++) {
                Block paste_1 = getWorld().getBlockAt(copy_from_x, 100, copy_from_z);
                if (!(paste_1.getType().equals(Material.AIR))) { // first three layers are identical
                    getWorld().getBlockAt(paste_to_x, 100, paste_to_z).setType(Material.WHITE_CONCRETE);
                    getWorld().getBlockAt(paste_to_x, 88, paste_to_z).setType(Material.LIGHT_GRAY_CONCRETE);
                    getWorld().getBlockAt(paste_to_x, 76, paste_to_z).setType(Material.WHITE_CONCRETE);
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
                    getWorld().getBlockAt(paste_to_x, 64, paste_to_z).setType(b.getType());
                }
                copy_from_z++;
            }
            copy_from_x++;
            copy_from_z = 175;
        }
    }

    @Override
    public void Border() {

    }
}
