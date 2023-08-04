package me.kotayka.mbc.gameTeams;

import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.gameMaps.bsabmMaps.BuildPlot;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.Location;

public class BuildMartTeam extends gameTeam {
    private BuildPlot[][] plots = new BuildPlot[BuildMart.NUM_PLOTS_PER_TEAM][2];
    private final Location SPAWN;
    private int buildsCompleted = 0;

    public BuildMartTeam(MBCTeam team, Location spawnLoc) {
        super(team);
        this.SPAWN = spawnLoc;
    }

    public void addExamplePlot(BuildPlot plot, int n) {
        plots[n][0] = plot;
    }

    public void addBuildPlot(BuildPlot plot, int n) {
        plots[n][1] = plot;
    }

    /**
     * Intended usage:
     * getPlots()[n][0] will give the example plot
     * getPlots()[n][1] will give the replication plot
     * n is the plot number for a team
     * @return 2D Array of team plots
     */
    public BuildPlot[][] getPlots() {
        return plots;
    }

    public Location getSPAWN() {
        return SPAWN;
    }
    public int getBuildsCompleted() { return buildsCompleted; }
    public void incrementBuildsCompleted() { buildsCompleted++; }
}
