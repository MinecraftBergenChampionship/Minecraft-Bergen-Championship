package me.kotayka.mbc;

import java.util.ArrayList;
import java.util.List;

import me.kotayka.mbc.games.Party;
import me.kotayka.mbc.partygames.Dragons;
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
    public World world = Bukkit.getWorld("Party");
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
        /*
        //Debug
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();
        Bukkit.broadcastMessage("In " + this.name() + ".start(), called by " + methodName);
         */

        // probably unnecessary
        if (MBC.getInstance().getMinigame().equals(this)) {
            return;
        }
        // start registering events for this game
        HandlerList.unregisterAll(MBC.getInstance().lobby);
        HandlerList.unregisterAll(MBC.getInstance().decisionDome);
        if (MBC.getInstance().party != null) {
            HandlerList.unregisterAll(MBC.getInstance().party);
        }
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
        MBC.getInstance().setCurrentGame(this);

        // if timer hasn't reached 1, stop it
        stopTimer();

        // standards
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
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
        createLineAll(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, "Party (" + name()) + ")");
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

    public void roundWinners(int winPoints, int survivalPoints) {
        String s;
        if (playersAlive.size() > 1) {
            StringBuilder survivors = new StringBuilder("The winners of this round are: ");
            for (int i = 0; i < playersAlive.size(); i++) {
                Participant p = playersAlive.get(i);
                winEffects(p);
                if (winPoints > 0) {
                    p.getPlayer().sendMessage(ChatColor.GREEN + "You survived the round!" + MBC.scoreFormatter(winPoints));
                    p.addCurrentScore(winPoints);
                } else {
                    p.getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
                }

                // check number of alive teammates
                int count = 0;
                for (Participant teammate : p.getTeam().getPlayers()) {
                    if (teammate.getPlayer().getGameMode() != GameMode.SPECTATOR)
                        count++;
                }

                if (count > 1) {
                    p.getPlayer().sendMessage(ChatColor.GREEN + "You've earned bonus survival points from your teammates!" + MBC.scoreFormatter(survivalPoints * (count-1)));
                    p.addCurrentScore(survivalPoints * (count-1));
                }

                if (i == playersAlive.size()-1) {
                    survivors.append("and ").append(p.getFormattedName());
                } else {
                    survivors.append(p.getFormattedName()).append(", ");
                }
            }
            s = survivors.toString()+ChatColor.WHITE+"!";
        } else if (playersAlive.size() == 1) {
            if (winPoints > 0) {
                playersAlive.getFirst().getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!" + MBC.scoreFormatter(winPoints));
                playersAlive.getFirst().addCurrentScore(winPoints);
            } else {
                playersAlive.getFirst().getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
            }
            winEffects(playersAlive.getFirst());
            s = "The winner of this round is " + playersAlive.getFirst().getFormattedName()+"!";
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
        playersAlive.remove(p);
        checkLastTeam(p.getTeam());
        updatePlayersAliveScoreboard();

        if (playersAlive.isEmpty()) {
            endEvents();
        }
    }

    public void checkLastTeam(MBCTeam t) {
        if (checkTeamEliminated(t)) {
            teamsAlive.remove(t);
            t.announceTeamDeath();
        }
    }

    public abstract void endEvents();

    public void updatePlayersAliveScoreboard() {
        createLineAll(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players: " + ChatColor.RESET+playersAlive.size() + "/"+MBC.getInstance().getPlayers().size() + " | " +
                                    ChatColor.GREEN + "" + ChatColor.BOLD+"Teams: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
        /*
        createLineAll(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.getInstance().getPlayers().size());
        createLineAll(2, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
         */
    }

    /*
     *
     */
    public void setupNext() {
        /*
        //Debug
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();
        Bukkit.broadcastMessage("In " + this.name() + ".setupNext(), called by " + methodName);
         */

        Party party = MBC.getInstance().party;
        if (party == null) return;
        HandlerList.unregisterAll(this);
        setGameState(GameState.INACTIVE);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            if (p.getPlayer().getAllowFlight() && !p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                removeWinEffect(p);
            }
            p.getPlayer().setMaxHealth(20);
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
        }
        MBC.getInstance().party.next();
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
