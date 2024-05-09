package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.PartyGame;
import me.kotayka.mbc.partygames.DiscoFever;
import me.kotayka.mbc.partygames.PartyGameFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Party extends Game {
    private final World world = Bukkit.getWorld("Party");
    protected final Location LOBBY = new Location(world, 0, 0, -1000);

    public Party() {
        super("Party", null);
    }

    @Override
    public void loadPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(LOBBY);
        }
    }

    @Override
    public void events() {

    }

    @Override
    public void createScoreboard(Participant p) {

    }

    @Override
    public void onRestart() {

    }
}
