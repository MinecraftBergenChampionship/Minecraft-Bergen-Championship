package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.entity.Player;

public abstract class GamePlayer {

    private final Participant participant;

    public GamePlayer(Participant p) {
        participant = p;
        MBC.getInstance().getGame().gamePlayers.add(this);
    }

    public Participant getParticipant() {
        return participant;
    }

    public Player getPlayer() { return participant.getPlayer(); }

    /**
     * Caller should only call if there is no minigame active, as this will fail.
     * @param p Player whose game player representation is to be found
     * @return GamePlayer instance for Player
     */
    public static GamePlayer getGamePlayer(Player p) {
        for (GamePlayer x : MBC.getInstance().getGame().gamePlayers) {
            if (x.getParticipant().getPlayer() == p) {
                return x;
            }
        }

        return null;
    }
}
