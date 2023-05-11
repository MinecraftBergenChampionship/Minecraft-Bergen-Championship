package me.kotayka.mbc;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;

public interface Scoreboard {
    void createScoreboard();
    void createScoreboard(Participant p);
    void updatePlayerRoundScore(Participant p);
    void updatePlayerGameScore(Participant p);
    void updateTeamRoundScore(Team t);
    void updateTeamGameScore(Team t);
}
