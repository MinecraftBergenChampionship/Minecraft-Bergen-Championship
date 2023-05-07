package me.kotayka.mbc.gameMaps.bsabmMaps;

import me.kotayka.mbc.gameMaps.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BSABMMap extends Map {

    private HashMap<String, BSABMBlueprint> plots = new HashMap<>();
    private List<String> gameOrder = new ArrayList<>();


    public BSABMMap() {
        super(Bukkit.getWorld("BSABM"));
        loadMaps();
        Collections.shuffle(gameOrder);
    }

    public void loadMaps() {
        Block b = Bukkit.getWorld("bsabmMaps").getBlockAt(-6, 185, 2);

        while (b.getType() == Material.DIAMOND_BLOCK) {
            BSABMBlueprint plot = new BSABMBlueprint(b.getLocation());
            plots.put(plot.getName(), plot);
            gameOrder.add(plot.getName());

            b = getWorld().getBlockAt(b.getX()-7, b.getY(), b.getZ());
        }
    }
}
