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

    /**
     * Updates the player's current coin count in-game on the scoreboard
     * Your Coins: {COIN_AMOUNT}
     * @param p Participant whose scoreboard to update
     */
    void updatePlayerCurrentScoreDisplay(Participant p);

    /**
     * Displays team current coin count in-game on the scoreboard
     * Team Coins: {COIN_AMOUNT}
     * Note: since team scoreboard is always active, this may be redundant.
     * @param t Team whose coin count to display
     */
    void displayTeamCurrentScore(MBCTeam t);

    /**
     * Displays team's total score in lobby
     * Team Coins: {COIN_AMOUNT}
     * Note: Since team scoreboard is always active in lobby, this may be redundant.
     * @param t Team whose coin count to display
     */
    void displayTeamTotalScore(MBCTeam t);
}