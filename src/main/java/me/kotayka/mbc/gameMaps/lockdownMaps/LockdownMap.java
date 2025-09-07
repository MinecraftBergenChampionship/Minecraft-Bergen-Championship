package me.kotayka.mbc.gameMaps.lockdownMaps;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.games.Lockdown;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;

// not sure if inheriting from Map is really necessary since theres only one commonality but just in case
public abstract class LockdownMap extends MBCMap {
    protected final Lockdown LOCKDOWN;
    private Location CENTER;
    private Location[][] woolLocations = new Location[6][6]; 
    public Location[] spawns;

    private MBCTeam[] teamOrder = new MBCTeam[6];

    public LockdownMap(Lockdown ld) {
        super(Bukkit.getWorld("Lockdown"));
        this.LOCKDOWN = ld;
    }

    public void loadWorld(Location center, Location[][] w) {
        this.CENTER = center;
        this.woolLocations = w;
        getWorld().setSpawnLocation(center);
        resetMap();
    }

    /*
     * Returns woolLocations as a list.
     */
    public List<Location> getWoolLocations() {
        List<Location> l = new ArrayList<Location>();

        for (int i = 0; i < woolLocations.length; i++) {
            for (int j = 0; j < woolLocations[i].length; j++) {
                l.add(woolLocations[i][j]);
            }
        }

        return l;
    }

    /*
     * Returns the row of a location. Returns -1 if not found.
     */
    public int rowOfLocation(Location l) {

        for (int i = 0; i < woolLocations.length; i++) {
            for (int j = 0; j < woolLocations[i].length; j++) {
                if (l.equals(woolLocations[i][j])) return i;
            }
        }

        return -1;
    }

    /*
     * Returns the column of a location. Returns -1 if not found.
     */
    public int columnOfLocation(Location l) {

        for (int i = 0; i < woolLocations.length; i++) {
            for (int j = 0; j < woolLocations[i].length; j++) {
                if (l.equals(woolLocations[i][j])) return j;
            }
        }

        return -1;
    }

    public abstract ArrayList<MBCTeam> roundOneSpawns();

    public abstract ArrayList<MBCTeam> roundTwoSpawns();

    public abstract ArrayList<MBCTeam> roundThreeSpawns();

    public void setTeamOrder(MBCTeam[] teamOrder) {
        this.teamOrder = teamOrder;
    }

    public MBCTeam[] getTeamOrder() {
        return teamOrder;
    }

    /**
     * Expensive function called after each round and when loading the game;
     * Resets the map to a state copied from another area in the Skybattle World.
     * @implSpec should call removeEntities() and resetKillMaps()
     */
    public abstract void resetMap();

    /**
     * Called to show where each team spawns. Will be used to give map of where each team is at beginning of round.
     */
    public abstract MBCTeam[] teamSpawnLocations();

    /**
     * Called to reset all team spawn locations in array.
     */
    public abstract void resetTeamSpawnLocations();

    /**
     * Remove all extraneous entities lingering in the map when resetting.
     * There is almost certainly a better way to do this but personally,
     *
     * @see LockdownMap resetMap()
     */
    public void removeEntities() {
        // Clear all floor items
        for (Item item : getWorld().getEntitiesByClass(Item.class)) {
            item.remove();
        }
    }

    public abstract void removeBarriers();

    public abstract void addBarriers();

    /**
     * Uses array of spawns to spawn players with their team in a random spawn
     * This is abstract to allow for customization of what items players spawn with.
     * TODO: could maybe standardize something and then override if necessary but I'm kinda lazy
     */
    public abstract void spawnPlayers();
    public abstract void spawnPlayers(int roundNum);

    public Location getCenter() { return CENTER; }

}
