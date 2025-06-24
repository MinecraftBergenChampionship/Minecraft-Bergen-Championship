package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class LockdownPlayer extends GamePlayer {
    // The player that dealt "special damage" (TNT, Creepers, etc). Used to track damage if player falls into the void.
    public Player lastDamager = null;
    public int kills = 0;
    private boolean potion = true;

    public LockdownPlayer(Participant p) {
        super(p);
    }

    public boolean hasPotion() { return potion; }
    public void setPotion(boolean b) { potion = b; }

}
