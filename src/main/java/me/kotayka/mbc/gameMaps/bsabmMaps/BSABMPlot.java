package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BSABMPlot {

    private BSABMBlueprint blueprint;

    private final Location SECorner;
    private final Location midPoint;

    private final boolean example;

    public BSABMPlot(Location seCorner, boolean example) {
        SECorner = seCorner;
        midPoint = new Location(SECorner.getWorld(), seCorner.getX()-3, seCorner.getY(), seCorner.getZ()-3);
        this.example = example;
    }

    public BSABMBlueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(BSABMBlueprint blueprint) {
        this.blueprint = blueprint;
        if (example) blueprint.placeCompleteBuild(midPoint);
        else blueprint.placeFirstLayer(midPoint);
    }

    private boolean checkBlockInGrid(Location location) {
        if (location.getY() >= SECorner.getY()+1 && location.getY() <= SECorner.getY()+6) {
            if (location.getX() >= SECorner.getX() && location.getX() <= SECorner.getX()+7) {
                if (location.getZ() >= SECorner.getZ() && location.getZ() <= SECorner.getZ()+7) {
                    return true;
                }
            }
        }
        return false;
    }

    public void blockBreak(BlockBreakEvent e) {
        if (checkBlockInGrid(e.getBlock().getLocation())) {
            e.setDropItems(false);
            e.getPlayer().getInventory().addItem(new ItemStack(e.getBlock().getType()));
        }
    }

    public boolean blockPlace(BlockPlaceEvent e) {
        return (checkBlockInGrid(e.getBlock().getLocation()) && getBlueprint().checkBuild(midPoint));
    }
}
