package me.kotayka.mbc;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * <b>PartyGame</b> represents a separate, individual game, which may be selected
 * to be played as a part of the game represented in Party.java.
 */
public abstract class PartyGame extends Minigame {
    private final World world = Bukkit.getWorld("Party");
    protected final Location SPAWN;
    protected final String[] INTRODUCTION;
    private int introLine = 0;

    public PartyGame(String name, Location spawn, String[] intro) {
        super(name);
        SPAWN = spawn;
        INTRODUCTION = intro;
    }

    @Override
    public void start() {
        // start registering events for this game
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
    }

    /**
     * Print game explanation, one line at a time.
     *
     * @modifies introLine
     */
    public void Introduction() {
        if (INTRODUCTION == null || introLine > INTRODUCTION.length) return;

        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(INTRODUCTION[introLine++]);
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }

    public World world() {
        return world;
    }
}
