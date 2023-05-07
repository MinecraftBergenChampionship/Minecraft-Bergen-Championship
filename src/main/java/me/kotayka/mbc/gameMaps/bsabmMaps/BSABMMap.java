package me.kotayka.mbc.gameMaps.bsabmMaps;

import me.kotayka.mbc.gameMaps.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class BSABMMap extends Map {

    private HashMap<String, BSABMBlueprint> plots = new HashMap<>();
    private List<String> gameOrder = new ArrayList<>();

    private HashMap<Material, BSABMBreakArea> breakBlocks = new HashMap<>();


    public BSABMMap(Location mapStartCoord) {
        super(Bukkit.getWorld("BSABM"));
        loadMaps(mapStartCoord);
        Collections.shuffle(gameOrder);
    }

    public void loadMaps(Location mapStartCoord) {
        Block b = Bukkit.getWorld("bsabmMaps").getBlockAt(mapStartCoord);

        while (b.getType() == Material.DIAMOND_BLOCK) {
            BSABMBlueprint plot = new BSABMBlueprint(b.getLocation());
            plots.put(plot.getName(), plot);
            gameOrder.add(plot.getName());

            b = getWorld().getBlockAt(b.getX()-7, b.getY(), b.getZ());
        }
    }

    public void addBreakArea(Material mat, BSABMBreakArea area) {
        breakBlocks.put(mat, area);
    }
}
