package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Trident extends TGTTOSMap {
    public Trident() {
        super("Trident", new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.TRIDENT)});
        super.loadMap(
            new Location[]{new Location(getWorld(), -490, 65, 503), new Location(getWorld(), -494, 65, 503), new Location(getWorld(), -498, 65, 503), new Location(getWorld(), -502, 65, 503), new Location(getWorld(), -506, 65, 503), new Location(getWorld(), -510, 65, 503)}, 
            new Location[]{new Location(getWorld(), -508, 65, 668), new Location(getWorld(), -493, 65, 668)},
               55
        );
    }

    /**
     * Set air or barriers at the start of each tgttos round.
     * @param barriers TRUE = Barriers, FALSE = Air
     */
    @Override
    public void Barriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 65; y <= 67; y++) {
            for (int x = -513; x <= -488; x++) {
                getWorld().getBlockAt(x, y, 505).setType(block);
                getWorld().getBlockAt(x, y, 500).setType(block);
            }
            for (int z = 500; z <= 505; z++) {
                getWorld().getBlockAt(-488, y, z).setType(block);
                getWorld().getBlockAt(-513, y, z).setType(block);
            }
        }
    }
}
