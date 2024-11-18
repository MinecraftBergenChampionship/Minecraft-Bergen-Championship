package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Glide extends TGTTOSMap {
    public Glide() {
        super("Glide", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.ELYTRA), new ItemStack(Material.SHEARS), new ItemStack(Material.SNOWBALL, 6), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), 204, 70, 200), new Location(getWorld(), 202, 70, 200), new Location(getWorld(), 200, 70, 200), new Location(getWorld(), 198, 70, 200), new Location(getWorld(), 196, 70, 200), new Location(getWorld(), 194, 70, 200)},
                new Location[]{new Location(getWorld(), 193, 87, 277), new Location(getWorld(), 203, 87, 272)},
                60
        );
    }

    /**
     * Set air or barriers at the start of each tgttos round.
     * @param barriers TRUE = Barriers, FALSE = Air
     */
    @Override
    public void Barriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 70; y <= 73; y++) {
            if (y == 73) {
                // set roof to prevent elytra hopping
                for (int x = 193; x <= 204; x++) {
                    for (int z = 196; z <= 201; z++) {
                        getWorld().getBlockAt(x, y, z).setType(block);
                    }
                }
            } else {
                for (int x = 193; x <= 204; x++) {
                    getWorld().getBlockAt(x, y, 201).setType(block);
                }

                for (int z = 196; z <= 201; z++) {
                    getWorld().getBlockAt(205, y, z).setType(block);
                    getWorld().getBlockAt(192, y, z).setType(block);
                }
            }
        }
    }
}
