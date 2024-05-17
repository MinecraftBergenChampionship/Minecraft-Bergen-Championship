package me.kotayka.mbc.partygames;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;

public class BeepTestLevel {
    private final Location pasteFrom;
    private final String name;
    private final CuboidRegion region;
    private final CuboidRegion reversedRegion;
    private final Location pasteReversed;

    public BeepTestLevel(CuboidRegion region, String name, Location pasteFrom) {
        this.region = region;
        this.pasteFrom = pasteFrom;
        this.name = name;

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        Location otherMin = new Location(pasteFrom.getWorld(), min.x()+2, region.getMinimumY(), min.z()-525);
        Location otherMax = new Location(pasteFrom.getWorld(), max.x()+2, region.getMaximumY(), max.z()-525);
        reversedRegion = new CuboidRegion(BukkitAdapter.asBlockVector(otherMin), BukkitAdapter.asBlockVector(otherMax));

        pasteReversed = new Location(pasteFrom.getWorld(), pasteFrom.getX()+2, pasteFrom.getY(), pasteFrom.getZ()-525);
    }

    public Location getPasteFrom() { return pasteFrom; }
    public String getName() { return name; }
    public CuboidRegion getRegion() { return region; }
    public CuboidRegion getReversedRegion() { return reversedRegion; }
    public Location getPasteReversed() { return pasteReversed; }
}
