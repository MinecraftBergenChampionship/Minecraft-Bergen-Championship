package me.kotayka.mbc.games;

import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Minigame;
import me.kotayka.mbc.Participant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Lobby extends Minigame {
    public static final Location LOBBY = new Location(Bukkit.getWorld("world"), 0, 1, 0);

    public Lobby() {
        super("Lobby");
    }

    public void createScoreboard(Participant p) {
        Bukkit.broadcastMessage("This is a test");

        newObjective(p);
        createLine(21, ChatColor.RED+""+ChatColor.BOLD + "Event begins in:", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:", p);
        createLine(17, p.getTeam().getChatColor()+p.getTeam().getTeamFullName(), p);
        createLine(16, ChatColor.RESET+ChatColor.RESET.toString()+ChatColor.RESET, p);
        createLine(15, ChatColor.GREEN+"Team Leaderboard: ", p);
        createLine(4, ChatColor.RESET.toString()+ChatColor.RESET, p);
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
                Bukkit.broadcastMessage(ChatColor.RED+"10 seconds left");
                break;
            case 0:
               toVoting();
               break;
        }
    }

    public void toVoting() {
        HandlerList.unregisterAll(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().decisionDome, MBC.getInstance().plugin);
        MBC.getInstance().decisionDome.start();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.getPlayer().getWorld().equals(Bukkit.getWorld("world"))) return;
        if (e.getPlayer().getLocation().getY() < -45){
            e.getPlayer().teleport(LOBBY);
        }
    }

    @Override
    public void start() {
        MBC.getInstance().setCurrentGame(this);
        createScoreboard();
        stopTimer();
        setTimer(120);
        setGameState(GameState.ACTIVE);
    }


    /**
     * Updates the player's total score in lobby
     * Your Coins: {COIN_AMOUNT}
     * @param p Participant whose scoreboard to update
     */
    public void updatePlayerTotalScoreDisplay(Participant p) {
        createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getRawTotalScore(), p);
    }

    @Override
    public void loadPlayers() {
        // tbd
    }
}
