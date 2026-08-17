package gg.mbc.event.managers;

import gg.mbc.event.teams.EventTeam;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import static net.kyori.adventure.text.Component.text;

public final class EventScoreboardManager {
    private final String HEALTH_SCOREBOARD_NAME = "showhealth";

    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();

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
        Scoreboard board = scoreboardManager.getNewScoreboard();
        player.setScoreboard(board);

        // add player to health scoreboard
        if (board.getObjective(HEALTH_SCOREBOARD_NAME) == null) {
            Objective h = board.registerNewObjective(HEALTH_SCOREBOARD_NAME, Criteria.HEALTH, text("❤").color(NamedTextColor.RED));
            h.setDisplaySlot(DisplaySlot.BELOW_NAME);
        } else {
            Objective h = board.getObjective(HEALTH_SCOREBOARD_NAME);
            h.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        // Setup team scoreboard display
        // Add to own scoreboard
        if (board.getTeam(team.scoreboardName()) == null) {
            Team thisScoreboardTeam = board.registerNewTeam(team.scoreboardName());
            thisScoreboardTeam.color(team.textColor());
            thisScoreboardTeam.prefix(text(team.icon(), NamedTextColor.WHITE).append(text(" ")));
            thisScoreboardTeam.setAllowFriendlyFire(false);
            thisScoreboardTeam.addPlayer(player);
            thisScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } else {
            Team thisScoreboardTeam = board.getTeam(team.scoreboardName());
            thisScoreboardTeam.addPlayer(player);
            thisScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        // add everyone else to this player's scoreboard
        for (Player player2 : Bukkit.getOnlinePlayers()) {
            if (player2.getUniqueId() == player.getUniqueId()) continue;
            EventTeam otherTeam = teamManager.getTeam(player2);
            if (otherTeam == null) continue;
            String otherTeamName = otherTeam.scoreboardName();
            if (board.getTeam(otherTeamName) == null) {
                Team scoreboardTeam = board.registerNewTeam(otherTeamName);
                scoreboardTeam.color(otherTeam.textColor());
                scoreboardTeam.prefix(text(otherTeam.icon(), NamedTextColor.WHITE).append(text(" ")));
                scoreboardTeam.setAllowFriendlyFire(false);
                scoreboardTeam.addPlayer(player2);
                scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            } else {
                board.getTeam(otherTeamName).addPlayer(player2);
            }

            // adds this player to everyone else's teams
            Scoreboard secondScoreboard = player2.getScoreboard();
            if (secondScoreboard.getTeam(team.scoreboardName()) == null) {
                Team otherScoreboardTeam = secondScoreboard.registerNewTeam(team.scoreboardName());
                otherScoreboardTeam.color(team.textColor());
                otherScoreboardTeam.prefix(text(team.icon(), NamedTextColor.WHITE).append(text(" ")));
                otherScoreboardTeam.setAllowFriendlyFire(false);
                otherScoreboardTeam.addPlayer(player);
                otherScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            } else {
                // add player to team
                Team otherScoreboardTeam = secondScoreboard.getTeam(team.scoreboardName());
                otherScoreboardTeam.addPlayer(player);
                otherScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }

        }
    }
}
