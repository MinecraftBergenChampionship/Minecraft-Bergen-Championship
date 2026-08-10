package gg.mbc.event.managers;

import gg.mbc.event.MBCEvent;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.teams.EventTeam;
import gg.mbc.event.teams.TeamType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

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
        // If Player was not previously on a team
        if (previous != null) {
            previous.removePlayer(eventPlayer);
            newTeam.addPlayer(eventPlayer);
            playerTeams.replace(id, previous, newTeam);
        } else {
            newTeam.addPlayer(eventPlayer);
            playerTeams.put(id, newTeam);
        }
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
        Bukkit.broadcast(Component.text(player.getName()).append(Component.text(" has joined the ")).append(team.displayName()));
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

    public String debugTeams() {
        StringBuilder debugMsg = new StringBuilder();
        debugMsg.append("[BEGIN DEBUG MESSAGE]---------------------------\n")
                .append(String.format("There are %d teams:\n", teams.size()));
        for (EventTeam team : teams.values()) {
            debugMsg.append(String.format("\t- %s\n", team.displayName()))
                    .append(String.format("\tTeam %s has %d players:\n", team.name(), team.players().size()));
            for (EventPlayer player : team.players()) {
                debugMsg.append(String.format("\t\t- %s\n", player.getEventName().toString()));
            }
        }
        debugMsg.append("[END DEBUG MESSAGE]------------------------------\n");

        return debugMsg.toString();
    }
}
