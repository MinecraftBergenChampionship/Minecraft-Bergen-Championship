package me.kotayka.mbc.partygames;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gamePlayers.OneShotPlayer;
import me.kotayka.mbc.gameMaps.oneshotMaps.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
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
    private OneShotMap map = new Meltdown(this);
    private Map<MBCTeam, Integer> teamKills = new HashMap<>();
    public Map<UUID, OneShotPlayer> oneShotPlayerMap = new HashMap<>();
    public Location[] spawnpoints = map.spawnpoints;
    private final int WIN_POINTS = 10;
    private final int KILL_POINTS = 1;
    private final int STREAK_POINTS = 1;
    private final int STREAK_KILL_POINTS = 1;
    private final int WEAPON_POINTS = 5;
    private Map<MBCTeam, BossBar> teamBossBars = new HashMap<>();

    private static OneShot instance = null;
    public static final ItemStack CROSSBOW_QUICK_CHARGE = new ItemStack(Material.CROSSBOW);
    public static final ItemStack BOW = new ItemStack(Material.BOW);
    public static final ItemStack CROSSBOW_MULTISHOT = new ItemStack(Material.CROSSBOW);
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
                        "⑰ +5 points for each player on a team for getting a new weapon\n" +
                        "⑰ +10 points for each player on a team for winning\n" +
                        "⑰ +1 extra point for kills with a 3 kill streak or more\n" +
                        "⑰ +1 extra point for killing a player with a 3 kill streak or more"
        });

        CrossbowMeta quickchargeMeta = (CrossbowMeta) CROSSBOW_QUICK_CHARGE.getItemMeta();
        quickchargeMeta.addChargedProjectile(new ItemStack(Material.ARROW, 1));
        quickchargeMeta.setUnbreakable(true);
        CROSSBOW_QUICK_CHARGE.setItemMeta(quickchargeMeta);
        CROSSBOW_QUICK_CHARGE.addEnchantment(Enchantment.QUICK_CHARGE, 3);
        
        ItemMeta bowMeta = BOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        BOW.setItemMeta(bowMeta);
        BOW.addEnchantment(Enchantment.ARROW_INFINITE, 1);

        CrossbowMeta multishotMeta = (CrossbowMeta) CROSSBOW_MULTISHOT.getItemMeta();
        multishotMeta.addChargedProjectile(new ItemStack(Material.ARROW, 1));
        multishotMeta.setUnbreakable(true);
        CROSSBOW_MULTISHOT.setItemMeta(multishotMeta);
        CROSSBOW_MULTISHOT.addEnchantment(Enchantment.MULTISHOT, 1);

        ItemMeta tridentMeta = TRIDENT.getItemMeta();
        tridentMeta.setUnbreakable(true);
        TRIDENT.setItemMeta(tridentMeta);
    }

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new OneShot();
            Bukkit.broadcastMessage("new one shot");
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
            p.getPlayer().stopSound(Sound.MUSIC_DISC_RELIC, SoundCategory.RECORDS);
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
        Location l = spawnpoints[(int)(Math.random()*spawnpoints.length)];
        p.getPlayer().teleport(l);
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setInvulnerable(true);
        p.getPlayer().setFlying(false);
        p.getPlayer().getInventory().clear();
        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 2, false, false));
        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 2, false, false));

        switch(teamKills.get(p.getTeam()) / 10) {
            case 0:
                p.getInventory().addItem(CROSSBOW_QUICK_CHARGE);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
            break;
            case 1:
                p.getInventory().addItem(BOW);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
            break;
            case 2:
                p.getInventory().addItem(CROSSBOW_MULTISHOT);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
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

        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { regainItems(p);}
          }, 40L);
    }

    public void regainItems(Participant p) {
        p.getPlayer().setInvulnerable(false);
        
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
        createLine(20, ChatColor.RED+""+ChatColor.BOLD+"Team in First:" + ChatColor.YELLOW + " Tied!", p);
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

            p.getPlayer().removePotionEffect(PotionEffectType.JUMP);
            p.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);

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
            OneShotPlayer s = oneShotPlayerMap.get(((Entity)e.getPlayer()).getUniqueId());
            if (s.streak >= 3) {
                Bukkit.broadcastMessage(Participant.getParticipant((Player) e.getPlayer()).getFormattedName() + ChatColor.BOLD + "'s streak of " + s.streak + " has been lost due to falling!");
            }
            s.streak = 0;
            Death(Participant.getParticipant((Player) e.getPlayer()));
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
                startingCountdown();
            } else {
                setGameState(GameState.ACTIVE);
                for (Player p : Bukkit.getOnlinePlayers()) {
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
        }
        
    }

    private void incrementBossBar(BossBar b, int k, MBCTeam m) {
        b.setProgress(((double)k)/10.0);
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
                if (s.streak >=3) {
                    Bukkit.broadcastMessage(damager.getFormattedName() + "" + ChatColor.BOLD + " has broke " + ChatColor.RESET + "" +
                        shot.getFormattedName() + ChatColor.BOLD + "'s streak of " + s.streak + "!");
                    damager.addCurrentScore(STREAK_KILL_POINTS);

                }
                s.streak = 0;

                damager.addCurrentScore(KILL_POINTS);
                if (d.streak >=3) {
                    damager.addCurrentScore(STREAK_POINTS);
                    if (d.streak == 3) {
                        Bukkit.broadcastMessage(damager.getFormattedName() + "" + ChatColor.BOLD + " has reached a streak of 3!");
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
                if (s.streak >=3) {
                    Bukkit.broadcastMessage(damager.getFormattedName() + "" + ChatColor.BOLD + " has broken " + ChatColor.RESET + "" +
                        shot.getFormattedName() + ChatColor.BOLD + "'s streak of " + s.streak + "!");
                    damager.addCurrentScore(STREAK_KILL_POINTS);

                }
                s.streak = 0;

                damager.addCurrentScore(KILL_POINTS);
                if (d.streak >=3) {
                    damager.addCurrentScore(STREAK_POINTS);
                    if (d.streak == 3) {
                        Bukkit.broadcastMessage(damager.getFormattedName() + "" + ChatColor.BOLD + " has reached a streak of 3!");
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
            Death(Participant.getParticipant((Player) event.getEntity()));
            EndGame(Participant.getParticipant(((Player) event.getDamager())).getTeam());
            event.setCancelled(true);
        }
    }

    private void nextWeapon(MBCTeam m) {
        BossBar b = teamBossBars.get(m);
        b.removeAll();
        switch (teamKills.get(m)) {
            case 10:
                for (Participant p : m.getPlayers()) {
                    p.getPlayer().activeBossBars();
                    p.getInventory().remove(Material.CROSSBOW);
                    p.getInventory().addItem(BOW);
                    p.addCurrentScore(WEAPON_POINTS);
                    b = Bukkit.createBossBar(ChatColor.YELLOW + "" + ChatColor.BOLD + "CURRENT WEAPON: Bow", BarColor.YELLOW, BarStyle.SOLID);
                    b.setProgress(0);
                    teamBossBars.replace(m, b);
                    b.setVisible(true);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage(ChatColor.YELLOW +"Your team reached 10 kills and recieved the " + ChatColor.BOLD + "bow" + ChatColor.RESET + ChatColor.YELLOW +"!");
                }
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "The " +ChatColor.RESET + m.teamNameFormat() + ChatColor.BOLD + "" + ChatColor.RED + " have gotten 10 kills!");
            return;
            case 20:
                for (Participant p : m.getPlayers()) {
                    p.getInventory().remove(Material.BOW);
                    p.getInventory().addItem(CROSSBOW_MULTISHOT);
                    p.addCurrentScore(WEAPON_POINTS);
                    b = Bukkit.createBossBar(ChatColor.GREEN + "" + ChatColor.BOLD + "CURRENT WEAPON: Multishot Crossbow", BarColor.GREEN, BarStyle.SOLID);
                    b.setProgress(0);
                    teamBossBars.replace(m, b);
                    b.setVisible(true);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage("Your team reached 20 kills and recieved the " + ChatColor.BOLD + "multishot crossbow" + ChatColor.RESET + "!");
                }
                Bukkit.broadcastMessage("The " + m.teamNameFormat() + " have gotten 20 kills!");
            return;
            case 30:
                for (Participant p : m.getPlayers()) {
                    p.getInventory().remove(Material.CROSSBOW);
                    p.getInventory().remove(Material.ARROW);
                    p.getInventory().addItem(TRIDENT);
                    p.addCurrentScore(WEAPON_POINTS);
                    b = Bukkit.createBossBar(ChatColor.AQUA + "" + ChatColor.BOLD + "CURRENT WEAPON: Trident", BarColor.BLUE, BarStyle.SOLID);
                    b.setProgress(0);
                    teamBossBars.replace(m, b);
                    b.setVisible(true);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage("Your team reached 30 kills and recieved the " + ChatColor.BOLD + "trident" + ChatColor.RESET + "!");
                }
                Bukkit.broadcastMessage("The " + m.teamNameFormat() + " have gotten 30 kills!");
            return;
            case 40:
                for (Participant p : m.getPlayers()) {
                    p.getInventory().remove(Material.TRIDENT);
                    p.getInventory().addItem(SWORD);
                    p.addCurrentScore(WEAPON_POINTS);
                    p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                    b = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "CURRENT WEAPON: Sword", BarColor.PURPLE, BarStyle.SOLID);
                    b.setProgress(0);
                    teamBossBars.replace(m, b);
                    b.setVisible(true);
                    b.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    p.getPlayer().sendMessage("Your team reached 40 kills and recieved the " + ChatColor.BOLD + "sword" + ChatColor.RESET + "! Get one kill to win!");
                }
                Bukkit.broadcastMessage("The " + m.teamNameFormat() + " have gotten 40 kills, " + ChatColor.BOLD + "and need one more to win the game!");
            return;
            default:
                Bukkit.broadcastMessage("This shouldn't be happening.");
        }
        
    }

    private void EndGame(MBCTeam m) {
        for (Participant p : m.getPlayers()) {
            p.addCurrentScore(WIN_POINTS);
            p.getPlayer().sendMessage(ChatColor.BOLD +"Your team won, and recieved " + ChatColor.GOLD + WIN_POINTS*m.getPlayers().size() + " points!");
        }
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().sendTitle(ChatColor.BOLD + "Game Over!", "", 0, 15, 15);
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().getInventory().clear();
            flightEffects(p);
            teamBossBars.get(p.getTeam()).setVisible(false);
        }
        
        Bukkit.broadcastMessage(ChatColor.BOLD + "The " + ChatColor.RESET + m.teamNameFormat() + " have won!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_BLOCKS, SoundCategory.RECORDS);
        }
        for (MBCTeam mt : MBC.getInstance().getValidTeams()) {
            BossBar b = teamBossBars.get(mt);
            b.removeAll();
            b.setVisible(false);
        }
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
                createLine(20, ChatColor.RED+""+ChatColor.BOLD+"Team in First:" + ChatColor.YELLOW + " Tied!", p);
            }
            else {
                createLine(20, ChatColor.RED+""+ChatColor.BOLD+"First: " + checkTopTeam().teamNameFormat(), p);
            }
        }

        damager.getPlayer().sendMessage(ChatColor.RED + "You killed " + ChatColor.RESET + shot.getFormattedName() + "!");
        shot.getPlayer().sendMessage(ChatColor.RED + "You were killed by " + ChatColor.RESET + damager.getFormattedName() + "!");
        shot.getPlayer().sendTitle(ChatColor.BOLD + "Respawning in 3 seconds...", "", 0, 15, 15);

        MBC.spawnFirework(shot);

        shot.getPlayer().setInvulnerable(true);
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


        died.getPlayer().setInvulnerable(true);
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
}
