package me.kotayka.mbc.gameTeams;

import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.gameMaps.bsabmMaps.Build;
import me.kotayka.mbc.gameMaps.bsabmMaps.BuildPlot;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class BuildMartTeam extends gameTeam {
    private final BuildPlot[][] plots = new BuildPlot[BuildMart.NUM_PLOTS_PER_TEAM][2];
    private final Location SPAWN;
    private final Map<Build, Integer> completions = new HashMap<>();
    private int buildsCompleted = 0; // this probably doesn't need to exist anymore but i'm keeping it for now just for convenience
    private int easyBuildsCompleted = 0;
    private int mediumBuildsCompleted = 0;
    private int hardBuildsCompleted = 0;


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
    public int getEasyBuildsCompleted() { return easyBuildsCompleted; }
    public int getMediumBuildsCompleted() { return mediumBuildsCompleted; }
    public int getHardBuildsCompleted() { return hardBuildsCompleted; }
    public void incrementBuildsCompleted(int id) {
        if (id != 0 && id != 1 && id != 2) return;
        buildsCompleted++;

        switch (id) {
            case 0 -> easyBuildsCompleted++;
            case 1 -> mediumBuildsCompleted++;
            default -> hardBuildsCompleted++;
        }
    }
    public Map<Build, Integer> getCompletions() { return completions; }
}
