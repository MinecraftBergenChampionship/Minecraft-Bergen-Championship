package me.kotayka.mbc.gameMaps.tgttosMap;

import me.kotayka.mbc.games.TGTTOS;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Walls extends TGTTOSMap {
    public Walls() {
        super("Walls", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), TGTTOS.getShears(), new ItemStack(Material.SNOWBALL, 6)});
        super.loadMap(
                new Location(getWorld(), 100, 71, -100),
                new Location[]{new Location(getWorld(), 94, 83, -182), new Location(getWorld(), 108, 83, -186)},
                57
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
            for (int x = 91; x <= 110; x++) {
                getWorld().getBlockAt(x, y, -101).setType(block);
            }
        }
    }
}
