package com.kotayka.mcc.Skybattle.listeners;

import com.comphenix.net.bytebuddy.build.Plugin;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDie implements Listener {
    public final Skybattle skybattle;
    public final Plugin plugin;

    public PlayerDie(Skybattle skybattle, Plugin plugin) {
        this.skybattle = skybattle;
        this.plugin = plugin;
    }

    /*
     * Spawn firework on death
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {
        Firework fw = new Firework();
        fw.spawnFirework(e.getEntity().getLocation());
        /* TODO: Set death message + scoring */
    }
}
