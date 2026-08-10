package gg.mbc.event.managers;

import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import gg.mbc.event.MBCEvent;
import gg.mbc.event.teams.EventTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

public final class EventScoreboardManager {
    private final String HEALTH_SCOREBOARD_NAME = "showhealth";

    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private final HashMap<Player, Scoreboard> playerScoreboards = new HashMap<>();

    public EventScoreboardManager() {

    }

    /**
     * Initialize internal scoreboards for each player.
     * @param player Player to initialize scoreboard for.
     * @param team Team the player is currently on.
     */
    @SuppressWarnings("null")
    public void initializePlayerScoreboard(Player player, EventTeam team, TeamManager teamManager) {
        // Create board data for player if necessary
        Scoreboard board = playerScoreboards.get(player);
        if (board == null) {
            board = scoreboardManager.getNewScoreboard();
            playerScoreboards.put(player, board);
        }

        // add player to health scoreboard
        if (board.getObjective(HEALTH_SCOREBOARD_NAME) == null) {
            Objective h = board.registerNewObjective(HEALTH_SCOREBOARD_NAME, Criteria.HEALTH, Component.text("❤").color(NamedTextColor.RED));
            h.setDisplaySlot(DisplaySlot.BELOW_NAME);
        } else {
            Objective h = board.getObjective(HEALTH_SCOREBOARD_NAME);
            h.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        // Setup team scoreboard display
        // Add to own scoreboard
        if (board.getTeam(team.scoreboardName()) == null) {
            Team thisScoreboardTeam = board.registerNewTeam(team.name());
            thisScoreboardTeam.color(team.textColor());
            thisScoreboardTeam.prefix(Component.text(team.icon(), NamedTextColor.WHITE).append(Component.text(" ")));
            thisScoreboardTeam.addPlayer(player);
            thisScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } else {
            Team thisScoreboardTeam = board.getTeam(team.name());
            thisScoreboardTeam.addPlayer(player);
            thisScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        // add everyone else to this player's scoreboard
        for (Player p : Bukkit.getOnlinePlayers()) {
            EventTeam otherTeam = teamManager.getTeam(player);
            String otherTeamName = otherTeam.scoreboardName();
            if (board.getTeam(otherTeamName) == null) {
                Team scoreboardTeam = board.registerNewTeam(otherTeamName);
                scoreboardTeam.color(otherTeam.textColor());
                scoreboardTeam.prefix(Component.text(otherTeam.icon(), NamedTextColor.WHITE).append(Component.text(" ")));
                scoreboardTeam.setAllowFriendlyFire(false);
                scoreboardTeam.addPlayer(p);
                scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            } else {
                board.getTeam(otherTeamName).addPlayer(p);
            }

        }
    }
}
