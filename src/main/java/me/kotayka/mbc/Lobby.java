package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TeamScoreSorter;
import me.kotayka.mbc.comparators.TotalIndividualComparator;
import me.kotayka.mbc.partygames.BeepTestLevel;
import me.kotayka.mbc.partygames.BeepTestLevelLoader;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.world.World;

import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Lobby extends Minigame {
    public static final Location LOBBY = new Location(Bukkit.getWorld("world"), 0.5, 1, 0.5, 180, 0);
    public final org.bukkit.World world = Bukkit.getWorld("world");
    public ArmorStand cameraman;
    private List<MBCTeam> reveal;
    private List<Participant> revealIndiv;
    private int revealCounter = 0;
    private int revealIndivCounter = 0;
    private int introCounter = 0;
    private List<Participant> lastIntro = new LinkedList<>();
    public static List<Leaderboard> individualLeaderboards = new ArrayList<>();

    private static final Location MINI_BEEP_SE = new Location(Bukkit.getWorld("world"), 94.5, 8, 80.5);
    private static final Location MINI_BEEP_NW = new Location(Bukkit.getWorld("world"), 68.5, -10, 46.5);
    public List<Participant> miniBeepers = new ArrayList<>(); 
    public int beepTestTimeRemaining = -1;
    public int taskBeepID = -1;
    private List<BeepTestLevel> easyLevels = null;
    private List<BeepTestLevel> regularLevels = null;
    private List<BeepTestLevel> mediumLevels = null;
    private List<BeepTestLevel> hardLevels = null;
    private BeepTestLevel currentLevel = null;
    private int beepRound = 0;
    private boolean activeBeep = false;
    private final int BEEP_DEATH_Y = -6;
    private final int BEEP_NORMAL_Z = 76;
    private final int BEEP_OPPOSITE_Z = 50;
    private final World WorldEditWorld = BukkitAdapter.adapt(Bukkit.getWorld("world"));
    private String lastLevelName = "";
    private boolean miniBeepStartable = true;

    private Map<Participant, Integer> pvpers = new HashMap<>();
    private boolean pvpStartable = true;
    private List<Player> pvpDead = new ArrayList<>();;

    private List<NPC> podiumNPCS = new ArrayList<>();

    public Lobby() {
        super("Lobby");
        Bukkit.getWorld("world").setTime(6000);
        colorPodiumsWhite();
        teamBarriers(false);
    }
    
    public void createScoreboard(Participant p) {
        newObjective(p);
        createLine(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team: " + p.getTeam().teamNameFormat(), p);
        if (MBC.getInstance().gameNum > 1) {
            createLine(21, ChatColor.RED+""+ChatColor.BOLD+"Event resumes in: ", p);
            createLine(16, ChatColor.RESET+ChatColor.RESET.toString()+ChatColor.RESET, p);
            createLine(15, String.format("%sTeam Leaderboard: (%d/6 Games)", ChatColor.GREEN, MBC.getInstance().gameNum), p);
            createLine(15, ChatColor.GREEN+"Team Leaderboard: ", p);
        } else {
            createLine(21, ChatColor.RED+""+ChatColor.BOLD + "Waiting for players...", p);
            // createLine(18, ChatColor.GREEN + "Teams Ready: ", p);
        }
        createLine(4, ChatColor.RESET.toString()+ChatColor.RESET, p);
        createLine(19, ChatColor.RESET.toString(), p);
        updatePlayerTotalScoreDisplay(p);

        displayTeamTotalScore(p.getTeam());
    }

    public void createScoreboardTeamReveal() {
        newObjective();
        for (Participant p : MBC.getInstance().participants) {
            newObjective(p);
            createLine(21, ChatColor.RED+""+ChatColor.BOLD+"Final Standings!", p);
            createLine(19, ChatColor.RESET.toString(), p);
            createLine(15, ChatColor.GREEN+"Team Leaderboard: ", p);
            createLine(4, ChatColor.RESET.toString()+ChatColor.RESET, p);
        }
        updateTeamStandings();
    }

    public void changeTeam(Participant p) {
        createLine(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team: " + p.getTeam().teamNameFormat(), p);
    }

    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                setGameState(GameState.STARTING);
                createLineAll(21, ChatColor.RED+""+ChatColor.BOLD + "To Decision Dome in: ");
                MBC.announce("Prepare to get warped into the decision dome!");
                timeRemaining = 16;
            } else {
                startingEvents(timeRemaining);
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 5) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.removePotionEffect(PotionEffectType.SATURATION);
                    p.playSound(p, Sound.BLOCK_PORTAL_TRAVEL, SoundCategory.RECORDS, 1, 1);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 110, 1, false, false));
                }
            }
            if (timeRemaining == 0) {
                toVoting();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (MBC.getInstance().gameNum == 4 && (timeRemaining == 260 || timeRemaining == 130)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_5, SoundCategory.RECORDS);
                    p.playSound(p, Sound.MUSIC_DISC_5, SoundCategory.RECORDS, 1, 1);
                }
            }
            if (timeRemaining == 0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_5, SoundCategory.RECORDS);
                }
                toVoting();
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            // TODO: Moving this to another function would probably be cleaner
            if (timeRemaining == 0) {
                toFinale();
            }
            if (revealCounter < 3 && timeRemaining < 127) {
                if (timeRemaining % 10 == 0) {
                    if (reveal.size() < (MBC.MAX_TEAMS - revealCounter)) {
                        String title = ChatColor.BOLD+"Nobody!";
                        Bukkit.broadcastMessage(title);
                        MBC.sendTitle(title, " ", 0, 80, 20);
                    } else {
                        revealTeam(reveal.get(revealCounter), MBC.MAX_TEAMS-revealCounter);
                    }
                    revealCounter++;
                } else if (timeRemaining % 5 == 0) {
                    String title = ChatColor.BOLD+"In " + Game.getPlace(MBC.MAX_TEAMS-revealCounter);
                    MBC.sendTitle(title," ", 20, 140, 20);
                    Bukkit.broadcastMessage("\n"+title+"...\n");
                }
            }
            if (timeRemaining > 135) {
                if (timeRemaining % 10 == 0) {
                    if (revealIndiv.size() < (8 - revealIndivCounter)) {
                        String title = ChatColor.BOLD+"Nobody!";
                        Bukkit.broadcastMessage(title);
                        MBC.sendTitle(title, " ", 0, 80, 20);
                    } else {
                        revealIndiv(revealIndiv.get(7 - revealIndivCounter), 8 - revealIndivCounter);
                    }
                    revealIndivCounter++;
                } else if (timeRemaining % 5 == 0) {
                    String title = ChatColor.BOLD+"In " + Game.getPlace(8 - revealIndivCounter);
                    MBC.sendTitle(title," ", 20, 140, 20);
                    Bukkit.broadcastMessage("\n"+title+"...\n");
                }
            }
            switch (timeRemaining) {
                case 217 -> {
                    revealIndiv = MBC.getInstance().getPlayers();
                    revealIndiv.sort(new TotalIndividualComparator());
                    while(revealIndiv.size() > 8) {
                        revealIndiv.remove(revealIndiv.size()-1);
                    }
                }
                case 130 -> {
                    loadPlayersScoreReveal();
                }
                case 127 -> {
                    reveal = getValidTeams();
                    reveal.sort(new TeamScoreSorter());
                }
                case 95 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"\nNow for the Finale Qualifiers!\n");
                }
                case 90 -> {
                    String title = String.format("In %s1st", ChatColor.GOLD);
                    Bukkit.broadcastMessage("\n" + title+ ChatColor.RESET + ChatColor.BOLD + "...\n");
                    MBC.sendTitle(title, " ", 20, 140, 20);
                }
                case 85 -> {
                    if (reveal.size() == 0) {
                        String title = ChatColor.BOLD+"Nobody!";
                        Bukkit.broadcastMessage(title);
                        MBC.sendTitle(title, " ", 0, 80, 20);
                    } else {
                        revealTeam(reveal.get(reveal.size()-1), 1);
                    }
                }
                case 80 -> {
                    Bukkit.broadcastMessage(ChatColor.RED + "Chat has temporarily been muted.");
                    Bukkit.broadcastMessage(ChatColor.BOLD+"\nFinally...\n");
                }
                case 79 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"\nThe team in " + ChatColor.GRAY+ "2nd"+ ChatColor.WHITE + ChatColor.BOLD+" place...\n");
                    MBC.sendTitle(ChatColor.BOLD+"In " + ChatColor.GRAY+"2nd", " ", 20, 140, 20);
                }
                case 77 -> {
                    if (reveal.size() <= 2) { return; }
                    MBCTeam two = reveal.get(reveal.size()-2);
                    MBCTeam third = reveal.get(reveal.size()-3);
                    double gap = two.getMultipliedTotalScore() - third.getMultipliedTotalScore();
                    Bukkit.broadcastMessage("\nWith a gap of " + ChatColor.BOLD + gap + ChatColor.RESET + " points...\n");
                    MBC.sendTitle(ChatColor.BOLD+"In " + ChatColor.GRAY+"2nd", "By " + gap + " points", 0, 140, 20);
                }
                case 75 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"\nPlaying in the finale...\n");
                }
                case 73 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"\nare...\n");
                }
                case 70 -> {
                    if (reveal.size() <= 1) {
                        String title = ChatColor.BOLD+"Nobody!";
                        Bukkit.broadcastMessage(title);
                        MBC.sendTitle(title, " ", 0, 80, 20);
                    } else {
                        revealTeam(reveal.get(reveal.size()-2), 2);
                    }
                }
                case 69 -> {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Chat has been unmuted!");
                    MBC.getInstance().sendMutedMessages();
                }
                case 66 -> {
                    Bukkit.broadcastMessage("\nWhich leaves us with " + ChatColor.DARK_RED + "3rd"+ChatColor.WHITE+".\n");
                }
                case 65 -> {
                    if (reveal.size() <= 2) {
                        String title = ChatColor.BOLD + "Nobody!";
                        Bukkit.broadcastMessage(title);
                        MBC.sendTitle(title, " ", 0, 80, 20);
                    } else {
                        revealTeam(reveal.get(reveal.size()-3), 3);
                    }
                }
                case 60 -> {
                    teamBarriers(false);
                    populatePodium();
                    boolean flag = reveal.size() > 1;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_WAIT, SoundCategory.RECORDS, 1, 1);
                        p.setGameMode(GameMode.ADVENTURE);
                        p.teleport(LOBBY);
                        if (flag) {
                            p.sendTitle("Final Duel!", reveal.get(reveal.size()-1).teamNameFormat() + " vs. " + reveal.get(reveal.size()-2).teamNameFormat(), 20, 60, 20);
                        }
                    }
                    cameraman.remove();
                }
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining < 0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.YELLOW+"Event Over!", "Thanks for playing!", 20, 120, 20);
                    createLineAll(21, ChatColor.GREEN.toString()+ChatColor.BOLD+"Event Over!");
                    createLineAll(20, "Thanks for playing!");
                }
                miniBeepStartable = true;
                pvpStartable = true;
            }
        }
    }

    public void toVoting() {
        HandlerList.unregisterAll(this);
        miniBeepEnd("Mini Beep has ended due to voting!");
        endPvp();
        miniBeepStartable = false;
        pvpStartable = false;
        setGameState(GameState.INACTIVE);
        if (MBC.getInstance().decisionDome == null) {
            MBC.getInstance().startGame(0);
        } else {
            MBC.getInstance().decisionDome.start();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.getPlayer().getWorld().equals(world)) return;

        if (getState().equals(GameState.END_ROUND) && e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) { e.setCancelled(true); }
        if (getState().equals(GameState.TUTORIAL) && e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) { e.setCancelled(true); }

        if (e.getPlayer().getLocation().getY() < -45){
            e.getPlayer().teleport(LOBBY);
        }

        if (miniBeepStartable && !activeBeep && e.getPlayer().getGameMode().equals(GameMode.ADVENTURE) && inBeepArea(e.getPlayer()) && !miniBeepers.contains(Participant.getParticipant(e.getPlayer()))) addBeepPlayer(e.getPlayer());
        if (activeBeep && inBeepArea(e.getPlayer()) && !miniBeepers.contains(Participant.getParticipant(e.getPlayer()))) e.getPlayer().teleport(new Location(world, 81.5, -4, 38.5, -180, 0));
        if (!inBeepArea(e.getPlayer()) && miniBeepers.contains(Participant.getParticipant(e.getPlayer()))) {
            removeBeepPlayer(e.getPlayer());
            e.getPlayer().playSound(e.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 1);
        }
        if (activeBeep && miniBeepers.contains(Participant.getParticipant(e.getPlayer())) && e.getPlayer().getY() <= BEEP_DEATH_Y) beepPlayerEliminated(e.getPlayer(), true);
    
        if (!pvpDead.contains(e.getPlayer()) && pvpStartable && e.getPlayer().getGameMode().equals(GameMode.ADVENTURE) && inPVPArea(e.getPlayer()) && !pvpers.keySet().contains(Participant.getParticipant(e.getPlayer()))) addPvpPlayer(e.getPlayer());
        if (!inPVPArea(e.getPlayer()) && pvpers.keySet().contains(Participant.getParticipant(e.getPlayer()))) {
            removePvpPlayer(e.getPlayer());
            e.getPlayer().playSound(e.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 1);
        }
        if(pvpDead.contains(e.getPlayer())) {
            e.getPlayer().teleport(new Location(world, 109.5, -4, -59.5, -90, 0));
            pvpDead.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (getState().equals(GameState.TUTORIAL) && e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) { e.setCancelled(true); }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!e.getPlayer().getLocation().getWorld().equals(world)) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType().equals(Material.OAK_WALL_SIGN)) {
            Block sign = e.getClickedBlock();
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines.length != 4) return;

            if (lines[1].equals(ChatColor.BOLD + "External")) {
                e.getPlayer().setResourcePack("https://download.mc-packs.net/pack/befa3f484caa75efa3a540342491e43082c346ea.zip", "befa3f484caa75efa3a540342491e43082c346ea");
            } else if (lines[1].equals(ChatColor.BOLD + "our custom")) {
                e.getPlayer().setResourcePack("https://download.mc-packs.net/pack/29992d6ccc406ad11e0550b17126ac1cb8f72b8a.zip", "29992d6ccc406ad11e0550b17126ac1cb8f72b8a");
            }
        } else if (e.getClickedBlock().getType().equals(Material.DAYLIGHT_DETECTOR)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void coralDecay(BlockFadeEvent e) {
        e.setCancelled((true));
    }

    @Override
    public void start() {
        MBC.getInstance().setCurrentGame(this);
        stopTimer();
        setGameState(GameState.ACTIVE);
        createScoreboard();
        beepBorders(false);
        loadPlayers();
        updateTeamStandings();
        miniBeepStartable = true;
        pvpStartable = true;
        if (MBC.getInstance().gameNum == 4) {
            setTimer(390);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p, Sound.MUSIC_DISC_5, SoundCategory.RECORDS, 1, 1);
            }
        } else {
            setTimer(130);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p, Sound.MUSIC_DISC_5, SoundCategory.RECORDS, 1, 1);
            }
        }
    }

    public void end() {
        MBC.getInstance().setCurrentGame(this);
        setGameState(GameState.END_GAME);
        world.setTime(18000);
        createScoreboardEnd();
        loadPlayersEnd();
        MBC.getInstance().lobby.populatePodium();
        stopTimer();
        setTimer(28);
    }

    public void revealTeam(MBCTeam t, int place) {
        colorPodium(place, t.getConcrete().getType());
        MBC.sendTitle(t.teamNameFormat(), "with " + t.getMultipliedTotalScore() + " points", 20, 90, 20);
        Bukkit.broadcastMessage("\n" + t.teamNameFormat() + " with " + t.getMultipliedTotalScore() + " points!\n");
        MBC.spawnFirework(placeLocation(place), t.getColor());
        for (Participant p : MBC.getInstance().participants) {
            if (p.getTeam().getIcon().equals(t.getIcon())) {
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(placeLocation(place));
            }
            createLine(teamScoreForDisplay(place), String.format("%s: %.1f", t.teamNameFormat(), t.getMultipliedTotalScore()), p);
        }
    }

    public void revealIndiv(Participant p, int place) {
        Location[] locs = {
            new Location(this.world, -13.5, 5, -58.5, -0, 0),
            new Location(this.world, -9.5, 4, -60.5, -0, 0),
             new Location(this.world, -5.5, 3, -61.5, -0, 0),
             new Location(this.world, -1.5, 2, -62.5, -0, 0),
            new Location(this.world, 2.5, 1, -62.5, -0, 0),
            new Location(this.world, 6.5, 0, -61.5, -0, 0),
            new Location(this.world, 10.5, -1, -60.5, -0, 0),
            new Location(this.world, 14.5, -2, -58.5, -0, 0),
        };

        MBC.sendTitle(p.getFormattedName(), "with " + p.getRawTotalScore() + " points", 20, 90, 20);
        Bukkit.broadcastMessage("\n" + p.getFormattedName()+ " with " + p.getRawTotalScore() + " points!\n");
        MBC.spawnFirework(locs[place], p.getTeam().getColor());
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().teleport(locs[place-1]);
    }

    // lol
    private int teamScoreForDisplay(int place) {
        return switch (place) {
            case 1 -> 14; //+13
            case 2 -> 13; // +11
            case 3 -> 12; // +9
            case 4 -> 11; // +7
            case 5 -> 10; // +5
            case 6 -> 9; // +3
            default -> -1;
        };
    }

    public void createScoreboardEnd() {
        newObjective();
        for (Participant p : MBC.getInstance().participants) {
            newObjective(p);
            createLine(21, ChatColor.RED+""+ChatColor.BOLD+"Event ends in:", p);
            createLine(19, ChatColor.RESET.toString(), p);
            createLine(15, ChatColor.GREEN+"Final Scores: ", p);
            createLine(4, ChatColor.RESET.toString()+ChatColor.RESET, p);
        }
        updateTeamStandings();
    }
    /*
    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        } else if (e.getEntity() instanceof SkeletonHorse) {
            e.setCancelled(true);
        }
    }
         */

    // prevent leaving cutscenes
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!(getState().equals(GameState.END_ROUND) && getState().equals(GameState.TUTORIAL))) return;

        if (e.getPlayer().getSpectatorTarget() != null && e.getPlayer().getSpectatorTarget().equals(cameraman)) {
            e.setCancelled(true);
        }
    }

    /**
     * Updates the player's total score in lobby
     * Your Coins: {COIN_AMOUNT}
     * @param p Participant whose scoreboard to update
     */
    public void updatePlayerTotalScoreDisplay(Participant p) {
        createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getRawTotalScore(), p);
    }

    @Override
    public void loadPlayers() {
        world.setTime(6000);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(LOBBY);
        }
    }

    public void loadPlayersScoreReveal() {
        cameraman = (ArmorStand) world.spawnEntity(new Location(world, -15.5, 0, -36.5, 90, -10), EntityType.ARMOR_STAND);
        cameraman.setInvisible(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            // probably better to have a global but doesn't matter for rn
            p.teleport(new Location(world, -15.5, 0, -36.5, 90, -10));
            p.setGameMode(GameMode.SPECTATOR);
            p.setSpectatorTarget(cameraman);
        }
    }

    public void loadPlayersIndivReveal() {
        cameraman = (ArmorStand) world.spawnEntity(new Location(world, 0.5, 0, -44, -180, 0), EntityType.ARMOR_STAND);
        cameraman.setInvisible(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            // probably better to have a global but doesn't matter for rn
            p.teleport(new Location(world, 0.5, 0, -44, -180, 0));
            p.setGameMode(GameMode.SPECTATOR);
            p.setSpectatorTarget(cameraman);
        }
    }

    public void prepareScoreReveal() {
        MBC.getInstance().setCurrentGame(this);
        MBC.npcManager.removeAllNPCs();
        setGameState(GameState.END_ROUND);
        world.setTime(13000);
        colorPodiumsWhite();
        createScoreboardTeamReveal();
        teamBarriers(true);
        loadPlayersIndivReveal();
        stopTimer();
        setTimer(220);
    }

    public void loadPlayersEnd() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().setMaxHealth(20);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            if (p.winner) {
                p.getPlayer().teleport(new Location(world, 76.5, 11, -20.5, 90, 0));
                p.getPlayer().getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
            } else {
                p.getPlayer().teleport(new Location(world, 62.5, 4, -20.5, -90, -25));
            }
            p.getPlayer().playSound(p.getPlayer(), Sound.MUSIC_DISC_WARD, SoundCategory.RECORDS, 1, 1);
        }
    }

    public void colorPodiumsWhite() {
        for (int i = 1; i < 7; i++) {
            colorPodium(i, Material.WHITE_CONCRETE);
        }
    }

    public void toFinale() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        setGameState(GameState.INACTIVE);
        miniBeepEnd("Mini Beep has ended due to quickfire!");
        endPvp();
        miniBeepStartable = false;
        pvpStartable = false;
        toQuickfire();
        // toDodgebolt();
    }

    public void toQuickfire() {
        if (MBC.getInstance().quickfire == null) {
            if (reveal.size() < 2) {
                MBC.getInstance().quickfire = new Quickfire();
            } else {
                MBC.getInstance().quickfire = new Quickfire(reveal.get(reveal.size()-1), reveal.get(reveal.size()-2));
            }
        }

        HandlerList.unregisterAll(MBC.getInstance().lobby);
        HandlerList.unregisterAll(MBC.getInstance().decisionDome);

        //MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().dodgebolt, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 255, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
        }

        //MBC.getInstance().dodgebolt.start();
        MBC.getInstance().quickfire.start();
    }

    public void startingEvents(int timeRemaining) {
        if (timeRemaining == 14) {
            MBC.announce("The Minecraft Championship is about to begin!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.ADVENTURE);
                p.teleport(LOBBY);
            }
            cameraman.remove();
        } else if (timeRemaining == 63) {
            world.setTime(23225);
            miniBeepEnd("Mini beep has ended due to the event beginning!");
            endPvp();
            miniBeepStartable = false;
            pvpStartable = false;
            createLineAll(21, ChatColor.RED+""+ChatColor.BOLD + "Event begins in: ");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 110, 1, false, false));
            }
        } else if (timeRemaining == 60) {
            cameraman = (ArmorStand) world.spawnEntity(new Location(world, 0.5, 0, 8.5, 180, -11), EntityType.ARMOR_STAND);
            cameraman.setCustomName(ChatColor.RED + "" + ChatColor.BOLD + "Camera here!");
            cameraman.setInvisible(true);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p, Sound.MUSIC_DISC_STRAD, SoundCategory.RECORDS, 1, 1);
                p.removePotionEffect(PotionEffectType.BLINDNESS);
                p.setGameMode(GameMode.SPECTATOR);
                p.setSpectatorTarget(cameraman);
            }
        } else if (timeRemaining == 59) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(ChatColor.GOLD.toString() + ChatColor.BOLD + "Introducing:", " ", 20, 60, 20);
            }
        } else if (timeRemaining < 60 && timeRemaining > 14 && timeRemaining % 7 == 0) {
            introTeam();
        }
    }
    /*
    public void toDodgebolt() {
        if (MBC.getInstance().dodgebolt == null) {
            if (reveal.size() < 2) {
                MBC.getInstance().dodgebolt = new Dodgebolt();
            } else {
                MBC.getInstance().dodgebolt = new Dodgebolt(reveal.get(reveal.size()-1), reveal.get(reveal.size()-2));
            }
        }
        //MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().dodgebolt, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
        }

        MBC.getInstance().dodgebolt.start();
    }
     */

    private void introTeam() {
        List<MBCTeam> teams = getValidTeams();
        if (introCounter >= teams.size()) {
            timeRemaining = 15;
        } else {
            MBCTeam intro = teams.get(introCounter++);
            if (!lastIntro.isEmpty()) {
                for (Participant p : lastIntro) {
                    p.getPlayer().setGameMode(GameMode.SPECTATOR);
                    p.getPlayer().setSpectatorTarget(cameraman);
                }
                lastIntro.clear();
            }
            lastIntro.addAll(intro.getPlayers());
            String introPlayers = "";
            for (Participant p : intro.getPlayers()) {
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().getInventory().clear();
                MBC.spawnFirework(new Location(world, 0.5, 2, 0.5), p.getTeam().getColor());
                p.getPlayer().teleport(new Location(world, 0, 1, 0, 0, 0));
                introPlayers += p.getFormattedName() + ", ";
            }
            introPlayers = introPlayers.substring(0, introPlayers.length()-2);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getPlayer().sendTitle(intro.teamNameFormat(), introPlayers, 20, 60, 20);
            }
        }
    }

    public void colorPodiums() {
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            colorPodium(t.getPlace(), t.getConcrete().getType());
        }
    }

    /*
     * Where to teleport players/spawn firework depending on
     * their team placement podium location
     */
    private Location placeLocation(int place) {
        switch(place) {
            case 6 -> {
                return new Location(world, -25, 1.5, -29);
            }
            case 5 -> {
                return new Location(world, -26, 2.5, -32);
            }
            case 4 -> {
                return new Location(world, -27, 3.5, -35);
            }
            case 3 -> {
                return new Location(world, -27, 4.5, -38);
            }
            case 2 -> {
                return new Location(world, -26, 5.5, -41);
            }
            case 1 -> {
                return new Location(world, -25, 6.5, -44);
            }
            default -> {
                return null;
            }
        }
    }

    public void populatePodium() {
        for (NPC npc : podiumNPCS) {
            MBC.npcManager.remove(npc);
        }

        podiumNPCS = new ArrayList<>();
        Location[] locs = {
                new Location(this.world, -13.5, 5, -58.5, -0, 0),
                new Location(this.world, -9.5, 4, -60.5, -0, 0),
                 new Location(this.world, -5.5, 3, -61.5, -0, 0),
                 new Location(this.world, -1.5, 2, -62.5, -0, 0),
                new Location(this.world, 2.5, 1, -62.5, -0, 0),
                new Location(this.world, 6.5, 0, -61.5, -0, 0),
                new Location(this.world, 10.5, -1, -60.5, -0, 0),
                new Location(this.world, 14.5, -2, -58.5, -0, 0),
        };

        /*
        Location[][] locs = {
                { new Location(this.world, -62.5, 15, -7.5, -90, 0), new Location(this.world, -9.5, 3, -33.5, -0, 0)},
                { new Location(this.world, -65.5, 14, -5.5, -90, 0), new Location(this.world, -7.5, 2, -36.5, -0, 0)},
                { new Location(this.world, -67.5, 13, -2.5, -90, 0), new Location(this.world, -4.5, 1, -38.5, -0, 0)},
                { new Location(this.world, -67.5, 12, .5, -90, 0), new Location(this.world, -1.5, 0, -39.5, -0, 0)},
                { new Location(this.world, -67.5, 12, 3.5, -90, 0), new Location(this.world, 2.5, 0, -39.5, -0, 0)},
                { new Location(this.world, -65.5, 11, 6.5, -90, 0), new Location(this.world, 5.5, 0, -38.5, -0, 0)},
                { new Location(this.world, -62.5, 11, 8.5, -90, 0), new Location(this.world, 8.5, 0, -36.5, -0, 0)},
                { new Location(this.world, -59.5, 11, 9.5, -90, 0), new Location(this.world, 10.5, 0, -33.5, -0, 0)},
        };
         */

        List<Participant> individual = MBC.getInstance().getPlayers();
        individual.sort(new TotalIndividualComparator());

        Leaderboard gameLeaderboard;

        if (individualLeaderboards.isEmpty()) {
            gameLeaderboard = new Leaderboard(individual, 1);
        }
        else {
            Leaderboard lastBoard = individualLeaderboards.getFirst();

            lastBoard.RemoveStands();

            gameLeaderboard = new Leaderboard(individual, lastBoard, individualLeaderboards.size());

            individualLeaderboards.removeFirst();
        }

        Leaderboard individualLeaderboard = new Leaderboard(individual, 0);
        individualLeaderboards.addFirst(individualLeaderboard);
        individualLeaderboards.add(gameLeaderboard);

        gameLeaderboard.spawnLeaderboard();
        individualLeaderboard.spawnLeaderboard();

        for (int i = 0; i < 8; i++) {
            Player p;
            if (i < individual.size()) {
                p = individual.get(i).getPlayer();
            } else {
                return;
            }

            Location l_0 = locs[i];
            //Location l_1 = locs[i][1];

            NPC npc1 = MBC.npcManager.createNPC(p,l_0);
            //NPC npc2 = MBC.npcManager.createNPC(p,l_1);

            //MBC.npcManager.showAll(npc1);
            //MBC.npcManager.showAll(npc2);
            podiumNPCS.add(npc1);
            //podiumNPCS.add(npc2);
        }
    }

    private void colorPodium(int place, Material m) {
        switch (place) {
            case 6 -> {
                world.getBlockAt(-26, 0, -30).setType(m);
                world.getBlockAt(-25, 0, -30).setType(m);
                world.getBlockAt(-26, 0, -29).setType(m);
                world.getBlockAt(-25, 0, -29).setType(m);
            }
            case 5 -> {
                for (int y = 0; y <= 1; y++) {
                    world.getBlockAt(-27, y, -33).setType(m);
                    world.getBlockAt(-27, y, -32).setType(m);
                    world.getBlockAt(-26, y, -33).setType(m);
                    world.getBlockAt(-26, y, -32).setType(m);
                }
            }
            case 4 -> {
                for (int y = 0; y <= 2; y++) {
                    world.getBlockAt(-28, y, -36).setType(m);
                    world.getBlockAt(-28, y, -35).setType(m);
                    world.getBlockAt(-27, y, -36).setType(m);
                    world.getBlockAt(-27, y, -35).setType(m);
                }
            }
            case 3 -> {
                for (int y = 0; y <= 3; y++) {
                    world.getBlockAt(-28, y, -39).setType(m);
                    world.getBlockAt(-28, y, -38).setType(m);
                    world.getBlockAt(-27, y, -39).setType(m);
                    world.getBlockAt(-27, y, -38).setType(m);
                }
            }
            case 2 -> {
                for (int y = 0; y <= 4; y++) {
                    world.getBlockAt(-27, y, -41).setType(m);
                    world.getBlockAt(-27, y, -42).setType(m);
                    world.getBlockAt(-26, y, -41).setType(m);
                    world.getBlockAt(-26, y, -42).setType(m);
                }
            }
            case 1 -> {
                for (int y = 0; y <= 5; y++) {
                    world.getBlockAt(-26, y, -45).setType(m);
                    world.getBlockAt(-25, y, -45).setType(m);
                    world.getBlockAt(-26, y, -44).setType(m);
                    world.getBlockAt(-25, y, -44).setType(m);
                }
            }
        }
    }

    public boolean inBeepArea(Player p) {
        Location l = p.getLocation();
        return l.getY() >= MINI_BEEP_NW.getY() && l.getY() <= MINI_BEEP_SE.getY()
            && l.getX() >= MINI_BEEP_NW.getX() && l.getX() <= MINI_BEEP_SE.getX()
            && l.getZ() >= MINI_BEEP_NW.getZ() && l.getZ() <= MINI_BEEP_SE.getZ();
    }

    // true -> game active, false -> game inactive
    public void beepBorders(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;
        Material m2 = b ? Material.AIR : Material.BARRIER;

        for (int y =  -2; y < 8; y++) {
            for (int z = 46; z <= 80; z++) {
                world.getBlockAt(94, y, z).setType(m);
                world.getBlockAt(68, y, z).setType(m);
            }
            for (int z = 50; z <= 76; z++) {
                world.getBlockAt(94, y, z).setType(Material.BARRIER);
                world.getBlockAt(68, y, z).setType(Material.BARRIER);
            }
            for (int x = 69; x < 94; x++) {
                world.getBlockAt(x, y, 46).setType(m);
                world.getBlockAt(x, y, 80).setType(m);
                world.getBlockAt(x, y, 50).setType(m2);
                world.getBlockAt(x, y, 76).setType(m2);
            }
        }
    }

    private void loadCourses() {
        easyLevels = BeepTestLevelLoader.loadEasyLevels();
        regularLevels = BeepTestLevelLoader.loadRegularLevels();
        mediumLevels = BeepTestLevelLoader.loadMediumLevels();
        hardLevels = BeepTestLevelLoader.loadHardLevels();
    }

    public void addBeepPlayer(Player p) {
        if (activeBeep) return;

        if (miniBeepers.isEmpty()) {
            miniBeepStart();
        }
        p.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Joined Mini Beep!"));
        miniBeepers.add(Participant.getParticipant(p));
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
    }

    public void removeBeepPlayer(Player p) {
        p.teleport(new Location(world, 81.5, -4, 38.5, -180, 0));
        miniBeepers.remove(Participant.getParticipant(p));
        if (miniBeepers.isEmpty()) {
            miniBeepEnd();
        }
    }

    public void beepPlayerEliminated(Player p, boolean b) {
        if (!activeBeep) return;

        // b is true if eliminated by y coord, false if eliminated by z coord

        p.playSound(p, Sound.ENTITY_BAT_DEATH, 1, 1);
        if (b) {
            if (beepRound == 1) {
                p.sendMessage("You were eliminated by " + ChatColor.AQUA + currentLevel.getName().trim() + "!");
            } else if (beepRound == 2) {
                p.sendMessage("You were eliminated by " + ChatColor.GREEN + currentLevel.getName().trim() + "!");
            } else if (beepRound == 3) {
                p.sendMessage("You were eliminated by " + ChatColor.YELLOW + currentLevel.getName().trim() + "!");
            } else {
                p.sendMessage("You were eliminated by " + ChatColor.RED + currentLevel.getName().trim() + "!");
            }
        }
        else {
            if (beepRound == 2) {
                p.sendMessage("You were eliminated by " + ChatColor.AQUA + lastLevelName.trim() + "!");
            } else if (beepRound == 3) {
                p.sendMessage("You were eliminated by " + ChatColor.GREEN + lastLevelName.trim() + "!");
            } else if (beepRound == 4) {
                p.sendMessage("You were eliminated by " + ChatColor.YELLOW + lastLevelName.trim() + "!");
            } else {
                p.sendMessage("You were eliminated by " + ChatColor.RED + lastLevelName.trim() + "!");
            }
        }
        
        removeBeepPlayer(p);

        for (Participant part : miniBeepers) {
            part.getPlayer().sendMessage(Participant.getParticipant(p).getFormattedName() + " was eliminated from Mini Beep.");
        }
    }

    public void setBeepTimer(int time) {
        if (beepTestTimeRemaining != -1) {
            stopBeepTimer();
        }

        beepTestTimeRemaining = time;

        taskBeepID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
            --beepTestTimeRemaining;
            
            miniBeepEvents();
            if (beepTestTimeRemaining < 0) {
                stopBeepTimer();
            }
        }, 20, 20);
    }

    public void stopBeepTimer() {
        MBC.getInstance().cancelEvent(taskBeepID);
    }

    private void chooseLevel() {
        List<BeepTestLevel> chooseFrom = null;
        if (beepRound < 1) {
            chooseFrom = easyLevels;
        } else if (beepRound < 2) {
            chooseFrom = regularLevels;
        } else if (beepRound < 3) {
            chooseFrom = mediumLevels;
        } else {
            chooseFrom = hardLevels;
        }

        // select random level
        int rand = (int) (Math.random() * chooseFrom.size());
        currentLevel = chooseFrom.get(rand);
        chooseFrom.remove(rand);
        beepRound++;

        changeBeepMap();
    }

    public void changeBeepMap() {

        for (int x = 69; x <= 93; x++) {
            for (int y = -6; y <= 7; y++) {
                for (int z = 51; z <= 75; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

        if (beepRound % 2 == 1) {
            EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld("world")));
            ForwardExtentCopy copy = new ForwardExtentCopy(BukkitAdapter.adapt(Bukkit.getWorld("Party")), currentLevel.getReversedRegion(), BukkitAdapter.asBlockVector(currentLevel.getPasteReversed()), editSession, BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("world"), 69, -6, 75)));
            try {
                Operations.complete(copy);
                editSession.close();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } else {
            EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld("world")));
            ForwardExtentCopy copy = new ForwardExtentCopy(BukkitAdapter.adapt(Bukkit.getWorld("Party")), currentLevel.getRegion(), BukkitAdapter.asBlockVector(currentLevel.getPasteFrom()), editSession, BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("world"), 69, -6, 75)));
            try {
                Operations.complete(copy);
                editSession.close();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
    }

    public void miniBeepStart() {
        loadCourses();
        setBeepTimer(75);
    }

    public void miniBeepEnd() {
        for (int x = 69; x <= 93; x++) {
            for (int y = -6; y <= 7; y++) {
                for (int z = 51; z <= 75; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
        if (activeBeep) {
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Mini Beep has ended!");
        }
        
        beepBorders(false);
        beepTestTimeRemaining = -1;
        beepRound = 0;
        activeBeep = false;
        lastLevelName = "";
        stopBeepTimer();
        currentLevel = null;
        easyLevels = null;
        regularLevels = null;
        mediumLevels = null;
        hardLevels = null;
        miniBeepers.clear();
    }

    public void miniBeepEnd(String s) {
        for (Participant p : miniBeepers) { 
            p.getPlayer().sendMessage(ChatColor.RED + "" + s);
        }
        miniBeepEnd();
    }

    public void miniBeepEvents() {

        switch(beepTestTimeRemaining) {
            case 65 -> {
                for (Participant p : miniBeepers) {
                    p.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Mini Beep starts in 5 seconds!");
                }
            }  
            case 60 -> {
                beepBorders(true);
                chooseLevel();
                activeBeep = true;

                for (int i = miniBeepers.size() -1; i >= 0; i--) {
                    
                    Player player = miniBeepers.get(i).getPlayer();
                    player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.BLOCKS, 1, 1);
                    if (miniBeepers.size() == 1) player.sendMessage(ChatColor.LIGHT_PURPLE + "Mini Beep has begun with 1 player!");
                    else player.sendMessage(ChatColor.LIGHT_PURPLE + "Mini Beep has begun with " + miniBeepers.size() + " players!");
                    player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA + "" + currentLevel.getName().trim()));
                }
            }
            case 48 -> {
                for (Participant p : miniBeepers) {
                    Player player = p.getPlayer();
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "3 seconds remain!");
                }
            }
            case 45 -> {
                lastLevelName = currentLevel.getName().trim();
                chooseLevel();

                for (int i = miniBeepers.size() -1; i >= 0; i--) {
                    if (checkPlayerDeath(miniBeepers.get(i))) continue;
                    Player player = miniBeepers.get(i).getPlayer();
                    player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 1);
                    player.sendMessage("You completed " + ChatColor.AQUA + lastLevelName + "!");
                    player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "" + currentLevel.getName().trim()));
                }         
            }
            case 33 -> {
                for (Participant p : miniBeepers) {
                    Player player = p.getPlayer();
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "3 seconds remain!");
                }
            }
            case 30 -> {
                lastLevelName = currentLevel.getName().trim();
                chooseLevel();

                for (int i = miniBeepers.size() -1; i >= 0; i--) {
                    if (checkPlayerDeath(miniBeepers.get(i))) continue;
                    Player player = miniBeepers.get(i).getPlayer();
                    player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 1);
                    player.sendMessage("You completed " + ChatColor.GREEN + lastLevelName + "!");
                    player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "" + currentLevel.getName().trim()));
                }  
            }
            case 18 -> {
                for (Participant p : miniBeepers) {
                    Player player = p.getPlayer();
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "3 seconds remain!");
                }
            }
            case 15 -> {
                lastLevelName = currentLevel.getName().trim();
                chooseLevel();

                for (int i = miniBeepers.size() -1; i >= 0; i--) {
                    if (checkPlayerDeath(miniBeepers.get(i))) continue;
                    Player player = miniBeepers.get(i).getPlayer();
                    player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 1);
                    player.sendMessage("You completed " + ChatColor.YELLOW + lastLevelName + "!");
                    player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "" + currentLevel.getName().trim()));
                }  
            }
            case 3 -> {
                for (Participant p : miniBeepers) {
                    Player player = p.getPlayer();
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "3 seconds remain!");
                }
            }
            case 0 -> {
                beepRound++;

                String survivors = ChatColor.BOLD + "Players have survived Mini Beep!\n";

                for (int i = miniBeepers.size() -1; i >= 0; i--) {
                    if (checkPlayerDeath(miniBeepers.get(i))) continue;
                    Player player = miniBeepers.get(i).getPlayer();
                    player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                    player.sendMessage("You completed " + ChatColor.RED + currentLevel.getName().trim() + "!");
                    survivors = survivors + miniBeepers.get(i).getFormattedName() + ", ";
                }  
                
                if (miniBeepers.size() > 0) {
                    survivors = survivors.substring(0, survivors.length()-2);
                    Bukkit.broadcastMessage(survivors);
                }

                miniBeepEnd();

                

            }
                
        }

        
    }

    public boolean checkPlayerDeath(Participant p) {
        if (beepRound % 2 == 0) {
            if (p.getPlayer().getZ() < BEEP_NORMAL_Z) {
                beepPlayerEliminated(p.getPlayer(), false);
                p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 1);
                return true;
            }
        }
        else {
            if (p.getPlayer().getZ() > BEEP_OPPOSITE_Z) {
                beepPlayerEliminated(p.getPlayer(), false);
                p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 1);
                return true;
            }
        }

        return false;
    }

    public void teamBarriers(boolean barriers) {
        Material m = barriers ? Material.BARRIER : Material.AIR;

        // first place
        world.getBlockAt(-27, 7, -44).setType(m);
        world.getBlockAt(-27, 7, -45).setType(m);
        world.getBlockAt(-26, 7, -46).setType(m);
        world.getBlockAt(-25, 7, -46).setType(m);
        world.getBlockAt(-24, 7, -45).setType(m);
        world.getBlockAt(-24, 7, -44).setType(m);
        world.getBlockAt(-25, 7, -43).setType(m);
        world.getBlockAt(-26, 7, -43).setType(m);

        // second place
        world.getBlockAt(-26, 6, -40).setType(m);
        world.getBlockAt(-27, 6, -40).setType(m);
        world.getBlockAt(-28, 6, -41).setType(m);
        world.getBlockAt(-28, 6, -42).setType(m);
        world.getBlockAt(-27, 6, -43).setType(m);
        world.getBlockAt(-26, 6, -43).setType(m);
        world.getBlockAt(-25, 6, -42).setType(m);
        world.getBlockAt(-25, 6, -41).setType(m);

        // third place
        world.getBlockAt(-28, 5, -37).setType(m);
        world.getBlockAt(-27, 5, -37).setType(m);
        world.getBlockAt(-26, 5, -38).setType(m);
        world.getBlockAt(-26, 5, -39).setType(m);
        world.getBlockAt(-27, 5, -40).setType(m);
        world.getBlockAt(-28, 5, -40).setType(m);
        world.getBlockAt(-29, 5, -39).setType(m);
        world.getBlockAt(-29, 5, -38).setType(m);

        // fourth place
        world.getBlockAt(-27, 4, -34).setType(m);
        world.getBlockAt(-28, 4, -34).setType(m);
        world.getBlockAt(-29, 4, -35).setType(m);
        world.getBlockAt(-29, 4, -36).setType(m);
        world.getBlockAt(-28, 4, -37).setType(m);
        world.getBlockAt(-27, 4, -37).setType(m);
        world.getBlockAt(-26, 4, -36).setType(m);
        world.getBlockAt(-26, 4, -35).setType(m);

        // fifth place
        world.getBlockAt(-27, 3, -31).setType(m);
        world.getBlockAt(-26, 3, -31).setType(m);
        world.getBlockAt(-25, 3, -32).setType(m);
        world.getBlockAt(-25, 3, -33).setType(m);
        world.getBlockAt(-26, 3, -34).setType(m);
        world.getBlockAt(-27, 3, -34).setType(m);
        world.getBlockAt(-28, 3, -33).setType(m);
        world.getBlockAt(-28, 3, -32).setType(m);

        // sixth place
        world.getBlockAt(-24, 2, -30).setType(m);
        world.getBlockAt(-24, 2, -29).setType(m);
        world.getBlockAt(-25, 2, -31).setType(m);
        world.getBlockAt(-25, 2, -28).setType(m);
        world.getBlockAt(-26, 2, -31).setType(m);
        world.getBlockAt(-26, 2, -28).setType(m);
        world.getBlockAt(-27, 2, -30).setType(m);
        world.getBlockAt(-27, 2, -29).setType(m);
    }

    public boolean inPVPArea(Player p) {
        Location l = p.getLocation();
        int x = (int)(l.getX());
        int z = (int)(l.getZ()) - 1;
        Block check = new Location(world, x, -6, z).getBlock();
        if (check.getType() == Material.WAXED_OXIDIZED_COPPER_BULB) {
            return true;
        }
        return false;
    }

    public void addPvpPlayer(Player p) {
        p.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Joined PVP!"));
        //MBC.getInstance().board.getTeam(Participant.getParticipant(p).getTeam().fullName).setAllowFriendlyFire(true);
        pvpers.put(Participant.getParticipant(p), 0);
        addPvpItems(p);
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.RESISTANCE);
        p.removePotionEffect(PotionEffectType.SATURATION);
    }

    public void addPvpItems(Player p) {
        p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
        p.getPlayer().getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        p.getPlayer().getInventory().addItem(new ItemStack(Material.STONE_AXE));
    }

    public void removePvpPlayer(Player p) {
        p.teleport(new Location(world, 109.5, -4, -59.5, -90, 0));
        p.getInventory().clear();
        p.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 255));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10, false, false));
        pvpers.remove(Participant.getParticipant(p));
    }

    public void endPvp() {
        for (Participant p : pvpers.keySet()) {
            Player player = p.getPlayer();
            removePvpPlayer(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setCancelled(true);
        Player killer = e.getPlayer().getKiller();
        if (killer == null) e.getPlayer().teleport(LOBBY);
        else {
            Participant killed = Participant.getParticipant(e.getPlayer());
            Participant k = Participant.getParticipant(killer);
            pvpers.replace(k, pvpers.get(k)+1);
            int streakKiller = pvpers.get(k);
            int streakKilled = pvpers.get(killed);
            removePvpPlayer(e.getPlayer());
            pvpDead.add(e.getPlayer());
            killer.setHealth(20);
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 255));

            if (streakKiller >= 3) {
                killer.sendMessage(new TextComponent(ChatColor.RED + "You are on a streak of " + pvpers.get(k) + "!"));
            }
            
            
            for (Participant p : pvpers.keySet()) {
                if (streakKilled >= 3) p.getPlayer().sendMessage(new TextComponent(killed.getFormattedName() + " was killed by " + k.getFormattedName() + ", breaking a streak of " + streakKilled + "!"));
                else p.getPlayer().sendMessage(new TextComponent(killed.getFormattedName() + " was killed by " + k.getFormattedName() + "!"));
            }
            if (streakKilled >= 3) e.getPlayer().sendMessage(new TextComponent(killed.getFormattedName() + " was killed by " + k.getFormattedName() + ", breaking a streak of " + streakKilled + "!"));
            else e.getPlayer().sendMessage(new TextComponent(killed.getFormattedName() + " was killed by " + k.getFormattedName() + "!"));

            killer.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Killed " + killed.getFormattedName()));
            killer.getPlayer().playSound(killer.getPlayer(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 0.5f, 1);
        }
        
        
    }

    
     //Ensure player to player damage is by pvpers
     
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            return;
        }

        Player damager = (Player)(e.getDamager());
        Player damaged = (Player)(e.getEntity());

        if (!(pvpers.keySet().contains(Participant.getParticipant(damager)) && pvpers.keySet().contains(Participant.getParticipant(damaged)))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        if (getState().equals(GameState.END_ROUND) && timeRemaining > 60 || getState().equals(GameState.TUTORIAL) && timeRemaining > 20) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().setSpectatorTarget(cameraman);
        } else {
            Player p = e.getPlayer();
            p.teleport(LOBBY);
            p.getInventory().clear();
            p.setInvulnerable(false);
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.removePotionEffect(PotionEffectType.RESISTANCE);
            p.addPotionEffect(MBC.SATURATION);
        }
    }

    /**
     * Ensures nothing is dropped by pvpers
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (pvpers.keySet().contains(p)) e.setCancelled(true);
        return;
   }
}
