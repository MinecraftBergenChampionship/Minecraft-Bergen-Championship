package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;

public class Cliffs extends TGTTOSMap {
    public Cliffs() {
        super("Cliffs", null);
        super.loadMap(
                new Location(getWorld(), -100, 71, -100),
                new Location[]{new Location(getWorld(), -114, 75, -213), new Location(getWorld(), -109, 75, -207)},
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
