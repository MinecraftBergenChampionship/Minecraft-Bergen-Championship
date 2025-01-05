package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SquidGame extends TGTTOSMap {
    public SquidGame() {
        super("SquidGame", new ItemStack[]{new ItemStack(Material.SNOWBALL, 2), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), 499, 60, 499, 180, 0), new Location(getWorld(), 503, 60, 499, 180, 0), new Location(getWorld(), 507, 60, 499, 180, 0), new Location(getWorld(), 511, 60, 499, 180, 0),new Location(getWorld(), 515, 60, 499, 180, 0), new Location(getWorld(), 519, 60, 499, 180, 0)},
                new Location[]{new Location(getWorld(), 531, 56, 438), new Location(getWorld(), 522, 56, 438), new Location(getWorld(), 513, 56, 438), new Location(getWorld(), 504, 56, 438), new Location(getWorld(), 495, 56, 438), new Location(getWorld(), 486, 56, 438)},
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
        for (int y = 60; y <= 63; y++) {
            for (int z = 496; z <= 500; z++) {
                getWorld().getBlockAt(496, y, z).setType(block);
                getWorld().getBlockAt(521, y, z).setType(block);
            }
            for (int x = 496; x <= 521; x++) {
                getWorld().getBlockAt(x, y, 496).setType(block);
                getWorld().getBlockAt(x, y, 496).setType(block);
            }
        }

    }
}
