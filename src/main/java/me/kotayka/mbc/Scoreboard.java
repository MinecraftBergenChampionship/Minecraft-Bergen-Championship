package me.kotayka.mbc;

public interface Scoreboard {
    /**
     * Create general scoreboard for game/event for every player
     * @see Scoreboard createScoreboard(Participant p)
     */
    void createScoreboard();

    /**
     * Create general scoreboard for game/event per
     * @param p Participant whose scoreboard to update
     */
    void createScoreboard(Participant p);
}