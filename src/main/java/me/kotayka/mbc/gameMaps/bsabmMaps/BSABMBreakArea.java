package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.Location;
import org.bukkit.Material;

public class BSABMBreakArea {

    private final Material material;
    private final Location lowNE;
    private final Location highSW;
    private final boolean square;

    private Location replicationLowNE = null;
    private Location replicationHighSW = null;

    public BSABMBreakArea(Material material, Location lowNE, Location highSW) {
        this.material = material;
        this.lowNE = lowNE;
        this.highSW = highSW;
        square = true;
    }

    public BSABMBreakArea(Material material, Location lowNE, Location highSW, Location replicationLowNE, Location replicationHighSW) {
        this.material = material;
        this.lowNE = lowNE;
        this.highSW = highSW;
        this.replicationLowNE = replicationLowNE;
        this.replicationHighSW = replicationHighSW;
        square = true;
    }

    public void replace() {

    }
}
