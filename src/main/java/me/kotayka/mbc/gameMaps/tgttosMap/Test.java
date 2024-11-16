package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Test extends TGTTOSMap {
    public Test() {
        super("Test", new ItemStack[]{new ItemStack(Material.WHITE_WOOL)});
        super.loadMap(new Location[]{new Location(getWorld(), 10, 81, 10)}, new Location[]{new Location(getWorld(), 20, 81, 20), new Location(getWorld(), 25, 81, 25)}, 50);
        // changed loadmap to one location
        //super.loadMap(new Location[]{new Location(getWorld(), 10, 81, 10), new Location(getWorld(), 15, 81, 15)},new Location[]{new Location(getWorld(), 20, 81, 20), new Location(getWorld(), 25, 81, 25)}, 50);
    }

    @Override
    public void Barriers(boolean barriers) {
    }
}
