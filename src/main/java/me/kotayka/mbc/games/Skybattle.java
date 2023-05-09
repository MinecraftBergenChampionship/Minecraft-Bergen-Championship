package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.skybattleMap.Classic;
import me.kotayka.mbc.gameMaps.skybattleMap.SkybattleMap;
import me.kotayka.mbc.gamePlayers.SkybattlePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Skybattle extends Game {
    public SkybattleMap map = new Classic(this);
    public List<SkybattlePlayer> skybattlePlayerList = new ArrayList<>();

    public Skybattle() {
        super(3, "Skybattle");
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Game: "+ MBC.gameNum+"/8:" + ChatColor.WHITE + " Sky Battle", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    public void loadPlayers() {
        for (Participant p : MBC.getIngamePlayer()) {
            p.getInventory().clear();

            p.getPlayer().setFlying(false);
            p.getPlayer().setAllowFlight(false);
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setHealth(20);

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 10, false, false));
            skybattlePlayerList.add(new SkybattlePlayer(p));
        }
        map.spawnPlayers();
    }

    @Override
    public void events() {
        setTimer(340);

    }

    @Override
    public void start() {

    }
}
