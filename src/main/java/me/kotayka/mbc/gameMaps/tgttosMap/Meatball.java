package me.kotayka.mbc.gameMaps.tgttosMap;

import me.kotayka.mbc.games.TGTTOS;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Meatball extends TGTTOSMap {
    public Meatball() {
        super("Meatball", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.SHEARS), new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
                new Location(getWorld(), -100, 70, 100),
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
            for (int z = 98; z <= 101; z++) {
                getWorld().getBlockAt(-102, y, z).setType(block);
                getWorld().getBlockAt(-98, y, z).setType(block);
            }

            for (int x = -101; x <= -99; x++) {
                getWorld().getBlockAt(x, y, 102).setType(block);
                getWorld().getBlockAt(x, y, 98).setType(block);
            }
        }
    }
}