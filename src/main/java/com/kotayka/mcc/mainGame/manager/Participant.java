package com.kotayka.mcc.mainGame.manager;

import org.bukkit.entity.Player;

public class Participant {
    public int totalCoins;
    public int roundCoins;
    public String team = "Spectator";
    public String fullName;

    public final Player player;
    public String ign;

    public Participant(Player player) {
        this.player = player;
        this.ign = player.getName();
    }
}
