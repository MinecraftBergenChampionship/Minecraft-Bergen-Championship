package gg.mbc.event;
import gg.mbc.EventPlugin;
import gg.mbc.event.managers.EventScoreboardManager;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.teams.TeamType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public final class MBCEvent {
    public final static int MAX_TEAMS = 6;
    public final static int MAX_PLAYERS_PER_TEAM = 4;

    private final EventPlugin plugin;
    private static MBCEvent mbc = null;
    private boolean DEBUG_MODE = true;

    private final EventScoreboardManager scoreboardManager;
    private final TeamManager teamManager;

    // Contains data for all players logged onto the server while plugin is active.
    private final Map<UUID, EventPlayer> playerData;

    private MBCEvent(EventPlugin plugin) {
        this.plugin = plugin;

        // managers
        this.scoreboardManager = new EventScoreboardManager();
        this.teamManager = new TeamManager();

        // event
        playerData = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerData.put(player.getUniqueId(), new EventPlayer(player, teamManager.getTeam(TeamType.SPECTATOR)));
        }
    }

    /**
     * Creates a new instance of this event.
     * Handles initialization of variables for this new instance.
     * @param plugin Representation of this plugin
     */
    public static void createEvent(EventPlugin plugin) {
        if (mbc == null) {
            mbc = new MBCEvent(plugin);
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

    /**
     * Adds the given player to event data registry
     * @param p Instance of player to add to event data
     */
    public void addPlayer(Player p) {
        UUID id = p.getUniqueId();
        if (getPlayer(id) != null) return;
        playerData.put(id, new EventPlayer(p, teamManager.getTeam(TeamType.SPECTATOR)));
    }

    public EventScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public TeamManager getTeamManager() { return teamManager; }

    public EventPlugin getPlugin() {
        return plugin;
    }


    public void debugTeams() {
        Component message = teamManager.debugTeams();
        // String message = PlainTextComponentSerializer.plainText().serialize(teamManager.debugTeams());
        plugin.getComponentLogger().info(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
            }
        }
    }
}
