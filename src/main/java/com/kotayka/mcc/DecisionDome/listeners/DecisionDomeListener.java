package com.kotayka.mcc.DecisionDome.listeners;

import com.kotayka.mcc.DecisionDome.DecisionDome;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class DecisionDomeListener implements Listener {
    private final DecisionDome decisionDome;

    public DecisionDomeListener(DecisionDome decisionDome) {
        this.decisionDome = decisionDome;
    }

    @EventHandler
    public void eggThrow(ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.EGG) {
            Egg egg = (Egg) event.getEntity();
            Entity chicken = egg.getLocation().getWorld().spawnEntity(egg.getLocation(), EntityType.CHICKEN);
            decisionDome.chickens.add(chicken);
        }
    }

    @EventHandler
    public void eggHatch(PlayerEggThrowEvent event) {
        event.setHatching(false);
    }
}
