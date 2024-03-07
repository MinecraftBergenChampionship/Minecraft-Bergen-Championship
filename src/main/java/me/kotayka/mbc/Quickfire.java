package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TeamScoreSorter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public class Quickfire extends FinaleGame {
    public static final ItemStack CROSSBOW = new ItemStack(Material.CROSSBOW);
    public static final ItemStack BOOTS = new ItemStack(Material.LEATHER_BOOTS);
    public static final int MAX_DIST_FROM_CENTER = 13225;
    private World world = Bukkit.getWorld("Quickfire");
    private final Location TEAM_ONE_SPAWN = new Location(world, 19.5, -60, 0);
    private final Location TEAM_TWO_SPAWN = new Location(world, -19.5,  -60, 0);
    private final Location SPAWN = new Location(world, 1.5, -35, 0.5);
    private int[] playersAlive;
    private int timeElapsed = 0;
    private int roundNum = 0;
    private boolean disconnect = false;
    public Quickfire() {
        super("Quickfire", new String[] {
                "Welcome to the Finale of MBC; Quickfire!\n Only the top two teams will play this round.\n",
                "Quickfire is a shooter game where every player has a crossbow, infinite arrows, and 4 hearts.\n",
                "Shooting another player will deal exactly one heart of health.\n" + ChatColor.RED + "Shoot someone four times to eliminate them.\n",
                "Eliminate the entire opposing team to win a round.\n" + ChatColor.YELLOW + "Quickfire is best of 5 rounds.\n",
                "Once the timer exceeds a minute and a half, all players will receive the " + ChatColor.BOLD + "glowing" + ChatColor.RESET + " effect.\n",
                "Each crossbow has the " + ChatColor.BOLD + "Quick Charge" + ChatColor.RESET + " enchantment, so be sure to fire fast!\n",
                "No points are awarded for this game.\n" + ChatColor.YELLOW + "The winning team will win the Minecraft Bruh Championship!\n"
        });

        /*
        for (Participant p : firstPlace.teamPlayers) {
            teamOnePlayers.add(new DodgeboltPlayer(p, true));
        }
        for (Participant p : secondPlace.teamPlayers) {
            teamTwoPlayers.add(new DodgeboltPlayer(p, false));
        }
         */

        ItemMeta bowMeta = CROSSBOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        CROSSBOW.setItemMeta(bowMeta);

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
    }

    public Quickfire(@NotNull MBCTeam firstPlace, @NotNull MBCTeam secondPlace) {
        super("Quickfire", firstPlace, secondPlace, new String[] {
                "Welcome to the Finale of MBC; Quickfire!\n Only the top two teams will play this round.\n",
                "Quickfire is a shooter game where every player has a crossbow, infinite arrows, and 4 hearts.\n",
                "Shooting another player will deal exactly one heart of health.\n" + ChatColor.RED + "Shoot someone four times to eliminate them.\n",
                "Eliminate the entire opposing team to win a round.\n" + ChatColor.YELLOW + "Quickfire is best of 5 rounds.\n",
                "Once the timer exceeds a minute and a half, all players will receive the " + ChatColor.BOLD + "glowing" + ChatColor.RESET + " effect.\n",
                "Each crossbow has the " + ChatColor.BOLD + "Quick Charge" + ChatColor.RESET + " enchantment, so be sure to fire fast!\n",
                "No points are awarded for this game.\n" + ChatColor.YELLOW + "The winning team will win the Minecraft Bruh Championship!\n"
        });

        ItemMeta bowMeta = CROSSBOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        CROSSBOW.setItemMeta(bowMeta);
        CROSSBOW.addEnchantment(Enchantment.QUICK_CHARGE, 3);


        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
    }

    @Override
    public void start() {
        loadPlayers();
        setGameState(GameState.TUTORIAL);
        setTimer(53);
    }

    @Override
    public void loadPlayers() {
        if (firstPlace == null) return;

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().setGameMode(GameMode.SPECTATOR);
            p.getPlayer().getInventory().clear();
            p.getPlayer().teleport(SPAWN);
        }
    }

    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                Barriers(false);
                setGameState(GameState.STARTING);
            } else {
                startingCountdown();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            createLineAll(20, ChatColor.RED.toString() + ChatColor.BOLD + "Time: " + getFormattedTime(timeElapsed));
            if (timeElapsed == 90) {
                for (Participant p : firstPlace.teamPlayers) {
                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100000, 2, false, false));
                }
                for (Participant p : secondPlace.teamPlayers) {
                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100000, 2, false, false));
                }
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + ChatColor.BOLD + " All players are now glowing!");
            }
            timeElapsed++;
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 10;
            } else if (timeRemaining == 5) {
                resetMap();
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 0) {
                returnToLobby();
            }
        }
    }

    public void startRound() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getInventory().clear();
            p.getPlayer().setInvulnerable(false);
            if (p.getTeam().equals(firstPlace)) {
                p.getPlayer().setMaxHealth(8);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getInventory().addItem(CROSSBOW);
                p.getInventory().setBoots(firstPlace.getColoredLeatherArmor(BOOTS));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(TEAM_ONE_SPAWN);
            } else if (p.getTeam().equals(secondPlace)) {
                p.getPlayer().setMaxHealth(8);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getInventory().addItem(CROSSBOW);
                p.getInventory().setBoots(secondPlace.getColoredLeatherArmor(BOOTS));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(TEAM_TWO_SPAWN);
            } else {
                p.getPlayer().setGameMode(GameMode.SPECTATOR);
                p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }

    private void endRound() {
        timeElapsed = 0;
        createScoreboard();
        for (Participant p : firstPlace.teamPlayers) {
            p.getPlayer().setInvulnerable(true);
        }
        for (Participant p : secondPlace.teamPlayers) {
            p.getPlayer().setInvulnerable(true);
        }
        playersAlive[0] = firstPlace.teamPlayers.size();
        playersAlive[1] = secondPlace.teamPlayers.size();
        createLineAll(21, ChatColor.RED.toString() + ChatColor.BOLD + "Next Round:");
        roundNum++;
        if (disconnect) {
            Bukkit.broadcastMessage("Event Paused!");
            setGameState(GameState.PAUSED);
        } else {
            setGameState(GameState.END_ROUND);
            setTimer(6);
        }
    }

    @EventHandler
    public void arrowHitWall(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (e.getHitEntity() != null) return;

        if (e.getHitBlock() != null && e.getHitBlockFace() != null) {
            arrow.remove();
        }
    }

    @EventHandler
    public void arrowHitPlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Participant victim = Participant.getParticipant((Player) e.getEntity());
        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        Participant shooter = Participant.getParticipant((Player) arrow.getShooter());
        e.setDamage(2);

        if (victim.getPlayer().getHealth() <= 2) {
            Death(victim, shooter);
            arrow.remove();
            e.setCancelled(true);
        } else {
            shooter.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(victim.getFormattedName() + " - " + ChatColor.RED + victim.getPlayer().getHealth() + " ♥"));
            for (Participant p : MBC.mbc.getPlayersAndSpectators()) {
                if (!(p.getTeam().equals(firstPlace)) && !(p.getTeam().equals(secondPlace))) {
                    p.getPlayer().sendMessage(shooter.getFormattedName() + ChatColor.RED + " has shot " + victim.getFormattedName() + "!");
                }
            }
        }
    }

    private void Death(Participant victim, Participant killer) {
        MBC.spawnFirework(victim);
        killer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(victim.getFormattedName() + " - " + ChatColor.RED + "0 ♥"));
        Bukkit.broadcastMessage(victim.getFormattedName() + " was shot by " + killer.getFormattedName());

        if (victim.getTeam().equals(firstPlace)) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
                // TODO, possibly: set spectator to another player on their team
            } else {
                score[1]++;
                if (score[1] == 3) endGame(secondPlace);
                else endRound();
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
                // TODO, possibly: set spectator to another player on their team
            } else {
                score[0]++;
                if (score[0] == 3) endGame(firstPlace);
                else endRound();
            }
        }
    }

    private void endGame(MBCTeam t) {
        Bukkit.broadcastMessage("\n" + t.teamNameFormat() + " win MBC!\n");
        for (Player p : Bukkit.getOnlinePlayers()) {
            createScoreboard();
            p.sendTitle(t.teamNameFormat() + " win MBC!", " ", 0, 100, 20);
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.playSound(p, Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 1);
            p.setInvulnerable(true);
        }
        for (Participant p : t.teamPlayers) {
            p.winner = true;
        }
        setGameState(GameState.END_GAME);
        createLineAll(21, ChatColor.RED.toString()+ChatColor.BOLD+"Back to lobby:");
        setTimer(13);
    }

    private void returnToLobby() {
        HandlerList.unregisterAll(this);
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
            p.getPlayer().setInvulnerable(true);
        }

        MBC.getInstance().lobby.end();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (p == null) return;

        disconnect = true;
        Bukkit.broadcastMessage(p.getFormattedName() + " disconnected!");
        if (p.getTeam().equals(firstPlace)) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
            } else {
                score[1]++;
                if (score[1] == 3) endGame(secondPlace);
                else endRound();
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
            } else {
                score[0]++;
                if (score[0] == 3) endGame(firstPlace);
                else endRound();
            }

        }
    }

    public void resetMap() {
        Barriers(true);
        for (Arrow a : world.getEntitiesByClass(Arrow.class)) {
            a.remove();
        }
    }

    @Override
    public void Unpause() {
        disconnect = false;
        setGameState(GameState.END_ROUND);
        setTimer(6);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (getState().equals(GameState.TUTORIAL)) {
            e.setCancelled(true);
            return;
        }

        if (e.getPlayer().getLocation().distanceSquared(SPAWN) > MAX_DIST_FROM_CENTER) {
            e.getPlayer().teleport(SPAWN);
        }
    }

    public void Barriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        int[] blocks_x = new int[] {15, 15, 16, 17, 18, 19, 20, 21, 22, 23, 23};
        int[] blocks_z = new int[] { 0,  1,  2,  3,  4,  4,  4,  3,  2,  1,  0};
        for (int y = -60; y <= -59; y++) {
            for (int x : blocks_x) {
                for (int z : blocks_z) {
                    world.getBlockAt(x, y, z).setType(m);
                    world.getBlockAt(-x, y, z).setType(m);
                    world.getBlockAt(x, y, -z).setType(m);
                    world.getBlockAt(-x, y, -z).setType(m);
                }
            }
        }
    }
}
