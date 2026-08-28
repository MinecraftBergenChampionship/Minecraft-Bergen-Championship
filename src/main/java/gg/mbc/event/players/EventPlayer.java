package gg.mbc.event.players;

import gg.mbc.util.MBCUtils;
import gg.mbc.event.teams.EventTeam;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Represents player data for a given participant.
 */
public class EventPlayer {
    // Player data
    private Player player;

    // display
    private Component displayName;

    public EventPlayer(Player player, EventTeam team) {
        this.player = player;
        this.displayName = MBCUtils.getDisplayName(team, player.getName());
    }

    /**
     * @return Instance of Player class associated with this player's event data
     */
    public Player getPlayer() { return player; }

    /**
     * Used to re-bind EventPlayer data with this instance of the Player class.
     * Usually done after a player has re-logged into the game.
     * @param p Instance of the player class associated with this player's event data
     */
    public void setPlayer(Player p) {
        this.player = p;
    }

    /**
     * Returns the player's formatted name.
     * This does not directly return the player's username.
     * @return Player's username formatted for the event.
     */
    public Component getEventName() {
        return displayName;
    }

    /**
     * Change this player's name display, as it is formatted.
     */
    public void changeEventName(EventTeam newTeam) {
        displayName = MBCUtils.getDisplayName(newTeam, player.getName());
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
