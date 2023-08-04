package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.Location;

public class BuildPlot {
    private Build build;
    private final Location SE_CORNER;
    private final Location MIDPOINT;
    private final boolean EXAMPLE;
    private final int ID;

    public BuildPlot(Location seCorner, boolean example, int ID) {
        SE_CORNER = seCorner;
        MIDPOINT = new Location(SE_CORNER.getWorld(), seCorner.getX()-3, seCorner.getY(), seCorner.getZ()-3);
        this.EXAMPLE = example;
        this.ID = ID;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
        if (EXAMPLE) placeBuild();
        else build.placeFirstLayer(MIDPOINT);
    }

    public void placeBuild() {
        build.placeCompleteBuild(MIDPOINT);
    }

    public void placeFirstLayer() {
        build.placeFirstLayer(MIDPOINT);
    }

    public void setAir() {
        build.setAir(MIDPOINT);
    }

    public boolean inBuildPlot(Location location) {
        return location.getY() >= SE_CORNER.getY() && location.getY() <= SE_CORNER.getY()+5
            && location.getX() <= SE_CORNER.getX() && location.getX() >= SE_CORNER.getX()-6
            && location.getZ() <= SE_CORNER.getZ() && location.getZ() >= SE_CORNER.getZ()-6;
    }

    public Location getCorner() { return SE_CORNER; }
    public Location getMIDPOINT() { return MIDPOINT; }
    public int getID() { return ID; }
}
