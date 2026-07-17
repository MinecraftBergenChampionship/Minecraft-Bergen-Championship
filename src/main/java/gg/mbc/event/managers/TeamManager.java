package gg.mbc.event.managers;

import gg.mbc.event.teams.EventTeam;
import gg.mbc.event.teams.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private final Map<TeamType, EventTeam> teams;
    private final Map<UUID, EventTeam> playerTeams;

    public TeamManager() {
        teams = new HashMap<>();
        playerTeams = new HashMap<>();

        for (TeamType type : TeamType.values()) {
            teams.put(type, new EventTeam(type));
        }

        // TODO: read from predefined mapping of player teams, if one exists
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerTeams.put(player.getUniqueId(), teams.get(TeamType.SPECTATOR));
        }
    }

    /**
     * @return EventTeam corresponding with the given TeamType t.
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
}
