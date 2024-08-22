package me.kotayka.mbc.gameMaps.quickfireMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Quickfire;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;

public abstract class QuickfireMap extends MBCMap {
    protected final Quickfire QUICKFIRE;
    private Location TEAM_ONE_SPAWN;
    private Location TEAM_TWO_SPAWN;
    private Location SPAWN;
    private int timeUntilGlowing;

    public QuickfireMap(Quickfire qf) {
        super(Bukkit.getWorld("Quickfire"));

        this.QUICKFIRE = qf;
    }

    public Location getTeamOneSpawn() {
        return TEAM_ONE_SPAWN;
    }

    public Location getTeamTwoSpawn() {
        return TEAM_TWO_SPAWN;
    }

    public Location getSpawn() {
        return SPAWN;
    }

    public int getTimeUntilGlowing() {
        return timeUntilGlowing;
    }

    public void loadWorld(Location TEAM_ONE_SPAWN, Location TEAM_TWO_SPAWN, Location SPAWN, int timeUntilGlowing) {
        this.TEAM_ONE_SPAWN = TEAM_ONE_SPAWN;
        this.TEAM_TWO_SPAWN = TEAM_TWO_SPAWN;
        this.SPAWN = SPAWN;
        this.timeUntilGlowing = timeUntilGlowing;
    }

    /**
     * Expensive function called after each round and when loading the game;
     * Resets the map to a state copied from another area in the Skybattle World.
     * @implSpec should call removeEntities() and resetKillMaps()
     */
    public abstract void resetBarriers(boolean b);

    public abstract void changeColor(MBCTeam firstPlace, MBCTeam secondPlace);
}
