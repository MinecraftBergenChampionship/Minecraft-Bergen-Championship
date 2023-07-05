package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Minigame;
import me.kotayka.mbc.Participant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Lobby extends Minigame {
    public static final Location LOBBY = new Location(Bukkit.getWorld("world"), 0, 1, 0);

    public Lobby() {
        super("Lobby");
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
        updatePlayerTotalScoreDisplay(p);

        p.getPlayer().sendMessage(p.getTeam().teamPlayers.toString());
        displayTeamTotalScore(p.getTeam());
        updateTeamStandings();
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
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getY() < -45){
            e.getPlayer().teleport(LOBBY);
        }
    }

    public void start() {
        MBC.getInstance().setCurrentGame(this);
        createScoreboard();
        stopTimer();
        setTimer(120);
    }

    /**
     * Updates the player's total score in lobby
     * Your Coins: {COIN_AMOUNT}
     * @param p Participant whose scoreboard to update
     */
    public void updatePlayerTotalScoreDisplay(Participant p) {
        createLine(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getRawTotalScore(), p);
    }

    @Override
    public void loadPlayers() {
        // tbd
    }
}
