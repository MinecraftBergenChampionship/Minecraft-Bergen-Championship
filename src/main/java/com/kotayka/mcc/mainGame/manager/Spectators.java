package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.TGTTOS.managers.Firework;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Spectators {
    public final Plugin plugin;
    List<Player> spectators = new ArrayList<Player>();

    public Spectators(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addSpectator(Player p) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                p.setGameMode(GameMode.SPECTATOR);
                spectators.add(p);
            }
        }, 0);

    }

    public boolean checkIfSpectator(Player p) {
        if (spectators.contains(p)) {
            return true;
        }
        return false;
    }

    public void removeAllSpectators() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                for (Player spec : spectators) {
                    spec.setGameMode(GameMode.ADVENTURE);
                }
            }
        }, 0);

    }

}
