package me.kotayka.mbc.games;

import me.kotayka.mbc.*;
import me.kotayka.mbc.partygames.PartyGameFactory;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.*;

import java.util.*;

public class Party extends Game {
    private final World world = Bukkit.getWorld("Party");
    protected final Location LOBBY = new Location(world, 0.5, -17.5, -999.5);
    private List<String> gameNames = new ArrayList<>(Arrays.asList("DiscoFever", "BeepTest", "Dragons")); // first is forced to be oneshot
    public static final int GAMES_PLAYED = 3;
    private int gameNum;
    private PartyGame partyGame = null;

    public Party() {
        super("Party", new String[] {
            "⑰ Play three minigames and try to get the highest score!",
            "⑰ Make sure to read the instructions for each game carefully!\n\n" +
            "⑰ Each game has unique gameplay, rules and scoring.",
            "⑰ The three minigames will be chosen from in a random order.\n\n" +
            "⑰ Our four games for this event are " + ChatColor.BOLD + "Dragons, Disco Fever, Beep Test, and One Shot." + ChatColor.RESET,
            ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                    "⑰ You'll find out! "
        });
    }

    @Override
    public void loadPlayers() {
        // standards
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().setLevel(0);
            p.getPlayer().setExp(0);

            // for intro
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 255, true, false));
            p.getPlayer().teleport(LOBBY);
        }
        preFirstRound();
    }

    public void start() {
        super.start();

        setGameState(GameState.TUTORIAL);

        setTimer(32);
    }

    /**
     * Generates random party game from gameNames.
     * Removes chosen game from gameNames.
     *
     * Returns associated PartyGame.
     */
    public PartyGame getRandomPartyGame() {
        if (gameNames.size() > 0) {
            String randomGame = gameNames.get((int)(Math.random()*gameNames.size()));
            gameNames.remove(randomGame);
            return PartyGameFactory.getPartyGame(randomGame);
        } else {
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
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().clear();
            p.getPlayer().setGameMode(GameMode.ADVENTURE);

            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }
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
            if (timeRemaining == 0) {
                gameNum++;
                startPartyGame();
            }
            else if (timeRemaining == 10) {
                if (gameNames.size() == 0) {
                    return;
                }
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The next Party Game is...\n");
                partyGame = getRandomPartyGame();
            }
            else if (timeRemaining == 5) {
                Bukkit.broadcastMessage(ChatColor.BOLD + partyGame.name()+"!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.BOLD + "" + partyGame.name() + "!", "", 0, 80, 20);
                    p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                }
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
        createLine(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, "Party"), p);
        createLine(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET), p);
        //createLineAll(22, ChatColor.GREEN + "Party Round: " + ChatColor.RESET + gameNum + "/3");
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET + ChatColor.RESET.toString(), p);
        updateInGameTeamScoreboard();
    }

    public void startPartyGame() {
        setGameState(GameState.INACTIVE);
        HandlerList.unregisterAll(this);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().setMaxHealth(20);
            p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
            p.getPlayer().setInvulnerable(true);
        }
        partyGame.start();
    }

    public void next() {
        stopTimer();
        setGameState(GameState.INACTIVE);
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
        if (GAMES_PLAYED == gameNum) {
            setGameState(GameState.END_GAME);
            setTimer(37);
        } else {
            setGameState(GameState.STARTING);
            setTimer(12);
        }
    }

    public int getGameNum() {
        return gameNum;
    }
    public void incrementGameNum() {
        gameNum++;
    }

    @Override
    public void onRestart() {

    }
}
