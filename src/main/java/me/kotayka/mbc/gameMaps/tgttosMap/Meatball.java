package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Meatball extends TGTTOSMap {
    public Meatball() {
        super("Meatball", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.SHEARS), new ItemStack(Material.FEATHER), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), -106, 70, 100), new Location(getWorld(), -103, 70, 94), new Location(getWorld(), -97, 70, 94), new Location(getWorld(), -94, 70, 100), new Location(getWorld(), -97, 70, 106), new Location(getWorld(), -103, 70, 106)},
                new Location[]{new Location(getWorld(), -105, 144, 104), new Location(getWorld(), -107, 144, 102)},
                64
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
            for (int z = 93; z <= 107; z++) {
                getWorld().getBlockAt(-108, y, z).setType(block);
                getWorld().getBlockAt(-92, y, z).setType(block);
            }

            for (int x = -107; x <= -93; x++) {
                getWorld().getBlockAt(x, y, 108).setType(block);
                getWorld().getBlockAt(x, y, 92).setType(block);
            }
        }
    }
}