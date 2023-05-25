package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Lobby extends Game {
    public static final Location LOBBY = new Location(Bukkit.getWorld("world"), 0, 1, 0);

    public Lobby() {
        super(0, "Lobby");
    }

    public void createScoreboard(Participant p) {
        Bukkit.broadcastMessage("This is a test");

        newObjective(p);
        createLine(22, ChatColor.RED+""+ChatColor.BOLD + "Event begins in:", p);
        createLine(21, ChatColor.GREEN + "Teams ready", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:", p);
        createLine(17, p.getTeam().getChatColor()+p.getTeam().getTeamFullName(), p);
        createLine(16, ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.GREEN+"Game Scores", p);
        createLine(3, ChatColor.RESET.toString()+ChatColor.RESET.toString(), p);
        updatePlayerGameScore(p);

        p.getPlayer().sendMessage(p.getTeam().teamPlayers.toString());
        updateTeamGameScore(p.getTeam());
        teamGames();
    }

    public void changeTeam(Participant p) {
        createLine(17, p.getTeam().getChatColor()+p.getTeam().getTeamFullName(), p);
    }

    public void events() {
        switch (timeRemaining) {
            case 10:
                Bukkit.broadcastMessage(ChatColor.RED+"60 seconds left");
                break;
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (!isGameActive()) return;
        if (e.getBlock().getType() == Material.DIAMOND_BLOCK) {
            Participant p = Participant.getParticipant(e.getPlayer());
            assert p != null;
            p.addGameScore(5);
        }
    }

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent e) {
        if (!isGameActive()) return;

        if (e.getPlayer().getLocation().getY() < 30){
            e.getPlayer().teleport(LOBBY);
        }
    }

    public void start() {
        MBC.getInstance().currentGame = this;
        createScoreboard();
        stopTimer();
        setTimer(120);
    }

    @Override
    public void loadPlayers() {
        // tbd
    }
}
