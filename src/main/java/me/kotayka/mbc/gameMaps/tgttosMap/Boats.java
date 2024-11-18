package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Boats extends TGTTOSMap {
    public Boats() {
        super("Boats", new ItemStack[]{new ItemStack(Material.OAK_BOAT), new ItemStack(Material.SNOWBALL, 10), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), 300, 70, -305, -90, 0), new Location(getWorld(), 300, 70, -303, -90, 0), new Location(getWorld(), 300, 70, -301, -90, 0), new Location(getWorld(), 300, 70, -299, -90, 0), new Location(getWorld(), 300, 70, -297, -90, 0), new Location(getWorld(), 300, 70, -295, -90, 0)},
             new Location[]{new Location(getWorld(), 462, 65, -262), new Location(getWorld(), 467, 65, -269)},
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
        for (int y = 70; y <= 72; y++) {
            for (int z = -307; z <= -294; z++) {
                getWorld().getBlockAt(301, y, z).setType(block);
            }
        }
    }
}
