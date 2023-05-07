package me.kotayka.mbc.gameTeams;

import me.kotayka.mbc.Team;
import me.kotayka.mbc.gameMaps.bsabmMaps.BSABMPlot;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class bsabmTeam extends gameTeam {

    private List<BSABMPlot> buildPlots = new ArrayList<>();
    private List<BSABMPlot> examplePlots = new ArrayList<>();

    private final Location spawnLoc;

    public bsabmTeam(Team team, Location spawnLoc) {
        super(team);
        this.spawnLoc = spawnLoc;
    }

    public void addBuildPlot(BSABMPlot plot) {
        buildPlots.add(plot);
    }

    public void addExamplePlot(BSABMPlot plot) {
        examplePlots.add(plot);
    }
}
