package me.kotayka.mbc.gameMaps.bsabmMaps;

import it.unimi.dsi.fastutil.Pair;
import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuildMartMap extends MBCMap {
    // Material corresponds to list since some materials may have multiple break areas (stained glass & concrete)
    // Note Note: it may be better to use a separate class, since these won't require BreakArea.replicaXYZ
    private final HashMap<Material, List<BreakArea>> breakAreas = new HashMap<Material, List<BreakArea>>();
    public final Map<BreakAreaType, Location> replicationLocations = new HashMap<>();
    public final int deathY;
    public final int FIRST_LAYER_Y;

    public AbstractBuildMartMap(int deathY, int firstLayerY) {
        super(Bukkit.getWorld("BSABM"));
        this.deathY = deathY;
        this.FIRST_LAYER_Y = firstLayerY;
    }

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

    public HashMap<Material, List<BreakArea>> BreakAreas() { return breakAreas; }
}
