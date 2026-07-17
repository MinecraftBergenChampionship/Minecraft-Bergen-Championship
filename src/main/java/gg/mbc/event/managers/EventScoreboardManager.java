package gg.mbc.event.managers;

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
    public void initializePlayerScoreboard(Player player, EventTeam team) {
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
            thisScoreboardTeam.prefix(team.displayName());
            thisScoreboardTeam.addPlayer(player);
            thisScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } else {
            Team thisScoreboardTeam = board.getTeam(team.name());
            thisScoreboardTeam.addPlayer(player);
            thisScoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        // add everyone else to this player's scoreboard
        /*
        for (Player p : Bukkit.getOnlinePlayers()) {
            EventTeam team = TeamManager.getTeam(player);
            // add everyone else to this player's scoreboard
            if (board.getTeam(p.getTeam().fullName) == null) {
                Team scoreboardTeam = board.registerNewTeam(p.getTeam().fullName);
                scoreboardTeam.setColor(p.getTeam().getChatColor());
                scoreboardTeam.setPrefix(String.format("%s%c ", ChatColor.WHITE, p.getTeam().getIcon()));
                scoreboardTeam.setAllowFriendlyFire(false);
                scoreboardTeam.addPlayer(p.getPlayer());
                scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            } else {
                board.getTeam(p.getTeam().fullName).addPlayer(p.getPlayer());
            }

            if ()
        } */
    }
}
