package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.PartyGame;
import me.kotayka.mbc.partygames.DiscoFever;
import me.kotayka.mbc.partygames.BeepTest;
import me.kotayka.mbc.partygames.PartyGameFactory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

public class Party extends Game {
    private final World world = Bukkit.getWorld("Party");
    protected final Location LOBBY = new Location(world, 0, 0, -1000);
    private PartyGameFactory factory = new PartyGameFactory();
    private int gameNum;

    public Party() {
        super("Party", new String[] {
            "⑰ Play three minigames and try to get the highest score!",
            "⑰ Make sure to read the instructions for each game carefully!\n\n" +
            "⑰ Each game has unique gameplay, rules and scoring.",
            "⑰ The three minigames will be chosen from in a random order.",
            ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                    "⑮ You'll find out! "
    });
    }

    @Override
    public void loadPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(LOBBY);
        }
    }

    public void start() {
        super.start();

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    public PartyGame getRandomPartyGame() {
        return factory.getPartyGame(factory.getRandomGame());
    }

    public void preFirstRound() {
        gameNum = 0;
        createLineAll(21, ChatColor.GREEN + "Game: " + ChatColor.RESET + gameNum + "/3");
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().clear();
            p.getPlayer().setGameMode(GameMode.ADVENTURE);

            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }
            ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));
        }
    }

    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                MBC.getInstance().sendMutedMessages();
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The game is starting!\n");
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
                }
                setGameState(GameState.STARTING);
                timeRemaining = 15;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {

        }
    }

    @Override
    public void createScoreboard(Participant p) {

    }

    @Override
    public void onRestart() {

    }
}
