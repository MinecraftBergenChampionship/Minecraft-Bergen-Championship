package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class OneShotPlayer extends GamePlayer{

    public int kills = 0;
    public int streak = 0;
    
    public OneShotPlayer(Participant p) {
        super(p);
    }

    public int getKills() {
        return kills;
    }
}
