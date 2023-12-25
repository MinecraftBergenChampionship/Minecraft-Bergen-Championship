package me.kotayka.mbc.gameMaps.bsabmMaps;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.*;
import org.bukkit.block.Block;

public class BreakArea {
    private final AbstractBuildMartMap map;
    private final Material MATERIAL;
    private final Location HIGH_SE;
    private final Location LOW_NW;
    private final World world;
    private final Location DISLOCATE_LOCATION; // Where to teleport players inside radius to prevent suffocation during replacement
    private final BreakAreaType TYPE;
    private final int VOLUME;
    private int brokenBlocks = 0;

    public BreakArea(Material material, Location lowNW, Location highSE, Location dislocateLocation) {
        this.MATERIAL = material;
        this.HIGH_SE = highSE;
        this.LOW_NW = lowNW;
        this.world = lowNW.getWorld();
        this.DISLOCATE_LOCATION = dislocateLocation;
        TYPE = BreakAreaType.REGULAR;
        VOLUME = (Math.abs(highSE.getBlockY() - lowNW.getBlockY())+1) * (Math.abs(highSE.getBlockX() - lowNW.getBlockX())+1) * (Math.abs(highSE.getBlockZ() - lowNW.getBlockZ())+1)-1;
        map = ((BuildMart) MBC.getInstance().getGame()).map;
    }

    public BreakArea(Material material, Location lowNW, Location highSE, BreakAreaType type, Location dislocateLocation) {
        this.MATERIAL = material;
        this.LOW_NW = lowNW;
        this.HIGH_SE = highSE;
        this.world = lowNW.getWorld();
        this.TYPE = type;
        this.DISLOCATE_LOCATION = dislocateLocation;
        map = ((BuildMart) MBC.getInstance().getGame()).map;
        VOLUME = map.getVolume(type);
    }

    public void Replace() {
        /*
         * Note: SE = (+,+), NW = (-,-)
         * Work from NW -> SE
         * This is distinctly not how the plots worked but this is separate
         */
        relocatePlayers();
        if (TYPE == BreakAreaType.REGULAR) {
            for (int y = LOW_NW.getBlockY(); y <= HIGH_SE.getY(); y++) {
                for (int x = LOW_NW.getBlockX(); x <= HIGH_SE.getX(); x++) {
                    for (int z = LOW_NW.getBlockZ(); z <= HIGH_SE.getBlockZ(); z++) {
                        world.getBlockAt(x,y,z).setType(MATERIAL);
                    }
                }
            }
        } else if (TYPE.toString().contains("LOGS")) {
            // ^^ this might be bad form but keeping it for now
            Location replicationLowNW = map.replicationLocations.get(TYPE);
            int copyY = replicationLowNW.getBlockY();
            int copyX = replicationLowNW.getBlockX();
            int copyZ = replicationLowNW.getBlockZ();
            for (int y = LOW_NW.getBlockY(); y <= HIGH_SE.getY(); y++) {
                for (int x = LOW_NW.getBlockX(); x <= HIGH_SE.getX(); x++) {
                    for (int z = LOW_NW.getBlockZ(); z <= HIGH_SE.getBlockZ(); z++) {
                        Block copy = world.getBlockAt(copyX, copyY, copyZ);
                        if (!copy.getType().equals(Material.AIR)) {
                            Block b = world.getBlockAt(x,y,z);
                            b.setBlockData(copy.getBlockData());
                        }
                        copyZ++;
                    }
                    copyX++;
                    copyZ = replicationLowNW.getBlockZ();
                }
                copyY++;
                copyX = replicationLowNW.getBlockX();
            }
        } else {
            Location replicationLowNW = map.replicationLocations.get(TYPE);
            int copyY = replicationLowNW.getBlockY();
            int copyX = replicationLowNW.getBlockX();
            int copyZ = replicationLowNW.getBlockZ();
            for (int y = LOW_NW.getBlockY(); y <= HIGH_SE.getY(); y++) {
                for (int x = LOW_NW.getBlockX(); x <= HIGH_SE.getX(); x++) {
                    for (int z = LOW_NW.getBlockZ(); z <= HIGH_SE.getBlockZ(); z++) {
                        Block copy = world.getBlockAt(copyX, copyY, copyZ);
                        if (!copy.getType().equals(Material.AIR)) {
                            Block b = world.getBlockAt(x, y, z);
                            b.setType(MATERIAL);
                        }
                        copyZ++;
                    }
                    copyX++;
                    copyZ = replicationLowNW.getBlockZ();
                }
                copyY++;
                copyX = replicationLowNW.getBlockX();
            }
        }
    }

    public void relocatePlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            Location l = p.getPlayer().getLocation();
            if (l.getBlockX() >= LOW_NW.getBlockX() && l.getBlockX() <= HIGH_SE.getBlockX() &&
                l.getBlockY() >= LOW_NW.getBlockY() && l.getBlockY() <= HIGH_SE.getBlockY() &&
                l.getBlockZ() >= LOW_NW.getBlockZ() && l.getBlockZ() <= HIGH_SE.getBlockZ())
                p.getPlayer().teleport(DISLOCATE_LOCATION);
        }
    }

    public void breakBlock() {
        brokenBlocks++;
        if (brokenBlocks == VOLUME) {
            Replace();
            brokenBlocks = 0;
        }
    }

    /*
    public boolean lastBlock() {
        int count = 0;
        if (TYPE == BreakAreaType.REGULAR) {
            for (int y = LOW_NW.getBlockY(); y <= HIGH_SE.getY(); y++) {
                for (int x = LOW_NW.getBlockX(); x <= HIGH_SE.getX(); x++) {
                    for (int z = LOW_NW.getBlockZ(); z <= HIGH_SE.getBlockZ(); z++) {
                        if (!(world.getBlockAt(x, y, z).getType().equals(MATERIAL))) {
                            count++;
                        }
                    }
                }
            }
        } else {
            Location replicationLowNW = map.replicationLocations.get(TYPE);
            int copyY = replicationLowNW.getBlockY();
            int copyX = replicationLowNW.getBlockX();
            int copyZ = replicationLowNW.getBlockZ();
            for (int y = LOW_NW.getBlockY(); y <= HIGH_SE.getY(); y++) {
                for (int x = LOW_NW.getBlockX(); x <= HIGH_SE.getX(); x++) {
                    for (int z = LOW_NW.getBlockZ(); z <= HIGH_SE.getBlockZ(); z++) {
                        if (!(world.getBlockAt(x, y, z).getType().equals(world.getBlockAt(copyX, copyY, copyZ).getType()))) {
                            count++;
                        }
                        copyZ++;
                    }
                    copyX++;
                    copyZ = replicationLowNW.getBlockZ();
                }
                copyY++;
                copyX = replicationLowNW.getBlockX();
            }
        }

        //Bukkit.broadcastMessage("[Debug] count == " + count);
        //Bukkit.broadcastMessage("[Debug] VOLUME == " + VOLUME);
        return count == VOLUME;
    }
     */

    public boolean inArea(Block b) { return inArea(b.getLocation()); }
    public boolean inArea(Location l) {
        return l.getY() >= LOW_NW.getY() && l.getY() <= HIGH_SE.getY()
            && l.getX() >= LOW_NW.getX() && l.getX() <= HIGH_SE.getX()
            && l.getZ() >= LOW_NW.getZ() && l.getZ() <= HIGH_SE.getZ();
    }

    public Material getType() { return MATERIAL; }
    public int getVolume() { return VOLUME; }
    public Location getLowNW() { return LOW_NW; }
    public Location getHigh_SE() { return HIGH_SE; }
}
