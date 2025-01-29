package me.kotayka.mbc.partygames;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gamePlayers.OneShotPlayer;
import me.kotayka.mbc.gamePlayers.PowerTagPlayer;
import me.kotayka.mbc.gameMaps.oneshotMaps.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;

public class OneShot extends PartyGame {
    private OneShotMap map = new Ascent(this);
    private Map<MBCTeam, Integer> teamKills = new HashMap<>();
    public Map<UUID, OneShotPlayer> oneShotPlayerMap = new HashMap<>();
    public Location[] spawnpoints = map.spawnpoints;
    private final int WIN_POINTS = 5;
    private final int KILL_POINTS = 1;
    private final int STREAK_POINTS = 1;
    private final int STREAK_KILL_POINTS = 1;
    private final int WEAPON_POINTS = 3;
    private Map<MBCTeam, BossBar> teamBossBars = new HashMap<>();

    private static OneShot instance = null;
    public static final ItemStack CROSSBOW_QUICK_CHARGE = new ItemStack(Material.CROSSBOW);
    public static final ItemStack CROSSBOW_MULTISHOT = new ItemStack(Material.CROSSBOW);
    public static final ItemStack BOW = new ItemStack(Material.BOW);
    public static final ItemStack TRIDENT = new ItemStack(Material.TRIDENT);
    public static final ItemStack SWORD = new ItemStack(Material.DIAMOND_SWORD);

