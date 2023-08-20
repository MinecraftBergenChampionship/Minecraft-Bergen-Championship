package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkybattlePlayer extends GamePlayer {
    // The player that dealt "special damage" (TNT, Creepers, etc). Used to track damage if player falls into the void.
    public Player lastDamager = null;
    public boolean voidDeath = false;
    public int kills = 0;

    public SkybattlePlayer(Participant p) {
        super(p);
    }


}
