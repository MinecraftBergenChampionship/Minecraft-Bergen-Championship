package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameTeams.bsabmTeam;
import me.kotayka.mbc.games.BSABM;

public class BSABMPlayer extends GamePlayer {

    private final BSABM bsabm;
    private bsabmTeam team = null;

    public BSABMPlayer(Participant p, BSABM bsabm) {
        super(p);
        this.bsabm = bsabm;

        switch (p.getTeam().getTeamName()) {
            case "RedRabbits":
                team=bsabm.red;
                break;
            case "YellowYaks":
                team=bsabm.yellow;
                break;
            case "GreenGuardians":
                team=bsabm.green;
                break;
            case "BlueBats":
                team=bsabm.blue;
                break;
            case "PurplePandas":
                team=bsabm.purple;
                break;
            case "PinkPiglets":
                team=bsabm.pink;
                break;
        }
    }

    public void giveItems() {
        getParticipant().getInventory().clear();

    }

    public void respawn() {
        giveItems();
        getParticipant().getPlayer().teleport(team.getSpawnLoc());
    }
}
