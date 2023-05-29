package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
        //if (!isGameActive()) return;
        if (e.getBlock().getType() == Material.DIAMOND_BLOCK) {
            Participant p = Participant.getParticipant(e.getPlayer());
            assert p != null;
            p.addGameScore(5);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getY() < -45){
            e.getPlayer().teleport(LOBBY);
        }

        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
            ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).setCheckpoint();
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(2));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.JUMP_PAD) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.SPEED_PAD) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
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
