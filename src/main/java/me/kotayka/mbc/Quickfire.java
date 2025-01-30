package me.kotayka.mbc;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import me.kotayka.mbc.gameMaps.quickfireMap.QuickfireMap;
import me.kotayka.mbc.gameMaps.quickfireMap.SnowGlobe;
import me.kotayka.mbc.gameMaps.quickfireMap.Castle;
import me.kotayka.mbc.gameMaps.quickfireMap.Mansion;
import me.kotayka.mbc.gamePlayers.QuickfirePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;



public class Quickfire extends FinaleGame {
    private final QuickfireMap map = new SnowGlobe(this);
    public static final ItemStack CROSSBOW = new ItemStack(Material.CROSSBOW);
    public static final ItemStack BOOTS = new ItemStack(Material.LEATHER_BOOTS);
    public Map<UUID, QuickfirePlayer> quickfirePlayers = new HashMap<>();
    public static final int MAX_DIST_FROM_CENTER = 13225;
    private World world = Bukkit.getWorld("Quickfire");
    private final Location TEAM_ONE_SPAWN = map.getTeamOneSpawn();
    private final Location TEAM_TWO_SPAWN = map.getTeamTwoSpawn();
    private final Location SPAWN = map.getSpawn();
    private int[] playersAlive;
    private int timeElapsed = 0;
    private int timeUntilGlowing = map.getTimeUntilGlowing();
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
                "No points are awarded for this game.\n" + ChatColor.YELLOW + "The winning team will win the Minecraft Bergen Championship!\n"
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
        CROSSBOW.addEnchantment(Enchantment.QUICK_CHARGE, 3);

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
    }

    public Quickfire(@NotNull MBCTeam firstPlace, @NotNull MBCTeam secondPlace) {
        super("Quickfire", firstPlace, secondPlace, new String[] {
                "⑩ Welcome to the Finale of MBC; Quickfire!\n Only the top two teams will play this round.\n",
                "⑩ Quickfire is a shooter game where every player has a crossbow, infinite arrows, and 4 hearts.\n",
                "⑩ Shooting another player will deal exactly one heart of health.\n" + ChatColor.RED + "Shoot someone four times to eliminate them.\n",
                "⑩ Eliminate the entire opposing team to win a round.\n" + ChatColor.YELLOW + "Quickfire is best of 5 rounds.\n",
                "⑩ Once the timer exceeds a minute and a half, all players will receive the " + ChatColor.BOLD + "glowing" + ChatColor.RESET + " effect.\n",
                "⑩ Each crossbow has the " + ChatColor.BOLD + "Quick Charge" + ChatColor.RESET + " enchantment, so be sure to fire fast!\n",
                "⑩ No points are awarded for this game.\n" + ChatColor.YELLOW + "The winning team will win the Minecraft Bergen Championship!\n"
        });

        if (logger == null) {
            initLogger();
            //Bukkit.broadcastMessage("logger bad :(");
        }

        ItemMeta bowMeta = CROSSBOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        CROSSBOW.setItemMeta(bowMeta);
        CROSSBOW.addEnchantment(Enchantment.QUICK_CHARGE, 3);

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
    }

    @Override
    public void start() {
        // stopTimer() and the commands to deregister lobby/dd should prob be moved to super.start()
        stopTimer();
        if (firstPlace == null) { return; }
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
        changeColor();
        loadPlayers();

        if (logger == null) {
            Bukkit.broadcastMessage("logger bad :( elsewhere wtf");
            initLogger();
        }


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
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 255, false, false));
            if (p.getTeam().equals(firstPlace) || p.getTeam().equals(secondPlace)) {
                quickfirePlayers.put(p.getPlayer().getUniqueId(), new QuickfirePlayer(p));
                p.board.getTeam(firstPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                p.board.getTeam(secondPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            }
        }
    }

    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining < 0) {
                Bukkit.broadcastMessage("tutorial negative");
                timeRemaining = 53;
            }
            if (timeRemaining == 0) {
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 30;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.BLOCKS, 1, 1);
                }
                Barriers(false);
                setGameState(GameState.ACTIVE);
                timeRemaining = 3600;
            } else {
                if (timeRemaining == 16) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
                    }
                }
                startingCountdown(Sound.ITEM_GOAT_HORN_SOUND_1);
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            createLineAll(20, ChatColor.RED.toString() + ChatColor.BOLD + "Time: " + ChatColor.RESET + getFormattedTime(timeElapsed));
            if (timeElapsed == timeUntilGlowing) {
                for (Participant p : firstPlace.teamPlayers) {
                    if (!p.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
                        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 2, false, false));
                }
                for (Participant p : secondPlace.teamPlayers) {
                    if (!p.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
                        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 2, false, false));
                }
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + ChatColor.BOLD + " All players are now glowing!");
            }
            else if (timeElapsed%217 == 201) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
                }
            }
            timeElapsed++;
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                resetMap();
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 10;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 0) {
                returnToLobby();
            }
        }
    }

    public void startRound() {
        Barriers(true);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            resetLine(p, 21);
            p.getInventory().clear();
            p.getPlayer().setInvulnerable(false);
            if (p.getTeam().equals(firstPlace)) {
                p.getPlayer().setMaxHealth(8);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getInventory().addItem(CROSSBOW);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
                p.getInventory().setBoots(firstPlace.getColoredLeatherArmor(BOOTS));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(TEAM_ONE_SPAWN);
                p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
            } else if (p.getTeam().equals(secondPlace)) {
                p.getPlayer().setMaxHealth(8);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getInventory().addItem(CROSSBOW);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
                p.getInventory().setBoots(secondPlace.getColoredLeatherArmor(BOOTS));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(TEAM_TWO_SPAWN);
                p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
            } else {
                p.getPlayer().setGameMode(GameMode.SPECTATOR);
                p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }

    private void endRound(MBCTeam winner) {
        for (Arrow a : world.getEntitiesByClass(Arrow.class)) {
            a.remove();
        }

        timeElapsed = 0;
        Bukkit.broadcastMessage("\n"+winner.teamNameFormat() + " have won the round!\n");
        logger.log(winner.getTeamName() + " have won the round!\n");
        createScoreboard();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.setInvulnerable(true);
            p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
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
    public void onArrowShoot(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();

        if (shooter instanceof Player) {
            Player player = (Player) shooter;

            ItemStack arrow = new ItemStack(Material.ARROW);
            player.getInventory().addItem(arrow);
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        event.getArrow().remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void arrowHit(ProjectileHitEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        if (!(e.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;

        if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
            Participant shot = Participant.getParticipant((Player) e.getHitEntity());
            Participant damager = Participant.getParticipant((Player) arrow.getShooter());

            if (damager.getTeam().equals(shot.getTeam())) return;

            QuickfirePlayer damagerPlayer = getQuickfirePlayer(damager.getPlayer());
            QuickfirePlayer shotPlayer = getQuickfirePlayer(shot.getPlayer());

            damager.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(shot.getFormattedName() + " - " + ChatColor.RED + ((int)(shot.getPlayer().getHealth()-2))/2+ " ♥"));
            damager.getPlayer().playSound(damager.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            damagerPlayer.incrementDamageDealt();
            shotPlayer.incrementDamageTaken();
            if (shot.getPlayer().getHealth() <= 2) {
                shotPlayer.incrementDeaths();
                damagerPlayer.incrementKills();
                Death(shot, damager);
                arrow.remove();
            } else {
                shot.getPlayer().damage(2);
                shot.getPlayer().setVelocity(new Vector(arrow.getVelocity().getX()*0.15, 0.3, arrow.getVelocity().getZ()*0.15));
                arrow.remove();
                logger.log(shotPlayer.getPlayer().getName() + " was shot by " + damager.getPlayerName());
            }
        }
    }

    private void Death(Participant victim, Participant killer) {
        MBC.spawnFirework(victim);
        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        killer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(victim.getFormattedName() + " - " + ChatColor.RED + "0 ♥"));
        Bukkit.broadcastMessage(victim.getFormattedName() + " was shot by " + killer.getFormattedName());
        logger.log(victim.getPlayerName() + " was shot and killed by " + killer.getPlayerName());

        if (victim.getTeam().equals(firstPlace)) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
                // TODO, possibly: set spectator to another player on their team
            } else {
                score[1]++;
                if (score[1] == 3) endGame(secondPlace);
                else endRound(secondPlace);
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
                // TODO, possibly: set spectator to another player on their team
            } else {
                score[0]++;
                if (score[0] == 3) endGame(firstPlace);
                else endRound(firstPlace);
            }
        }
    }

    private void endGame(MBCTeam t) {
        Bukkit.broadcastMessage("\n" + t.teamNameFormat() + " win MBC!\n");
        logger.log(t.getTeamName() + " win MBC!\n");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.GLOWING);
            createScoreboard();
            p.sendTitle(t.teamNameFormat() + " win MBC!", " ", 0, 100, 20);
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_6, SoundCategory.RECORDS, 1, 1);
            p.setInvulnerable(true);
        }

        for (Participant p : t.teamPlayers) {
            p.winner = true;
        }
        logger.logStats();

        setGameState(GameState.END_GAME);
        createLineAll(21, ChatColor.RED.toString()+ChatColor.BOLD+"Back to lobby:");
        setTimer(13);
    }

    private void returnToLobby() {
        HandlerList.unregisterAll(this);
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.board.getTeam(firstPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            p.board.getTeam(secondPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            p.getPlayer().setMaxHealth(20);
            p.getPlayer().setHealth(20);
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

        MBC.getInstance().lobby.end();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (p == null) return;
        if (!(p.getTeam().equals(firstPlace) && p.getTeam().equals(secondPlace))) return;

        disconnect = true;
        Bukkit.broadcastMessage(p.getFormattedName() + " disconnected!");
        if (p.getTeam().equals(firstPlace)) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
            } else {
                score[1]++;
                if (score[1] == 3) endGame(secondPlace);
                else endRound(secondPlace);
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
            } else {
                score[0]++;
                if (score[0] == 3) endGame(firstPlace);
                else endRound(firstPlace);
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

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(e.getClickedBlock().getType() == Material.SPRUCE_TRAPDOOR || 
                e.getClickedBlock().getType() == Material.DARK_OAK_TRAPDOOR ||
                e.getClickedBlock().getType() == Material.OAK_TRAPDOOR) e.setCancelled(true);
        }
    }

    public void Barriers(boolean b) {
        map.resetBarriers(b);
    }

    private void changeColor() {
        map.changeColor(firstPlace, secondPlace);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }


    public QuickfirePlayer getQuickfirePlayer(Player p) {
        return quickfirePlayers.get(p.getUniqueId());
    }
}
