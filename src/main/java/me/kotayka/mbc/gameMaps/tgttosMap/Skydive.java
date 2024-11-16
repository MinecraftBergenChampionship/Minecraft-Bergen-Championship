package me.kotayka.mbc.gameMaps.tgttosMap;

import me.kotayka.mbc.games.TGTTOS;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Skydive extends TGTTOSMap {
    public Skydive() {
        super("Skydive", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.SHEARS), new ItemStack(Material.SNOWBALL, 6), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), -300, 120, 295, 90, 0), new Location(getWorld(), -300, 120, 297, 90, 0), new Location(getWorld(), -300, 120, 299, 90, 0), new Location(getWorld(), -300, 120, 300, 90, 0), new Location(getWorld(), -300, 120, 302, 90, 0), new Location(getWorld(), -300, 120, 304, 90, 0)}, 
            new Location[]{new Location(getWorld(), -399, 50, 297), new Location(getWorld(), -394, 50, 302)},
                45
        );
    }

    /**
     * Set air or barriers at the start of each tgttos round.
     * @param barriers TRUE = Barriers, FALSE = Air
     */
    @Override
    public void Barriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 120; y <= 123; y++) {
            for (int z = 293; z <= 306; z++) {
                getWorld().getBlockAt(-302, y, z).setType(block);
            }
        }
    }
}
