package gg.mbc.event.players;

import org.bukkit.entity.Player;

/**
 * Represents player data for a given participant.
 */
public class EventPlayer {
    // Player data
    private Player player;

    public EventPlayer(Player player) {
        this.player = player;
    }


    public Player getPlayer() { return player; }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
