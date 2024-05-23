package me.kotayka.mbc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * <b>PartyGame</b> represents a separate, individual game, which may be selected
 * to be played as a part of the game represented in Party.java.
 */
public abstract class PartyGame extends Game {
    private final World world = Bukkit.getWorld("Party");
    private int introLine = 0;
    private boolean isGameOver = false;
    public List<Participant> playersAlive = new ArrayList<>();
    public List<MBCTeam> teamsAlive = new ArrayList<>();

    public PartyGame(String name, String[] intro) {
        super(name, intro);
    }

    /**
     * Starts the minigame.
     * Unregisters events for other games and resets timer.
     * Several scoreboard lines are created TODO move them to another function for clarity
     */
    @Override
    public void start() {
        // start registering events for this game
        HandlerList.unregisterAll(MBC.getInstance().lobby);
        HandlerList.unregisterAll(MBC.getInstance().decisionDome);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);

        // if timer hasn't reached 1, stop it
        stopTimer();

        // standards
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().setLevel(0);
            p.getPlayer().setExp(0);

            // for intro
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 255, true, false));
        }
        loadPlayers();
        createScoreboard();
        createLineAll(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, "Party! (" + name()) + ")");
        createLineAll(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET));
    }

    /**
     * Print game explanation, one line at a time.
     *
     * @modifies introLine
     */
    public void Introduction() {
        if (INTRODUCTION == null || introLine >= INTRODUCTION.length) return;

        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(INTRODUCTION[introLine++]);
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }

    public boolean checkTeamEliminated(MBCTeam team) {
        int deadPlayers = 0;
        for (Participant p : team.teamPlayers) {
            if (checkIfDead(p)) {
                deadPlayers++;
            }
        }

        return deadPlayers == team.teamPlayers.size();
    }

    public void roundWinners(int points) {
        String s;
        if (playersAlive.size() > 1) {
            StringBuilder survivors = new StringBuilder("The winners of this round are: ");
            for (int i = 0; i < playersAlive.size(); i++) {
                Participant p = playersAlive.get(i);
                winEffects(p);
                p.getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
                if (points > 0) {
                    p.addCurrentScore(points);
                }

                if (i == playersAlive.size()-1) {
                    survivors.append("and ").append(p.getFormattedName());
                } else {
                    survivors.append(p.getFormattedName()).append(", ");
                }
            }
            s = survivors.toString()+ChatColor.WHITE+"!";
        } else if (playersAlive.size() == 1) {
            playersAlive.get(0).getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
            playersAlive.get(0).addCurrentScore(points);
            winEffects(playersAlive.get(0));
            s = "The winner of this round is " + playersAlive.get(0).getFormattedName()+"!";
        } else {
            s = "Nobody survived the round.";
        }
        logger.log(s+"\n");
        Bukkit.broadcastMessage(s);
    }

    public void winEffects(Participant p) {
        p.getPlayer().setInvulnerable(true);
        MBC.spawnFirework(p);
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(true);
        p.getPlayer().setFlying(true);
    }

    public boolean checkIfDead(Participant p) {
        return !playersAlive.contains(p);
    }

    public boolean checkIfAlive(Participant p) {
        return playersAlive.contains(p);
    }

    public void updatePlayersAlive(Participant p) {
        Bukkit.broadcastMessage(p.getFormattedName() + " partied too hard");
        playersAlive.remove(p);
        checkLastTeam(p.getTeam());
        updatePlayersAliveScoreboard();

        if (playersAlive.size() == 0) {
            timeRemaining = 1;
        }
    }

    public void checkLastTeam(MBCTeam t) {
        if (checkTeamEliminated(t)) {
            teamsAlive.remove(t);
            t.announceTeamDeath();
        }
    }

    public void updatePlayersAliveScoreboard() {
        createLineAll(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players: " + ChatColor.RESET+playersAlive.size() + "/"+MBC.getInstance().getPlayers().size() + " | " +
                                    ChatColor.GREEN + "" + ChatColor.BOLD+"Teams: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
        /*
        createLineAll(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.getInstance().getPlayers().size());
        createLineAll(2, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
         */
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void gameOver() {
        isGameOver = true;
    }

    public World world() {
        return world;
    }
}
