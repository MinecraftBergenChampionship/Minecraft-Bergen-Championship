package me.kotayka.mbc.partygames;

import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;

public class BeepTestLevel {
    private final Location pasteFrom;
    private final String name;
    private final CuboidRegion region;

    public BeepTestLevel(CuboidRegion region, String name, Location pasteFrom) {
        this.region = region;
        this.pasteFrom = pasteFrom;
        this.name = name;
    }

    public Location getPasteFrom() { return pasteFrom; }
    public String getName() { return name; }
    public CuboidRegion getRegion() { return region; }
}
