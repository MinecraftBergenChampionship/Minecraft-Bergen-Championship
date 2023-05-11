package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class BuildMart extends BSABMMap {

    public BuildMart() {
        super(new Location(Bukkit.getWorld("bsabmMaps"),-6,185,2));

        addBreakArea(Material.BLUE_CONCRETE, new BSABMBreakArea(Material.BLUE_CONCRETE, new Location(getWorld(), -120, -12, 164), new Location(getWorld(), -124, -12, 159), getWorld()));
    }
}
