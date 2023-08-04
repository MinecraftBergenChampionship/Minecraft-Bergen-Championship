package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import me.kotayka.mbc.games.BuildMart;

public class BuildMartPlayer extends GamePlayer {

    private final BuildMart bsabm;
    private BuildMartTeam team = null;

    public BuildMartPlayer(Participant p, BuildMart buildMart) {
        super(p);
        this.bsabm = buildMart;

        switch (p.getTeam().getTeamName()) {
            case "RedRabbits" -> team = buildMart.red;
            case "YellowYaks" -> team = buildMart.yellow;
            case "GreenGuardians" -> team = buildMart.green;
            case "BlueBats" -> team = buildMart.blue;
            case "PurplePandas" -> team = buildMart.purple;
            case "PinkPiglets" -> team = buildMart.pink;
        }
    }

    public void respawn() {
        getParticipant().getInventory().clear();
        getParticipant().getPlayer().teleport(team.getSPAWN());
    }
}
