package me.kotayka.mbc.gameMaps.bsabmMaps;

import it.unimi.dsi.fastutil.Pair;
import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuildMartMap {
    // Material corresponds to list since some materials may have multiple break areas (stained glass & concrete)
    // Note Note: it may be better to use a separate class, since these won't require BreakArea.replicaXYZ
    private final HashMap<Material, List<BreakArea>> breakAreas = new HashMap<Material, List<BreakArea>>();
    public final Map<BreakAreaType, Location> replicationLocations = new HashMap<>();
    public final int deathY;
    public final int FIRST_LAYER_Y;
    public final Location INTRO_LOC;
    private final World world = Bukkit.getWorld("BSABM");

    public AbstractBuildMartMap(int deathY, int firstLayerY, Location INTRO_LOC) {
        this.deathY = deathY;
        this.FIRST_LAYER_Y = firstLayerY;
        this.INTRO_LOC = INTRO_LOC;
    }

    public World getWorld() { return world; }

    public void addBreakArea(BreakArea area) {
        Material m = area.getType();
        List<BreakArea> temp = breakAreas.get(m);
        if (temp == null) {
            temp = new ArrayList<>(1);
            temp.add(area);
            breakAreas.put(m, temp);
        } else {
            breakAreas.get(m).add(area);
        }
    }

    /**
     * Load plots for every team to build.
     * @param teams Array of all the teams for simplicity.
     */
    public abstract void loadTeamPlots(BuildMartTeam[] teams);

    /**
     * Resets all break areas of the map
     */
    public void resetBreakAreas() {
        for (List<BreakArea> l : breakAreas.values()) {
            for (BreakArea area : l) {
                area.Replace();
            }
        }
    }

    /**
     * Load all breaking areas
     */
    public abstract void loadBreakAreas();

    /**
     * Open or close portals for main area and all teams.
     * @param b TRUE = open, FALSE = close
     */
    public abstract void openPortals(boolean b);

    /**
     * Handle movement events
     * @param e passed from BuildMart
     */
    public abstract void onMove(PlayerMoveEvent e);

    /**
     * Reset chests and furnaces
     */
    public abstract void resetBlockInventories();


    public abstract int getVolume(BreakAreaType type);

    public HashMap<Material, List<BreakArea>> BreakAreas() { return breakAreas; }
}
