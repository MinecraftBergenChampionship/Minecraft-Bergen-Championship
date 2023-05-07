package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BSABMBreakArea {

    private final Material material;
    private final Location lowSE;
    private final Location highNW;
    private final World world;
    private Location replicationLowSE = null;
    private Location replicationHighNW = null;

    public BSABMBreakArea(Material material, Location lowSE, Location highNW, World world) {
        this.material = material;
        this.lowSE = lowSE;
        this.highNW = highNW;
        this.world = world;
    }

    public BSABMBreakArea(Material material, Location lowSE, Location highNW, World world, Location replicationLowSE, Location replicationHighNW) {
        this.material = material;
        this.lowSE = lowSE;
        this.highNW = highNW;
        this.world = world;
        this.replicationLowSE = replicationLowSE;
        this.replicationHighNW = replicationHighNW;
    }

    public void replace() {
        if (replicationLowSE == null) {
            for (int y = lowSE.getBlockY(); y <= highNW.getY(); y++) {
                for (int x = highNW.getBlockX(); x <= lowSE.getX(); y++) {
                    for (int z = highNW.getBlockX(); z <= lowSE.getX(); y++) {
                        world.getBlockAt(x,y,z).setType(material);
                    }
                }
            }
        }
    }

    public boolean blockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();

        if (loc.getY() >= lowSE.getY() && loc.getY() <= highNW.getY()) {
            if (loc.getX() >= highNW.getX() && loc.getX() <= lowSE.getX()) {
                if (loc.getZ() >= highNW.getZ() && loc.getZ() <= lowSE.getZ()) {
                    e.setDropItems(false);
                    e.getPlayer().getInventory().addItem(new ItemStack(e.getBlock().getType()));
                }
            }
        }
        return false;
    }
}
