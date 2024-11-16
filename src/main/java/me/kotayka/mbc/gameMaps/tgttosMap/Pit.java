package me.kotayka.mbc.gameMaps.tgttosMap;

import me.kotayka.mbc.games.TGTTOS;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Pit extends TGTTOSMap {
    public Pit() {
        super("Pit", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.SHEARS), new ItemStack(Material.SNOWBALL, 6), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), 100, 70, 96, -90, 0), new Location(getWorld(), 100, 70, 98, -90, 0), new Location(getWorld(), 100, 70, 100, -90, 0), new Location(getWorld(), 100, 70, 102, -90, 0), new Location(getWorld(), 100, 70, 104, -90, 0), new Location(getWorld(), 100, 70, 106, -90, 0)}, 
            new Location[]{new Location(getWorld(), 151, 59, 104), new Location(getWorld(), 145, 59, 104)},
                50
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
            for (int z = 94; z <= 107; z++) {
                getWorld().getBlockAt(103, y, z).setType(block);
            }
        }
    }
}
