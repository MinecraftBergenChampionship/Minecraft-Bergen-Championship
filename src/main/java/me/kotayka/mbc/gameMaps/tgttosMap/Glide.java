package me.kotayka.mbc.gameMaps.tgttosMap;

import me.kotayka.mbc.games.TGTTOS;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Glide extends TGTTOSMap {
    public Glide() {
        super("Glide", new ItemStack[]{new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.ELYTRA), TGTTOS.getShears(), new ItemStack(Material.SNOWBALL, 6)});
        super.loadMap(
                new Location(getWorld(), 200, 70, 200),
                new Location[]{new Location(getWorld(), 193, 87, 277), new Location(getWorld(), 203, 87, 272)},
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
        // TODO
        for (int y = 70; y <= 72; y++) {
            for (int x = 193; x <= 204; x++) {
                getWorld().getBlockAt(x, y, 201).setType(block);
            }

            for (int z = 196; z <= 201; z++) {
                getWorld().getBlockAt(205, y, z).setType(block);
                getWorld().getBlockAt(192, y, z).setType(block);
            }
        }
    }
}
