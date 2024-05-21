package me.kotayka.mbc.gameMaps.bsabmMaps;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class BuildPlot {
    private final World WORLD;
    private Build build;
    private final Location SE_CORNER;
    private final Location MIDPOINT;
    private final boolean EXAMPLE;
    private ArmorStand buildNameDisplay = null;
    private ArmorStand authorNameDisplay = null;
    private final int ID;

    public BuildPlot(Location seCorner, boolean example, int ID) {
        SE_CORNER = seCorner;
        MIDPOINT = new Location(SE_CORNER.getWorld(), seCorner.getX()-3, seCorner.getY(), seCorner.getZ()-3);
        this.EXAMPLE = example;
        this.ID = ID;
        this.WORLD = SE_CORNER.getWorld();
    }

    public Build getBuild() {
        return build;
    }

    public void setNameDisplays() {
        if (buildNameDisplay == null) {
            Location display = new Location(WORLD, MIDPOINT.getX()+0.5, MIDPOINT.getY() + 7, MIDPOINT.getZ()+0.5);
            buildNameDisplay = (ArmorStand) WORLD.spawnEntity(display, EntityType.ARMOR_STAND);
            buildNameDisplay.setInvulnerable(true);
            buildNameDisplay.setInvisible(true);
            buildNameDisplay.setGravity(false);
        }

        if (authorNameDisplay == null) {
            Location display = new Location(WORLD, MIDPOINT.getX()+0.5, MIDPOINT.getY()+6.5, MIDPOINT.getZ()+0.5);
            authorNameDisplay = (ArmorStand) WORLD.spawnEntity(display, EntityType.ARMOR_STAND);
            authorNameDisplay.setInvulnerable(true);
            authorNameDisplay.setInvisible(true);
            authorNameDisplay.setGravity(false);
        }

        ChatColor color = switch (ID) {
            case 0 -> ChatColor.GREEN;
            case 1 -> ChatColor.YELLOW;
            default -> ChatColor.RED;
        };

        buildNameDisplay.setCustomName(color + "" + ChatColor.BOLD+this.build.getName());
        buildNameDisplay.setCustomNameVisible(true);

        String creator = this.build.getAuthor();
        if (creator.equals("MCC Original")) {
            authorNameDisplay.setCustomName(creator);
        } else {
            authorNameDisplay.setCustomName("by " + this.build.getAuthor());
        }
        authorNameDisplay.setCustomNameVisible(true);
    }

    public void setBuild(Build build) {
        this.build = build;
        if (EXAMPLE) {
            placeBuild();
            setNameDisplays();
        }
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

    public void removeNames() {
        if (buildNameDisplay != null) {
            buildNameDisplay.remove();
        }
        if (authorNameDisplay != null) {
            authorNameDisplay.remove();
        }
    }

    /**
     * Gets the amount the build is completed, as a percent between 0 and 1.
     * @return Number between 0 and 1 representing how much a build is done.
     */
    public double getPercentCompletion() {
        return build.getPercentCompletion(MIDPOINT);
    }

    public Location getCorner() { return SE_CORNER; }
    public Location getMIDPOINT() { return MIDPOINT; }
    public int getID() { return ID; }
}
