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
import java.util.*;

public class Party extends Game {
    private final World world = Bukkit.getWorld("Party");
    protected final Location LOBBY = new Location(world, 0, 0, -1000);
    private PartyGameFactory factory = new PartyGameFactory();
    private List<String> gameNames = Arrays.asList("DiscoFever", "BeepTest", "DiscoFever");
    private int gameNum;
    private PartyGame partyGame = null;

    public Party() {
        super("Party", new String[] {
            "⑰ Play three minigames and try to get the highest score!",
            "⑰ Make sure to read the instructions for each game carefully!\n\n" +
            "⑰ Each game has unique gameplay, rules and scoring.",
            "⑰ The three minigames will be chosen from in a random order.\n\n" +
            "⑰ Our three games for this event are " + ChatColor.BOLD + "Disco Fever, Disco Fever, and Beep Test." + ChatColor.RESET,
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

    /**
     * Generates random party game from gameNames.
     * Removes chosen game from gameNames.
     * Returns associated PartyGame.
     */
    public PartyGame getRandomPartyGame() {
        if (gameNames.size() > 0) {
            String randomGame = gameNames.get((int)(Math.random()*gameNames.size()));
            gameNames.remove(randomGame);
            return factory.getPartyGame(randomGame);
        }
        else {
            Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "Oops, this shouldn't have been sent :(\n");
            return null;
        }
        
    }

    /**
     * Does stuff before the first round that is necessary.
     * 
     * 
     */
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
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The first Party Game will be picked shortly...\n");
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
                }
                setGameState(GameState.STARTING);
                timeRemaining = 15;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 10) {
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The next Party Game is...\n");
            }
            if (timeRemaining == 5) {
                partyGame = getRandomPartyGame();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle("\n" + MBC.MBC_STRING_PREFIX + ChatColor.BOLD + partyGame.name() + "!" + ChatColor.RESET, "", 0, 15, 15);
                    p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                }
            }
            if (timeRemaining == 0) {
                setGameState(GameState.ACTIVE);
                gameNum++;
                partyGame.start();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (partyGame.isGameOver()) {
                if (gameNum == 3) {
                    timeRemaining = 37;
                    setGameState(GameState.END_GAME);
                }
                else {
                    timeRemaining = 5;
                    setGameState(GameState.END_ROUND);
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getPlayer().teleport(LOBBY);
                }

            }
        } else if (getState().equals(GameState.END_ROUND)) {
            Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The next Party Game will be picked shortly...");
            if (timeRemaining == 0) {
                setGameState(GameState.STARTING);
                timeRemaining = 15;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 36) {
                gameOverGraphics();
            }
            gameEndEvents();

        }
    }

    @Override
    public void createScoreboard(Participant p) {

    }

    @Override
    public void onRestart() {

    }
}
