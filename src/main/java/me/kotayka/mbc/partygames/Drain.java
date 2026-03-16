package me.kotayka.mbc.partygames;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.dragonsMap.Arrgh;
import me.kotayka.mbc.gameMaps.dragonsMap.ConchStreet;
import me.kotayka.mbc.gameMaps.dragonsMap.DragonsMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;

public class Drain extends PartyGame {

    private static Drain instance = null;

    private final Location[] SPAWN = 
    {   new Location(Bukkit.getWorld("Party"), 2000, 102, 1975, 0, 0),
        new Location(Bukkit.getWorld("Party"), 2000, 102, 2025, 180, 0),
        new Location(Bukkit.getWorld("Party"), 2025, 102, 2000, 90, 0),
        new Location(Bukkit.getWorld("Party"), 1975, 102, 2000, -90, 0)
    };


    private final int DRAIN_POINTS_18 = 200;
    private final int DRAIN_POINTS_24 = 320;
    private final int DRAIN_POINTS = DRAIN_POINTS_18;

    private final int PATTERN_POINTS_18 = 100;
    private final int PATTERN_POINTS_24 = 160;
    private final int PATTERN_POINTS = PATTERN_POINTS_18;

    private final int SPECIAL_POINTS_18 = 100;
    private final int SPECIAL_POINTS_24 = 160;
    private final int SPECIAL_POINTS = SPECIAL_POINTS_18;

    private final org.bukkit.World world = Bukkit.getWorld("Party");

    private final List<String> specialtasks = new ArrayList<>(Arrays.asList("onebyone", "mass", "edge", "center", "teamlover", ""));

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new Drain();
        }
        return instance;
    }

    private Drain() {
        super("Drain", new String[]{
                "⑰ In Drain, your goal is to DRAIN the water at the bottom of the map.\n\n" +
                "⑰ By jumping into a water tile, the water will be replaced with one block of your team's wool.\n\n",
                "⑰ Your general goal is to have as many tiles of your team's color as possible.\n\n" +
                "⑰ However, you also get points for constructing patterns and other specific tasks.",
                "⑰ Points are split proportionally based on how much of that task you completed.\n\n" +
                "⑰ The more you can perform a task compared to other teams, the more points you get!",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +180 points, split proportionally by tiles are drained\n" +
                        "⑰ +90 points, split proportionally by patterns created\n" +
                        "⑰ +90 points, split proportionally by specific task completed"});

    }

    @Override
    public void start() {
        super.start();

        teamsAlive.addAll(getValidTeams());

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setInvulnerable(false);
        }

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    @Override
    public void endEvents() {
        

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().stopSound(Sound.MUSIC_DISC_RELIC, SoundCategory.RECORDS);
        }

        logger.logStats();

        if (MBC.getInstance().party == null) {
            for (Participant p : MBC.getInstance().getPlayers()) {
                p.addCurrentScoreToTotal();
            }
            if (MBC.getInstance().gameNum != 6) {
                MBC.getInstance().updatePlacings();
            }
            returnToLobby();
        } else {
            // start next game
            setupNext();
        }

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void loadPlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getInventory().clear();
            p.getPlayer().teleport(SPAWN[(int)(Math.random()*4)]);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(boots));
        }
    }

    public void handleDeath(Player victim, boolean fallDamage) {

        Participant p = Participant.getParticipant(victim);
        
    }


    @Override
    public void events() {
        switch (getState()) {
            case TUTORIAL:
                if (timeRemaining == 0) {
                    MBC.getInstance().sendMutedMessages();
                    setGameState(GameState.STARTING);
                    setTimer(30);
                } else if (timeRemaining % 7 == 0) {
                    Introduction();
                }
                break;
            case STARTING:
                startingCountdown("sfx.starting_beep");
                mapCreator("Drain Map", "bigkirbypuff_");
                if (timeRemaining == 9) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, "sfx.game_starting_jingle", SoundCategory.RECORDS, 1, 1);
                    }
                }
                if (timeRemaining == 0) {
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getPlayer().playSound(p.getPlayer(), Sound.MUSIC_DISC_RELIC, SoundCategory.RECORDS,1,1);
                        p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        
                    }
                    setGameState(GameState.ACTIVE);
                    setTimer(180);
                }
                break;
            case ACTIVE:
                if (timeRemaining == 0) {
                    setGameState(GameState.END_ROUND);
                    setTimer(30);
                }

                break;
            case END_ROUND: 
                break;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i.equals(Material.LEATHER_HELMET)) e.setCancelled(true);
        if (i.equals(Material.LEATHER_CHESTPLATE)) e.setCancelled(true);
        if (i.equals(Material.LEATHER_LEGGINGS)) e.setCancelled(true);
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!isGameActive()) return;

        event.setCancelled(true);
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, "Party (" + name()) + ")", p);
        createLine(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET), p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updateInGameTeamScoreboard();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getPlayer();
        event.setCancelled(true);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!getState().equals(GameState.ACTIVE)) {
            event.setCancelled(true);
            return;
        }
        
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        Participant p = Participant.getParticipant(e.getPlayer());
        Bukkit.broadcastMessage(p.getFormattedName() + " has disconnected!");
        logger.log(p.getPlayerName() + " disconnected!");

    }

    @Override
    public World world() {
        return world;
    }

}
