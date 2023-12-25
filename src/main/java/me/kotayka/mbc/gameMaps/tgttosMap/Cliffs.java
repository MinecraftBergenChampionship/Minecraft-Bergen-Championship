package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Cliffs extends TGTTOSMap {
    public Cliffs() {
        super("Cliffs", new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS)});
        Location l = new Location(getWorld(), -100, 71, -100);
        l.setYaw((float) 180);
        super.loadMap(
                l, new Location[]{new Location(getWorld(), -113, 75, -212), new Location(getWorld(), -110, 75, -208)},
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
        for (int y = 71; y <= 73; y++) {
            for (int x = -106; x <= -93; x++) {
                getWorld().getBlockAt(x, y, -102).setType(block);
            }
        }
    }
}
