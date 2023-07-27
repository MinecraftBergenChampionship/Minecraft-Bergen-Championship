package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.entity.Player;

public abstract class GamePlayer {
    private final Participant participant;

    public GamePlayer(Participant p) {
        participant = p;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Player getPlayer() { return participant.getPlayer(); }

    // NOTE: removed this to remove gamePlayers since games may or may not even use gamePlayers, and the ones
    // that do haven't even used the list? this is probably because i didn't realize that existed yet, but even
    // though its probably best to have one centralized list, this is probably easiest atm to just have a storage
    // unique to each type so i don't have to go crazy casting, and handling disconnects for each specific game
    // would be easier
    /**
     * Caller should only call if there is no minigame active, as this will fail.
     * @param p Player whose game player representation is to be found
     * @return GamePlayer instance for Player
     */
    /*
    public static GamePlayer getGamePlayer(Player p) {
        for (GamePlayer x : MBC.getInstance().getGame().gamePlayers) {
            if (x.getParticipant().getPlayer().getName().equals(p.getName())) {
                return x;
            }
        }

        return null;
    }
    */
}
