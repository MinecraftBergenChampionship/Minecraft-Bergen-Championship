package gg.mbc.event.managers;

import gg.mbc.event.MBCEvent;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.teams.EventTeam;
import gg.mbc.event.teams.TeamType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

import static net.kyori.adventure.text.Component.text;

public class TeamManager {
    private final Map<TeamType, EventTeam> teams;
    private final Map<String, EventTeam> teamNames;
    private final Map<UUID, EventTeam> playerTeams;

    public TeamManager() {
        teams = new HashMap<>();
        playerTeams = new HashMap<>();
        teamNames = new HashMap<>();

        for (TeamType type : TeamType.values()) {
            EventTeam newTeam = new EventTeam(type);
            teams.put(type, newTeam);
            teamNames.put(newTeam.scoreboardName(), newTeam);
        }

        // TODO: read from predefined mapping of player teams, if one exists
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerTeams.put(player.getUniqueId(), teams.get(TeamType.SPECTATOR));
        }
    }

    /**
     * Handles changing a player's team.
     * @implNote Scoring has not been implemented yet, but changes to a team's score
     *           are designed to be made through this function.
     * @param player Player that is changing teams.
     * @param newTeam Team that `player` is joining.
     */
    public void changeTeam(Player player, EventTeam newTeam) {
        // TODO: handle scoring changes.
        UUID id = player.getUniqueId();
        EventTeam previous = playerTeams.get(id);
        EventPlayer eventPlayer = MBCEvent.getInstance().getPlayer(id);

        eventPlayer.changeEventName(newTeam);

        // If Player was not previously on a team
        if (previous != null) {
            previous.removePlayer(eventPlayer);
            newTeam.addPlayer(eventPlayer);
            playerTeams.replace(id, previous, newTeam);
        } else {
            newTeam.addPlayer(eventPlayer);
            playerTeams.put(id, newTeam);
        }

        EventScoreboardManager esm = MBCEvent.getInstance().getScoreboardManager();
        esm.initializePlayerScoreboard(player, newTeam, this);
    }

    /**
     * Handles changing a player's team given the String name of the team.
     * @implNote Scoring has not been implemented yet, but changes to a team's score
     *           are designed to be made through this function.
     * @param player Player that is chasnging teams.
     * @param name Name of the requested team to join
     * @see gg.mbc.commands.changeteam
     * @see TeamManager::changeTeam(Player, EventTeam)
     * @return success if the name was a valid team name
     */
    public boolean changeTeam(Player player, String name) {
        EventTeam team = getTeam(name);
        if (team == null) {
            return false;
        }
        changeTeam(player, team);
        Bukkit.broadcast(text(player.getName(), NamedTextColor.GOLD).append(text(" has joined the ", NamedTextColor.WHITE)).append(team.displayName()));
        return true;
    }

    /**
     * Get Team by player information.
     */
    public EventTeam getTeam(UUID id) { return playerTeams.get(id); }
    public EventTeam getTeam(Player player) { return playerTeams.get(player.getUniqueId()); }

    /**
     * Return corresponding instance of a given team type t.
     * @param t TeamType to retrieve the team instance of.
     * @return EventTeam corresponding with the given TeamType t.
     */
    public EventTeam getTeam(TeamType t) {
        return teams.get(t);
    }

    /**
     * Get EventTeam instance from team name
     * @param s String representing the EventTeam.
     * @return EventTeam instance corresponding with given String, or null if none is found.
     */
    public EventTeam getTeam(String s) {
        if (teamNames.containsKey(s)) {
            return teamNames.get(s);
        }
        for (EventTeam team : teams.values()) {
            if (team.name().equalsIgnoreCase(s) || team.scoreboardName().equalsIgnoreCase(s)) {
                return team;
            }
        }
        return null;
    }

    /**
     * @return all TeamTypes in String format as an iterable collection.
     */
    public Collection<String> getTypes() {
        return Collections.unmodifiableCollection(teamNames.keySet());
    }

    /**
     * Admin Debug Info Function
     * @return Component representing debug message
     */
    public Component debugTeams() {
        Component debugMsg = text("\n[BEGIN DEBUG MESSAGE]---------------------------\n")
                .append(text("There are " + teams.size() + " teams:\n"));
        for (EventTeam team : teams.values()) {
            debugMsg = debugMsg.append(team.displayName().append(
                    text(String.format(" has %d player%s!\n", team.players().size(), team.players().size() == 1 ? "" : "s"))));
            for (EventPlayer player : team.players()) {
                debugMsg = debugMsg.append(text("- ").append(player.getEventName()).append(text("\n")));
            }
        }
        debugMsg = debugMsg.append(text("[END DEBUG MESSAGE]------------------------------"));
        return debugMsg;
    }
}
