package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Elytra extends TGTTOSMap {
    public Elytra() {
        super("Elytra", new ItemStack[]{new ItemStack(Material.ELYTRA), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), 300, 66, 0)},
                new Location[]{new Location(getWorld(), 448, 33, -2), new Location(getWorld(), 452, 33, 2)},
               10
        );
    }

    /**
     * Set air or barriers at the start of each tgttos round.
     * @param barriers TRUE = Barriers, FALSE = Air
     */
    @Override
    public void Barriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int z = -3; z <= 3; z++) {
            for (int y = 65; y <= 68; y++) {
                getWorld().getBlockAt(308, y, z).setType(block);
            }
            if (z >= -2 && z <= 2) {
                getWorld().getBlockAt(308, 69, z).setType(block);
            }
            if (z >= -1 && z < 1) {
                getWorld().getBlockAt(308, 70, z).setType(block);
            }
        }

    }
}
