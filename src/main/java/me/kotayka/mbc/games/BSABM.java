package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.bsabmMaps.BSABMMap;
import me.kotayka.mbc.gamePlayers.BSABMPlayer;
import me.kotayka.mbc.gameTeams.bsabmTeam;
import me.kotayka.mbc.gameMaps.bsabmMaps.BuildMart;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class BSABM extends Game {

    public BSABMMap map = new BuildMart();
    public bsabmTeam red = new bsabmTeam(MBC.red, new Location(map.getWorld(), -107, 1, 150, -90, 0));
    public bsabmTeam yellow = new bsabmTeam(MBC.yellow, new Location(map.getWorld(), -68, 1, 150, -90, 0));
    public bsabmTeam green = new bsabmTeam(MBC.green, new Location(map.getWorld(), -28, 1, 150, -90, 0));
    public bsabmTeam blue = new bsabmTeam(MBC.blue, new Location(map.getWorld(), 11, 1, 150, -90, 0));
    public bsabmTeam purple = new bsabmTeam(MBC.purple, new Location(map.getWorld(), 50, 1, 150, -90, 0));
    public bsabmTeam pink = new bsabmTeam(MBC.pink, new Location(map.getWorld(), 89, 1, 150, -90, 0));

    public BSABM() {
        super(3, "BSABM");


    }

    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Game: "+ MBC.gameNum+"/8:" + ChatColor.WHITE + " TGTTOS", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    public void events() {

    }

    public void start() {
        super.start();
        setTimer(600);
    }

    public void loadPlayers() {
        for (Participant p : MBC.getIngamePlayer()) {
            BSABMPlayer bsabmPlayer = new BSABMPlayer(p, this);
            gamePlayers.add(bsabmPlayer);
            bsabmPlayer.respawn();
            p.getPlayer().teleport(new Location(map.getWorld(), 10, 1, 0, -90, 0));
        }
    }
}
