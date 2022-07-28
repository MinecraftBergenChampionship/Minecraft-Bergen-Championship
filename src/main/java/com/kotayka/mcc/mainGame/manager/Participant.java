package com.kotayka.mcc.mainGame.manager;

import org.bukkit.ChatColor;
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

    public void Die(Player player, Player killer) {
        killer.sendTitle("\n", "[X] " + player.getName(), 0, 60, 40);
        player.sendMessage(ChatColor.RED + "You were eliminated by " + killer.getName() + "!");
        killer.sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + player.getName() + "!");
    }
    public void setTeam(String teamName) {
        team = teamName;
    }
}
