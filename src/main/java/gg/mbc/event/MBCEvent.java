package gg.mbc.event;
import gg.mbc.event.managers.EventScoreboardManager;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.players.EventPlayer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class MBCEvent {
    public final static int MAX_TEAMS = 6;
    public final static int MAX_PLAYERS_PER_TEAM = 4;

    private final Plugin plugin;
    private static MBCEvent mbc = null;
    private boolean DEBUG_MODE = true;

    private final EventScoreboardManager scoreboardManager;
    private final TeamManager teamManager;

    // Contains data for all players logged onto the server while plugin is active.
    private final Map<UUID, EventPlayer> playerData;

    private MBCEvent(Plugin plugin) {
        this.plugin = plugin;

        // managers
        this.scoreboardManager = new EventScoreboardManager();
        this.teamManager= new TeamManager();

        // event
        playerData = new HashMap<>();
    }

    /**
     * Creates a new instance of this event.
     * Handles initialization of variables for this new instance.
     * @param plugin Representation of this plugin
     */
    public static void createEvent(Plugin plugin) {
        mbc = new MBCEvent(plugin);
    }

    void resetPlayerStatus() {
        // Reset all player status
        for (Player player : Bukkit.getOnlinePlayers()) {
            getInstance().playerData.put(player.getUniqueId(), new EventPlayer(player));
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
            player.setInvulnerable(false);

            for (Player player2 : Bukkit.getOnlinePlayers()) {
                if (player2.getUniqueId() == player.getUniqueId()) continue;
                player.showPlayer(plugin, player2);
            }
        }

    }

    /**
     * Stops the event and destroys this current instance;
     */
    public static void stopEvent() {
        mbc = null;
    }

    /**
     * Instance getter for this Singleton class.
     * @throws RuntimeException If there is no active event
     */
    public static MBCEvent getInstance() {
        if (mbc == null) {
            throw new RuntimeException("No event is active.");
        }

        return mbc;
    }

    /**
     * Returns the instance of EventPlayer data for a given UUID.
     * If no matching id is found, returns null.
     * @return EventPlayer representation of the player with a specified UUID.
     */
    public EventPlayer getPlayer(UUID id) {
        return playerData.get(id);
    }
}
