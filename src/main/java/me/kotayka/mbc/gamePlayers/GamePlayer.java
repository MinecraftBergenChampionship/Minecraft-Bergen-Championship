package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class GamePlayer {

    private Participant participant;

    public GamePlayer(Participant p) {
        participant = p;
        MBC.getInstance().currentGame.gamePlayers.add(this);
    }

    public Participant getParticipant() {
        return participant;
    }

    public Player getPlayer() { return participant.getPlayer(); }

    public static GamePlayer getGamePlayer(Player p) {
        for (GamePlayer x : MBC.getInstance().currentGame.gamePlayers) {
            if (x.getParticipant().getPlayer() == p) {
                return x;
            }
        }

        return null;
    }
}
