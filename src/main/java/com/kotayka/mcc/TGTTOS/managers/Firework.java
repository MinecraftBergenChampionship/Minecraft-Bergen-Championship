package com.kotayka.mcc.TGTTOS.managers;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.FireworkMeta;

public class Firework {

    public void spawnFirework(Location loc) {
        org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(5);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    public void spawnFireworkWithColor(Location loc, Color color) {
        org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(5);
        fwm.addEffect(FireworkEffect.builder().withColor(color).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }
}
