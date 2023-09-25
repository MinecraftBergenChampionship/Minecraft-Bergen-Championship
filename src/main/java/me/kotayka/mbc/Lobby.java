package me.kotayka.mbc;

import me.kotayka.mbc.NPCs.NPC;
import me.kotayka.mbc.comparators.TeamScoreSorter;
import me.kotayka.mbc.comparators.TotalIndividualComparator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends Minigame {
    public static final Location LOBBY = new Location(Bukkit.getWorld("world"), 0.5, 1, 0.5, 180, 0);
    public final World world = Bukkit.getWorld("world");
    public ArmorStand cameraman;
    private List<MBCTeam> reveal;
    private int revealCounter = 0;

    private List<NPC> podiumNPCS = new ArrayList<>();

    public Lobby() {
        super("Lobby");
        Bukkit.getWorld("world").setTime(6000);
        colorPodiumsWhite();
        teamBarriers(false);
    }

    public void createScoreboard(Participant p) {
        newObjective(p);
        if (MBC.getInstance().gameNum > 1) {
            createLine(21, ChatColor.RED+""+ChatColor.BOLD+"Event resumes in: ", p);
        } else {
            createLine(21, ChatColor.RED+""+ChatColor.BOLD + "Event begins in:", p);
        }
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:", p);
        createLine(17, p.getTeam().getChatColor()+p.getTeam().getTeamFullName(), p);
        createLine(16, ChatColor.RESET+ChatColor.RESET.toString()+ChatColor.RESET, p);
        createLine(15, ChatColor.GREEN+"Team Leaderboard: ", p);
        createLine(4, ChatColor.RESET.toString()+ChatColor.RESET, p);
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
        createLine(17, p.getTeam().getChatColor()+p.getTeam().getTeamFullName(), p);
    }

    public void events() {
        if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 0) {
                toVoting();
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                toDodgebolt();
            }
            if (revealCounter < 3) {
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
                    Bukkit.broadcastMessage(title+"...");
                }
            }
            switch (timeRemaining) {
                case 127 -> {
                    reveal = getValidTeams();
                    reveal.sort(new TeamScoreSorter());
                    Bukkit.broadcastMessage("[Debug] reveal == " + reveal);
                }
                case 95 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"Now for the Dodgebolt Qualifiers!");
                }
                case 90 -> {
                    String title = String.format("In %s1st", ChatColor.GOLD);
                    Bukkit.broadcastMessage(title+"...");
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
                    Bukkit.broadcastMessage(ChatColor.BOLD+"Finally...");
                }
                case 79 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"The team in " + ChatColor.GRAY+ "2nd"+ ChatColor.WHITE + ChatColor.BOLD+" place...");
                    MBC.sendTitle(ChatColor.BOLD+"In " + ChatColor.GRAY+"2nd", " ", 20, 140, 20);
                }
                case 77 -> {
                    if (reveal.size() <= 2) { return; }
                    MBCTeam two = reveal.get(reveal.size()-2);
                    MBCTeam third = reveal.get(reveal.size()-3);
                    Bukkit.broadcastMessage("With a gap of " + (two.getMultipliedTotalScore() - third.getMultipliedTotalScore()) + " points...");
                    MBC.sendTitle(ChatColor.BOLD+"In " + ChatColor.GRAY+"2nd", "With " + two.getMultipliedTotalScore() + " points", 0, 140, 20);
                }
                case 75 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"Playing in dodgebolt...");
                }
                case 73 -> {
                    Bukkit.broadcastMessage(ChatColor.BOLD+"is...");
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
                case 66 -> {
                    Bukkit.broadcastMessage("Which leaves us with " + ChatColor.DARK_RED + "3rd"+ChatColor.WHITE+",");
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
                    boolean flag = false;
                    if (reveal.size() > 1) {
                        flag = true;
                    }
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
                    p.sendTitle(ChatColor.YELLOW+"Event Over!", "Thanks for playing!", 20, 60, 20);
                    createLineAll(21, ChatColor.GREEN.toString()+ChatColor.BOLD+"Event Over!");
                    createLineAll(20, "Thanks for playing!");
                }
            }
        }
    }

    public void toVoting() {
        HandlerList.unregisterAll(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().decisionDome, MBC.getInstance().plugin);
        MBC.getInstance().decisionDome.start();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.getPlayer().getWorld().equals(world)) return;

        if (getState().equals(GameState.END_ROUND) && e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) { e.setCancelled(true); }

        if (e.getPlayer().getLocation().getY() < -45){
            e.getPlayer().teleport(LOBBY);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!e.getPlayer().getLocation().getWorld().equals(world)) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType().equals(Material.DAYLIGHT_DETECTOR)) {
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
        setGameState(GameState.ACTIVE);
        createScoreboard();
        loadPlayers();
        updateTeamStandings();
        stopTimer();
        setTimer(120);
    }

    public void end() {
        MBC.getInstance().setCurrentGame(this);
        setGameState(GameState.END_GAME);
        createScoreboardEnd();
        world.setTime(18000);
        loadPlayersEnd();
        updateTeamStandings();
        stopTimer();
        setTimer(28);
    }

    public void revealTeam(MBCTeam t, int place) {
        colorPodium(place, t.getConcrete().getType());
        MBC.sendTitle(t.teamNameFormat(), "with " + t.getMultipliedTotalScore() + " points", 20, 90, 20);
        Bukkit.broadcastMessage(t.teamNameFormat() + " with " + t.getMultipliedTotalScore() + " points!");
        MBC.spawnFirework(placeLocation(place), t.getColor());
        for (Participant p : MBC.getInstance().participants) {
            if (p.getTeam().getIcon().equals(t.getIcon())) {
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(placeLocation(place));
            }
            createLine(teamScoreForDisplay(place), String.format("%s: %.1f", t.teamNameFormat(), t.getMultipliedTotalScore()), p);
        }
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

    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        } else if (e.getEntity() instanceof SkeletonHorse) {
            e.setCancelled(true);
        }
    }

    // prevent leaving cutscenes
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!getState().equals(GameState.END_ROUND)) return;

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
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(LOBBY);
        }
    }

    public void loadPlayersScoreReveal() {
        cameraman = (ArmorStand) world.spawnEntity(new Location(world, -14.5, -1, -21.5, 140, 0), EntityType.ARMOR_STAND);
        cameraman.setInvisible(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(LOBBY);
            p.setGameMode(GameMode.SPECTATOR);
            p.setSpectatorTarget(cameraman);
        }
    }

    public void prepareScoreReveal() {
        MBC.getInstance().setCurrentGame(this);
        setGameState(GameState.END_ROUND);
        world.setTime(13000);
        colorPodiumsWhite();
        createScoreboardTeamReveal();
        teamBarriers(true);
        loadPlayersScoreReveal();
        stopTimer();
        setTimer(130);
    }

    public void loadPlayersEnd() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            if (p.winner) {
                p.getPlayer().teleport(new Location(world, 49.5, 0.5, 0.5, 90, 0));
                p.getPlayer().getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
            } else {
                p.getPlayer().teleport(new Location(world, 38.5, -3, 0.5, -90, 0));
            }
            p.getPlayer().playSound(p.getPlayer(), Sound.MUSIC_DISC_WARD, SoundCategory.RECORDS, 1, 1);
        }
    }

    public void colorPodiumsWhite() {
        for (int i = 0; i < 6; i++) {
            colorPodium(i, Material.WHITE_CONCRETE);
        }
    }

    public void toDodgebolt() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        setGameState(GameState.INACTIVE);
        if (MBC.getInstance().dodgebolt == null) {
            if (reveal.size() < 2) {
                MBC.getInstance().dodgebolt = new Dodgebolt();
            } else {
                MBC.getInstance().dodgebolt = new Dodgebolt(reveal.get(reveal.size()-1), reveal.get(reveal.size()-2));
            }
        }
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().dodgebolt, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
        }

        MBC.getInstance().dodgebolt.start();
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
                return new Location(world, -18, -1, -24);
            }
            case 5 -> {
                return new Location(world, -17, 0, -28);
            }
            case 4 -> {
                return new Location(world, -18, 1, -32);
            }
            case 3 -> {
                return new Location(world, -20, 2, -29);
            }
            case 2 -> {
                return new Location(world, -24, 3, -27);
            }
            case 1 -> {
                return new Location(world, -25, 4, -31);
            }
            default -> {
                return null;
            }
        }
    }

    public void populatePodium() {
        for (NPC npc : podiumNPCS) {
            MBC.npcManager.removeAll(npc);
        }

        podiumNPCS = new ArrayList<>();

        Location[] locs = {
                new Location(this.world, -62.5, 15, -7.5, -90, 0),
                new Location(this.world, -65.5, 14, -5.5, -90, 0),
                new Location(this.world, -67.5, 13, -2.5, -90, 0),
                new Location(this.world, -67.5, 12, .5, -90, 0),
                new Location(this.world, -67.5, 12, 3.5, -90, 0),
                new Location(this.world, -65.5, 11, 6.5, -90, 0),
                new Location(this.world, -62.5, 11, 8.5, -90, 0),
                new Location(this.world, -59.5, 11, 9.5, -90, 0),
        };

        List<Participant> individual = MBC.getInstance().getPlayers();
        individual.sort(new TotalIndividualComparator());

        for (int i = 0; i < 8; i++) {
            Player p;
            if (i < individual.size()) {
                p = individual.get(i).getPlayer();
            } else {
                return;
            }

            Location l = locs[i];

            NPC npc = MBC.npcManager.createNPC(p,l);

            MBC.npcManager.showAll(npc);
            podiumNPCS.add(npc);
        }
    }

    private void colorPodium(int place, Material m) {
        switch (place) {
            case 6 -> {
                world.getBlockAt(-18, -2, -24).setType(m);
                world.getBlockAt(-18, -2, -25).setType(m);
                world.getBlockAt(-19, -2, -24).setType(m);
                world.getBlockAt(-19, -2, -25).setType(m);
            }
            case 5 -> {
                for (int y = -2; y <= -1; y++) {
                    world.getBlockAt(-17, y, -28).setType(m);
                    world.getBlockAt(-17, y, -29).setType(m);
                    world.getBlockAt(-18, y, -28).setType(m);
                    world.getBlockAt(-18, y, -29).setType(m);
                }
            }
            case 4 -> {
                for (int y = -3; y <= 0; y++) {
                    world.getBlockAt(-18, y, -32).setType(m);
                    world.getBlockAt(-18, y, -33).setType(m);
                    world.getBlockAt(-19, y, -32).setType(m);
                    world.getBlockAt(-19, y, -33).setType(m);
                }
            }
            case 3 -> {
                for (int y = -2; y <= 1; y++) {
                    world.getBlockAt(-20, y, -29).setType(m);
                    world.getBlockAt(-20, y, -30).setType(m);
                    world.getBlockAt(-21, y, -29).setType(m);
                    world.getBlockAt(-21, y, -30).setType(m);
                }
            }
            case 2 -> {
                for (int y = -3; y <= 2; y++) {
                    world.getBlockAt(-24, y, -27).setType(m);
                    world.getBlockAt(-24, y, -28).setType(m);
                    world.getBlockAt(-25, y, -27).setType(m);
                    world.getBlockAt(-25, y, -28).setType(m);
                }
            }
            case 1 -> {
                for (int y = -6; y <= 3; y++) {
                    world.getBlockAt(-25, y, -31).setType(m);
                    world.getBlockAt(-25, y, -32).setType(m);
                    world.getBlockAt(-26, y, -31).setType(m);
                    world.getBlockAt(-26, y, -32).setType(m);
                }
            }
        }
    }

    public void teamBarriers(boolean barriers) {
        Material m = barriers ? Material.BARRIER : Material.AIR;

        // first place
        world.getBlockAt(-27, 5, -31).setType(m);
        world.getBlockAt(-27, 5, -32).setType(m);
        world.getBlockAt(-26, 5, -33).setType(m);
        world.getBlockAt(-25, 5, -33).setType(m);
        world.getBlockAt(-24, 5, -32).setType(m);
        world.getBlockAt(-24, 5, -31).setType(m);
        world.getBlockAt(-25, 5, -30).setType(m);
        world.getBlockAt(-26, 5, -30).setType(m);

        // second place
        world.getBlockAt(-26, 4, -27).setType(m);
        world.getBlockAt(-26, 4, -28).setType(m);
        world.getBlockAt(-25, 4, -29).setType(m);
        world.getBlockAt(-24, 4, -29).setType(m);
        world.getBlockAt(-23, 4, -28).setType(m);
        world.getBlockAt(-23, 4, -27).setType(m);
        world.getBlockAt(-24, 4, -26).setType(m);
        world.getBlockAt(-25, 4, -26).setType(m);

        // third place
        world.getBlockAt(-22, 3, -29).setType(m);
        world.getBlockAt(-22, 3, -30).setType(m);
        world.getBlockAt(-21, 3, -31).setType(m);
        world.getBlockAt(-20, 3, -31).setType(m);
        world.getBlockAt(-19, 3, -30).setType(m);
        world.getBlockAt(-19, 3, -29).setType(m);
        world.getBlockAt(-20, 3, -28).setType(m);
        world.getBlockAt(-21, 3, -28).setType(m);

        // fourth place
        world.getBlockAt(-20, 2, -32).setType(m);
        world.getBlockAt(-20, 2, -33).setType(m);
        world.getBlockAt(-19, 2, -34).setType(m);
        world.getBlockAt(-18, 2, -34).setType(m);
        world.getBlockAt(-17, 2, -33).setType(m);
        world.getBlockAt(-17, 2, -32).setType(m);
        world.getBlockAt(-18, 2, -31).setType(m);
        world.getBlockAt(-19, 2, -31).setType(m);

        // fifth place
        world.getBlockAt(-19, 1, -28).setType(m);
        world.getBlockAt(-19, 1, -29).setType(m);
        world.getBlockAt(-18, 1, -30).setType(m);
        world.getBlockAt(-17, 1, -30).setType(m);
        world.getBlockAt(-16, 1, -29).setType(m);
        world.getBlockAt(-16, 1, -28).setType(m);
        world.getBlockAt(-17, 1, -27).setType(m);
        world.getBlockAt(-18, 1, -27).setType(m);

        // sixth place
        world.getBlockAt(-20, 0, -24).setType(m);
        world.getBlockAt(-20, 0, -25).setType(m);
        world.getBlockAt(-19, 0, -26).setType(m);
        world.getBlockAt(-18, 0, -26).setType(m);
        world.getBlockAt(-17, 0, -25).setType(m);
        world.getBlockAt(-17, 0, -24).setType(m);
        world.getBlockAt(-18, 0, -23).setType(m);
        world.getBlockAt(-19, 0, -23).setType(m);
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        if (getState().equals(GameState.END_ROUND) && timeRemaining > 60) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().setSpectatorTarget(cameraman);
        } else {
            e.getPlayer().teleport(LOBBY);
        }
    }
}
