package me.kotayka.mbc.gameTeams;

import me.kotayka.mbc.Team;
import me.kotayka.mbc.gameMaps.bsabmMaps.BSABMPlot;

import java.util.ArrayList;
import java.util.List;

public class bsabmTeam extends gameTeam {

    private List<BSABMPlot> buildPlots = new ArrayList<>();
    private List<BSABMPlot> examplePlots = new ArrayList<>();

    public bsabmTeam(Team team) {
        super(team);
    }

    public void addBuildPlot(BSABMPlot plot) {
        buildPlots.add(plot);
    }

    public void addExamplePlot(BSABMPlot plot) {
        examplePlots.add(plot);
    }
}
