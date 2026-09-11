package me.kotayka.mbc.gameMaps.tgttosMap;

import me.kotayka.mbc.games.TGTTOS;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Freeway extends TGTTOSMap {
    public Freeway() {
        super("Freeway", "bigkirbypuff_", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.SHEARS), new ItemStack(Material.SNOWBALL, 6), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), 486, 65, -486, 180, 0), new Location(getWorld(), 491, 65, -486, 180, 0), new Location(getWorld(), 496, 65, -486, 180, 0), new Location(getWorld(), 503, 65, -486, 180, 0), new Location(getWorld(), 508, 65, -486, 180, 0), new Location(getWorld(), 513, 65, -486, 180, 0)}, 
            new Location[]{new Location(getWorld(), 492, 64, -617), new Location(getWorld(), 508, 64, -617)},
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
        for (int y = 65; y <= 70; y++) {
            for (int x = 483; x <= 517; x++) {
                getWorld().getBlockAt(x, y, -488).setType(block);
            }
        }
    }
}