    private OneShot() {
        super("OneShot", new String[] {
                "⑰ Use your weapons to kill other players with a one shot kill!\n\n" + 
                "⑰ You can't kill people yet, obviously, but you'll be able to soon!",
                "⑰ Every 10 kills your team gets, you'll get a new weapon.\n\n" + 
                "⑰ Get to 40 kills and get a melee kill to win!",
                "⑰ Get points for every kill, getting a new weapon, and winning!\n\n" + 
                "⑰ You can even get bonus points for getting a high enough kill streak!",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +1 point per kill\n" +
                        "⑰ +3 points for each player on a team for getting a new weapon\n" +
                        "⑰ +5 points for each player on a team for winning\n" +
                        "⑰ +1 extra point for kills with a 5 kill streak or more\n" +
                        "⑰ +1 extra point for killing a player with a 5 kill streak or more"
        });

        CrossbowMeta quickchargeMeta = (CrossbowMeta) CROSSBOW_QUICK_CHARGE.getItemMeta();
        quickchargeMeta.addChargedProjectile(new ItemStack(Material.ARROW, 1));
        quickchargeMeta.setUnbreakable(true);
        CROSSBOW_QUICK_CHARGE.setItemMeta(quickchargeMeta);
        CROSSBOW_QUICK_CHARGE.addEnchantment(Enchantment.QUICK_CHARGE, 3);

        CrossbowMeta multishotMeta = (CrossbowMeta) CROSSBOW_MULTISHOT.getItemMeta();
        multishotMeta.addChargedProjectile(new ItemStack(Material.ARROW, 1));
        multishotMeta.setUnbreakable(true);
        CROSSBOW_MULTISHOT.setItemMeta(multishotMeta);
        CROSSBOW_MULTISHOT.addEnchantment(Enchantment.MULTISHOT, 1);
        CROSSBOW_MULTISHOT.addEnchantment(Enchantment.QUICK_CHARGE, 1);
        
        ItemMeta bowMeta = BOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        BOW.setItemMeta(bowMeta);
        BOW.addEnchantment(Enchantment.INFINITY, 1);

        ItemMeta tridentMeta = TRIDENT.getItemMeta();
        tridentMeta.setUnbreakable(true);
        TRIDENT.setItemMeta(tridentMeta);
    }

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new OneShot();
        }
        return instance;
    }

    public void start() {
        super.start();

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    @Override
    public void endEvents() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().stopSound(Sound.MUSIC_DISC_BLOCKS, SoundCategory.RECORDS);
            p.getPlayer().setFireTicks(0);
        }
        logger.logStats();

        if (MBC.getInstance().party == null) {
            for (Participant p : MBC.getInstance().getPlayers()) {
                p.addCurrentScoreToTotal();
            }
            MBC.getInstance().updatePlacings();
            returnToLobby();
        } else {
            // start next game
            setupNext();
        }
    }

    public void playerRespawn(Participant p) {
        int randomSpawn = (int)(Math.random()*spawnpoints.length);

        int preventInfiniteCounter = 0;
        while (playerWithinFifteen(randomSpawn) && preventInfiniteCounter < spawnpoints.length - 5) {
            randomSpawn = (int)(Math.random()*spawnpoints.length);
        }

        Location l = spawnpoints[randomSpawn];
        p.getPlayer().teleport(l);
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setInvulnerable(true);
        p.getPlayer().setFlying(false);
        p.getPlayer().getInventory().clear();
        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 2, false, false));
        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 2, false, false));       

        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { regainItems(p);}
          }, 40L);
    }

    public boolean playerWithinFifteen(int spawnInt) {
        if (spawnInt < 0 || spawnInt >= spawnpoints.length) return true;
        Location l = spawnpoints[spawnInt];
        for (Participant p : MBC.getInstance().getPlayers()) {
            if (p.getPlayer().getLocation().distance(l) <=15 && p.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
                return true;
            }
        }
        return false;
    }

    public void regainItems(Participant p) {
        p.getPlayer().setInvulnerable(false);

        switch(teamKills.get(p.getTeam()) / 10) {
            case 0:
                p.getInventory().addItem(CROSSBOW_QUICK_CHARGE);
                p.getInventory().addItem(new ItemStack(Material.ARROW,1));
            break;
            case 1:
                p.getInventory().addItem(CROSSBOW_MULTISHOT);
                p.getInventory().addItem(new ItemStack(Material.ARROW,1));
            break;
            case 2:
                p.getInventory().addItem(BOW);
                p.getInventory().addItem(new ItemStack(Material.ARROW,1));
            break;
            case 3:
                p.getInventory().addItem(TRIDENT);
            break;
            case 4:
                p.getInventory().addItem(SWORD);
                p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            break;
            default:
                Bukkit.broadcastMessage("This shouldn't be happening.");
        }
        
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        p.getInventory().setHelmet(p.getTeam().getColoredLeatherArmor(helmet));
        p.getInventory().setChestplate(p.getTeam().getColoredLeatherArmor(chestplate));
        p.getInventory().setLeggings(p.getTeam().getColoredLeatherArmor(leggings));
        p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(boots));
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, "Party (" + name()) + ")", p);
        createLine(20, ChatColor.RED+""+ChatColor.BOLD+"First:" + ChatColor.YELLOW + " Tied!", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);
        createLine(3, ChatColor.YELLOW+""+ChatColor.BOLD+"Kills: "+ChatColor.RESET+"0", p);
        updatePlayersAliveScoreboard();
        updateInGameTeamScoreboard();
    }

    @Override
    public void onRestart() {
        resetPlayers();
        for (MBCTeam m : MBC.getInstance().getValidTeams()) {
            BossBar b = teamBossBars.get(m);
            if (b != null) {
                b.setVisible(false);
            }
        } 
        teamKills.clear();
    }

    public void resetPlayers() {
        teamKills = new HashMap<>();
        for (OneShotPlayer p : oneShotPlayerMap.values()) {
            p.kills = 0;
            p.streak = 0;
        }
    }

    @Override
    public void loadPlayers() {
        setPVP(false);
        for (MBCTeam m : MBC.getInstance().getValidTeams()) {
            BossBar b = teamBossBars.get(m);
            if (b != null) {
                b.setVisible(false);
            }
        } 
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setFlying(false);

            oneShotPlayerMap.put(p.getPlayer().getUniqueId(), new OneShotPlayer(p));

            p.getPlayer().removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 2, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 2, false, false));
            // reset scoreboard & variables after each round
            updatePlayersAliveScoreboard(p);

            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

            p.getInventory().setHelmet(p.getTeam().getColoredLeatherArmor(helmet));
            p.getInventory().setChestplate(p.getTeam().getColoredLeatherArmor(chestplate));
            p.getInventory().setLeggings(p.getTeam().getColoredLeatherArmor(leggings));
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(boots));

        }

        createScoreboard();

        for (MBCTeam m : MBC.getInstance().getValidTeams()) {
            teamKills.put(m, 0);
            BossBar b = Bukkit.createBossBar(ChatColor.RED + "" + ChatColor.BOLD + "CURRENT WEAPON: Quick Charge Crossbow", BarColor.RED, BarStyle.SOLID);
            b.setProgress(0);
            teamBossBars.put(m, b);
        }

        spawnPlayers();
    }

    private void spawnPlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            Location l = spawnpoints[(int)(Math.random()*spawnpoints.length)];
            p.getPlayer().teleport(l);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
        }
        
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;
        if (map == null) return;

        if (e.getPlayer().getLocation().getY() < map.DEATH_Y) {
            if (e.getPlayer().getGameMode() != GameMode.ADVENTURE) {
                e.getPlayer().teleport(map.spawnpoints[(int)(Math.random()*spawnpoints.length)]);
                return;
            }
            
            e.getPlayer().setVelocity(new Vector(0, 0, 0));
            OneShotPlayer s = oneShotPlayerMap.get((e.getPlayer()).getUniqueId());
            if (s.streak >= 5) {
                String message = Participant.getParticipant(e.getPlayer()).getFormattedName() + ChatColor.BOLD + "'s streak of " + s.streak + " has been lost due to falling!";
                Bukkit.broadcastMessage(message);
                logger.log(message);
            }
            s.streak = 0;
            Death(Participant.getParticipant(e.getPlayer()));
        }
    }

    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                MBC.getInstance().sendMutedMessages();
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The game is starting!\n");
                setGameState(GameState.STARTING);
                timeRemaining = 15;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown(Sound.ITEM_GOAT_HORN_SOUND_1);
                if (timeRemaining == 9) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_7, SoundCategory.RECORDS, 1, 1);
                    }
                }
            } else {
                setGameState(GameState.ACTIVE);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.BLOCKS, 0.75f, 1);
                    p.playSound(p, Sound.MUSIC_DISC_BLOCKS, SoundCategory.RECORDS, 1, 1);
                }
                setPVP(true);
                       
                for (Participant p : MBC.getInstance().getPlayers()) {
                    teamBossBars.get(p.getTeam()).addPlayer(p.getPlayer());
                    p.getPlayer().setInvulnerable(false);
                    p.getPlayer().setGameMode(GameMode.ADVENTURE);
                    p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
                    p.getInventory().addItem(CROSSBOW_QUICK_CHARGE);
                    p.getInventory().addItem(new ItemStack(Material.ARROW,64));
                }
                for (MBCTeam m : MBC.getInstance().getValidTeams()) {
                    BossBar b = teamBossBars.get(m);
                    b.setVisible(true);
                } 
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 7) Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Most Individual Kills:");
            if (timeRemaining == 5) topKillers();
            if (timeRemaining == 0) endEvents();

        }
    }

    private void incrementBossBar(BossBar b, int k, MBCTeam m) {
        b.setProgress(((double)k)/10.0);
    }

    private void topKillers() {
        PowerTagPlayer[] killersSorted = new PowerTagPlayer[5];

        ArrayList<PowerTagPlayer> arrayPowerTagPlayers = new ArrayList(oneShotPlayerMap.values());
        for (int j = 0; j < arrayPowerTagPlayers.size(); j++) {
            PowerTagPlayer p = arrayPowerTagPlayers.get(j);
            for (int i = 0; i < killersSorted.length; i++) {
                if (killersSorted[i] == null) {
                    killersSorted[i] = p;
                    break;
                }
                if (killersSorted[i].getTimeSurvived() < p.getTimeSurvived()) {
                    PowerTagPlayer q = p;
                    p = killersSorted[i];
                    killersSorted[i] = q;
                }
            }
        }


        StringBuilder topFive = new StringBuilder();
        
        //Bukkit.broadcastMessage("[Debug] fastestLaps.keySet().size() == " + fastestLaps.keySet().size());
        for (int i = 0; i < killersSorted.length; i++) {
            if (killersSorted[i] == null) break;
            topFive.append(String.format((i+1) + ". %-18s %s\n", killersSorted[i].getParticipant().getFormattedName(), (killersSorted[i].getKills())));
            
        }
        Bukkit.broadcastMessage(topFive.toString());
    }

    @EventHandler
    public void hit(ProjectileHitEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();
            if (e.getHitBlock() != null) {
                arrow.remove();
                return;
            }
            if (!(arrow.getShooter() instanceof Player)) return;
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                Participant shot = Participant.getParticipant((Player) e.getHitEntity());
                Participant damager = Participant.getParticipant((Player) arrow.getShooter());
                if (shot.getTeam().equals(damager.getTeam())) return;
                if (shot.getPlayer().isInvulnerable())  return;

                OneShotPlayer d = oneShotPlayerMap.get(((Entity) arrow.getShooter()).getUniqueId());
                d.kills++;
                d.streak++;
                createLine(3, ChatColor.YELLOW+""+ChatColor.BOLD+"Kills: "+ChatColor.RESET+d.kills, damager);

                OneShotPlayer s = oneShotPlayerMap.get(e.getHitEntity().getUniqueId());
                if (s.streak >=5) {
                    String message = damager.getFormattedName() + " has broken " + shot.getFormattedName() + "'s streak of " + s.streak + "!";
                    Bukkit.broadcastMessage(message);
                    logger.log(message);
                    damager.getPlayer().sendMessage(ChatColor.GREEN + "You broke a streak of " + s.streak + "!" + MBC.scoreFormatter(STREAK_KILL_POINTS));
                    damager.addCurrentScore(STREAK_KILL_POINTS);

                }
                s.streak = 0;

                damager.addCurrentScore(KILL_POINTS);
                if (d.streak >=5) {
                    damager.addCurrentScore(STREAK_POINTS);
                    damager.getPlayer().sendMessage(ChatColor.GREEN + "You have a streak of " + d.streak + "!" + MBC.scoreFormatter(STREAK_POINTS));
                    if (d.streak == 5) {
                        String message = damager.getFormattedName() + " has reached a streak of 5, and is now glowing!";
                        Bukkit.broadcastMessage(message);
                        logger.log(message);
                        damager.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 255, false, false));
                    }
                }
                arrow.remove();
                Death(shot, damager);
            }
        }
        else if (e.getEntity() instanceof Trident) {
            Trident trident = (Trident) e.getEntity();
            if (!(trident.getShooter() instanceof Player)) return;
            if (teamKills.get(Participant.getParticipant(((Player) trident.getShooter())).getTeam()) == 40) {
                trident.remove();
                return;
            } 
            if (e.getHitBlock() != null) {
                trident.remove();
                ((Player) trident.getShooter()).getInventory().addItem(TRIDENT);
                ((Player) trident.getShooter()).playSound(((Player) trident.getShooter()), Sound.ITEM_TRIDENT_HIT_GROUND, 1, 1);
                return;
            }
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                Participant shot = Participant.getParticipant((Player) e.getHitEntity());
                Participant damager = Participant.getParticipant((Player) trident.getShooter());
                if (shot.getTeam().equals(damager.getTeam())) return;
                if (shot.getPlayer().isInvulnerable())  return;

                OneShotPlayer d = oneShotPlayerMap.get(((Entity) trident.getShooter()).getUniqueId());
                d.kills++;
                d.streak++;
                createLine(3, ChatColor.YELLOW+""+ChatColor.BOLD+"Kills: "+ChatColor.RESET+d.kills, damager);

                OneShotPlayer s = oneShotPlayerMap.get(e.getHitEntity().getUniqueId());
                if (s.streak >=5) {
                    String message = damager.getFormattedName() + " has broken " + shot.getFormattedName() + "'s streak of " + s.streak + "!";
                    Bukkit.broadcastMessage(message);
                    logger.log(message);
                    damager.getPlayer().sendMessage(ChatColor.GREEN + "You broke a streak of " + s.streak + "!" + MBC.scoreFormatter(STREAK_KILL_POINTS));
                    damager.addCurrentScore(STREAK_KILL_POINTS);

                }
                s.streak = 0;

                damager.addCurrentScore(KILL_POINTS);
                if (d.streak >=5) {
                    damager.addCurrentScore(STREAK_POINTS);
                    damager.getPlayer().sendMessage(ChatColor.GREEN + "You have a streak of " + d.streak + "!" + MBC.scoreFormatter(STREAK_POINTS));
                    if (d.streak == 5) {
                        String message = damager.getFormattedName() + " has reached a streak of 5, and is now glowing!";
                        Bukkit.broadcastMessage(message);
                        logger.log(message);
                        damager.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 255, false, false));
                    }
                }
                trident.remove();
                ((Player) trident.getShooter()).getInventory().addItem(TRIDENT);
                Death(shot, damager);
            }
        }
    }

    public MBCTeam checkTopTeam() {
        int topKills = 0;
        MBCTeam topKillerTeam = null;
        for (MBCTeam m : MBC.getInstance().getValidTeams()) {
            int kills = teamKills.get(m);
            if (kills > topKills) {
                topKills = kills;
                topKillerTeam = m;
            }
            else if (kills == topKills) {
                topKillerTeam = null;
            }
        }
        return topKillerTeam;
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        if (!getState().equals(GameState.ACTIVE)) return;
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        if (((Player) event.getDamager()).getGameMode() != GameMode.ADVENTURE) return;
        if (teamKills.get(Participant.getParticipant(((Player) event.getDamager())).getTeam()) != 40) return;

        if(((Player)event.getDamager()).getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD ||
        ((Player)event.getDamager()).getInventory().getItemInOffHand().getType() == Material.DIAMOND_SWORD) {
            if ((Participant.getParticipant(((Player) event.getDamager())).getTeam()).equals((Participant.getParticipant(((Player) event.getEntity())).getTeam()))) return;
            Death(Participant.getParticipant((Player) event.getEntity()), Participant.getParticipant(((Player)event.getDamager())));
            ((Player)event.getDamager()).sendMessage(ChatColor.RED + "You killed " + ChatColor.RESET + Participant.getParticipant((Player) event.getEntity()).getFormattedName() + "!" + MBC.scoreFormatter(KILL_POINTS));
            EndGame(Participant.getParticipant(((Player) event.getDamager())).getTeam());
            event.setCancelled(true);
        }
    }

    private void nextWeapon(MBCTeam m) {
        BossBar b = teamBossBars.get(m);
        b.setVisible(false);
        b.removeAll();
        String message = "";
        switch (teamKills.get(m)) {
            case 10:
                b = Bukkit.createBossBar(ChatColor.YELLOW + "" + ChatColor.BOLD + "CURRENT WEAPON: Multishot Crossbow", BarColor.GREEN, BarStyle.SOLID);
                b.setProgress(0);
                teamBossBars.replace(m, b);
                b.setVisible(true);
                for (Participant p : m.getPlayers()) {
                    p.getInventory().remove(Material.BOW);
                    p.getInventory().addItem(CROSSBOW_MULTISHOT);
                    p.addCurrentScore(WEAPON_POINTS);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage(ChatColor.RED +"Your team reached 10 kills and recieved the " + ChatColor.BOLD + "multishot crossbow" + ChatColor.RESET + "" + ChatColor.RED +"!" + MBC.scoreFormatter(WEAPON_POINTS));
                }
                message = ChatColor.YELLOW + "" + ChatColor.BOLD + "The " +ChatColor.RESET + "" + ChatColor.BOLD + m.teamNameFormat() + ChatColor.YELLOW + "" + ChatColor.BOLD + " have gotten 10 kills!";
            return;
            case 20:
                b = Bukkit.createBossBar(ChatColor.GREEN + "" + ChatColor.BOLD + "CURRENT WEAPON: Bow", BarColor.YELLOW, BarStyle.SOLID);
                b.setProgress(0);
                teamBossBars.replace(m, b);
                for (Participant p : m.getPlayers()) {
                    p.getPlayer().activeBossBars();
                    p.getInventory().remove(Material.CROSSBOW);
                    p.getInventory().addItem(BOW);
                    p.addCurrentScore(WEAPON_POINTS);
                    b.setVisible(true);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage(ChatColor.RED +"Your team reached 20 kills and recieved the " + ChatColor.BOLD + "bow" + ChatColor.RESET + "" + ChatColor.RED +"!" + MBC.scoreFormatter(WEAPON_POINTS));
                }
                message = ChatColor.GREEN + "" + ChatColor.BOLD + "The " +ChatColor.RESET + m.teamNameFormat() + ChatColor.GREEN + "" + ChatColor.BOLD + " have gotten 20 kills!";
            return;
            case 30:
                b = Bukkit.createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "CURRENT WEAPON: Trident", BarColor.BLUE, BarStyle.SOLID);
                b.setProgress(0);
                teamBossBars.replace(m, b);
                b.setVisible(true);
                for (Participant p : m.getPlayers()) {
                    p.getInventory().remove(Material.CROSSBOW);
                    p.getInventory().remove(Material.ARROW);
                    p.getInventory().addItem(TRIDENT);
                    p.addCurrentScore(WEAPON_POINTS);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage(ChatColor.RED +"Your team reached 30 kills and recieved the " + ChatColor.BOLD + "trident" + ChatColor.RESET + "" + ChatColor.RED +"!" + MBC.scoreFormatter(WEAPON_POINTS));
                }
                message = ChatColor.AQUA + "" + ChatColor.BOLD + "The " +ChatColor.RESET + "" + ChatColor.BOLD  + m.teamNameFormat() + ChatColor.AQUA + "" + ChatColor.BOLD + " have gotten 30 kills!";
            return;
            case 40:
                b = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "CURRENT WEAPON: Sword", BarColor.PURPLE, BarStyle.SOLID);
                b.setProgress(0);
                teamBossBars.replace(m, b);
                b.setVisible(true);
                for (Participant p : m.getPlayers()) {
                    p.getInventory().remove(Material.TRIDENT);
                    p.getInventory().addItem(SWORD);
                    p.addCurrentScore(WEAPON_POINTS);
                    p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage(ChatColor.RED + "Your team reached 40 kills and recieved the " + ChatColor.BOLD + "sword" + ChatColor.RESET + "" + ChatColor.RED + "! Get one kill to win!" + MBC.scoreFormatter(WEAPON_POINTS));
                }
                message = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "The " +ChatColor.RESET + "" + ChatColor.BOLD  +  m.teamNameFormat() + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " have gotten 40 kills, and need one more to win the game!";
            return;
            default:
                message = "This shouldn't be happening.";
        }
        Bukkit.broadcastMessage(message);
        logger.log(message);
    }

    private void EndGame(MBCTeam m) {
        for (Participant p : m.getPlayers()) {
            p.addCurrentScore(WIN_POINTS);
            p.getPlayer().sendMessage(ChatColor.BOLD +"Your team won!" + MBC.scoreFormatter(WIN_POINTS));
        }
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().sendTitle(ChatColor.BOLD + "Game Over!", "", 0, 15, 15);
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().getInventory().clear();
            flightEffects(p);
            teamBossBars.get(p.getTeam()).setVisible(false);
        }
        
        String message = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "The " + ChatColor.RESET + "" + ChatColor.BOLD + m.teamNameFormat() + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " have won!";
        Bukkit.broadcastMessage(message);
        logger.log(message);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_BLOCKS, SoundCategory.RECORDS);
            p.removePotionEffect(PotionEffectType.GLOWING);
        }
        for (MBCTeam mt : MBC.getInstance().getValidTeams()) {
            BossBar b = teamBossBars.get(mt);
            b.removeAll();
            b.setVisible(false);
        }

        //timeRemaining = 10;
        //setGameState(GameState.END_ROUND);
        // not sure if this work rn
        endEvents();
    }

    private void Death(Participant shot, Participant damager) {
        damager.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Killed " + ChatColor.RESET + shot.getFormattedName()));
        damager.getPlayer().playSound(damager.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);

        MBCTeam m = damager.getTeam();

        int damagerTeamKills = teamKills.get(m);
        damagerTeamKills++;
        
        teamKills.replace(m, damagerTeamKills);
        if (damagerTeamKills == 41) {
            EndGame(m);
        }
        else if (damagerTeamKills % 10 == 0) {
            nextWeapon(m);
        }
        else {
            incrementBossBar(teamBossBars.get(m), damagerTeamKills % 10, m);
        }

        for (Participant p : MBC.getInstance().getPlayers()) {
            if (checkTopTeam() == null) {
                createLine(20, ChatColor.RED+""+ChatColor.BOLD+"First:" + ChatColor.YELLOW + " Tied!", p);
            }
            else {
                createLine(20, ChatColor.RED+""+ChatColor.BOLD+"First: " + checkTopTeam().teamNameFormat(), p);
            }
        }

        damager.getPlayer().sendMessage(ChatColor.RED + "You killed " + ChatColor.RESET + shot.getFormattedName() + "!" + MBC.scoreFormatter(KILL_POINTS));
        shot.getPlayer().sendMessage(ChatColor.RED + "You were killed by " + ChatColor.RESET + damager.getFormattedName() + "!");
        shot.getPlayer().sendTitle(ChatColor.BOLD + "Respawning in 3 seconds...", "", 0, 15, 15);

        MBC.spawnFirework(shot);

        logger.log(shot.getFormattedName() + ChatColor.RED + " was killed by " + ChatColor.RESET + damager.getFormattedName() + "!");

        shot.getPlayer().setInvulnerable(true);
        shot.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
        shot.getInventory().remove(Material.CROSSBOW);
        shot.getInventory().remove(Material.TRIDENT);
        shot.getInventory().remove(Material.BOW);
        shot.getInventory().remove(Material.DIAMOND_SWORD);
        shot.getInventory().remove(Material.ARROW);
        shot.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 255, false, false));
        shot.getPlayer().setGameMode(GameMode.SPECTATOR);
        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { playerRespawn(shot);}
          }, 60L);
    }

    private void Death(Participant died) {
        died.getPlayer().sendMessage(ChatColor.RED + "You fell!");
        died.getPlayer().sendTitle(ChatColor.BOLD + "Respawning in 3 seconds...", "", 0, 15, 15);

        logger.log(died.getFormattedName() + " fell.");


        died.getPlayer().setInvulnerable(true);
        died.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
        died.getInventory().remove(Material.CROSSBOW);
        died.getInventory().remove(Material.TRIDENT);
        died.getInventory().remove(Material.BOW);
        died.getInventory().remove(Material.DIAMOND_SWORD);
        died.getInventory().remove(Material.ARROW);
        died.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 255, false, false));
        died.getPlayer().setGameMode(GameMode.SPECTATOR);
        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { playerRespawn(died);}
          }, 60L);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
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
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i.equals(Material.LEATHER_HELMET)) e.setCancelled(true);
        if (i.equals(Material.LEATHER_CHESTPLATE)) e.setCancelled(true);
        if (i.equals(Material.LEATHER_LEGGINGS)) e.setCancelled(true);
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
    }
}
