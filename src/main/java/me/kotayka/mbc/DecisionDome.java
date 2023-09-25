package me.kotayka.mbc;

import me.kotayka.mbc.teams.Spectator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Current voting gimmick ; support for 8 games at the moment
 * Most quadrant/section code provided by @Jack-Crowley
 */
public class DecisionDome extends Minigame {
    private final World world = Bukkit.getWorld("DecisionDome");
    private boolean revealedGames;
    private List<String> gameNames = new ArrayList<>(Arrays.asList("TGTTOS", "Ace Race", "Survival Games", "Skybattle", "Spleef","BuildMart"));
    private List<VoteChicken> chickens = new ArrayList<>(MBC.getInstance().getPlayers().size());
    private final Map<Material, Section> sections = new HashMap<>(8);
    public Set<Player> voters = new HashSet<>();
    private final int[][] coordsForBorder = {
            {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, -1}, {0, -2}, {0, -3}, {0, -4}, {0, -5}, {0, -6}, {0, -7},
            {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {-1, 0}, {-2, 0}, {-3, 0}, {-4, 0}, {-5, 0}, {-6, 0}, {-7, 0},
            {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {1, -1}, {2, -2}, {3, -3}, {4, -4}, {5, -5}, {-1, 1}, {-2, 2}, {-3, 3}, {-4, 4},
            {-5, 5}, {-1, -1}, {-2, -2}, {-3, -3}, {-4, -4}, {-5, -5}
    };
    private int currentSection = (int)(Math.random()*gameNames.size());
    private Section winner;
    private boolean tie = false;

    public DecisionDome(boolean revealedGames) {
        super("DecisionDome");
        this.revealedGames = revealedGames;
        initSections();
    }

    @Override
    public void start() {
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);

        // Initialize variables and map
        removeEntities();
        if (!revealedGames) {
            initSections();
        }
        chickens.clear();
        tie = false;
        winner = null;

        // Deal with players
        loadPlayers();
        createScoreboard();

        if (sections.size() == 1) {
            // start only game
            for (Section s : sections.values()) {
                winner = s;
            }
            createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Warping to game: ");
            setGameState(GameState.END_GAME);
            setTimer(13);
        } else {
            // debug
            for (Section s : sections.values()) {
            }
            setGameState(GameState.STARTING);
            stopTimer();
            if (revealedGames) {
                setTimer(10);
            } else {
                setSectionsRed();
                setTimer(48);
            }
        }
    }

    @Override
    public void loadPlayers() {
        if (!voters.isEmpty()) {
            voters.clear();
        }
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            Location l;
            switch (t.getChatColor()) {
                case RED -> {
                    l = new Location(world, 7, -27, 13);
                    l.setYaw((float) 155.5);
                }
                case YELLOW -> {
                    l = new Location(world, -7, -27, 13);
                    l.setYaw((float) -145.5);
                }
                case GREEN -> {
                    l = new Location(world, -14, -27, 0);
                    l.setYaw((float) -90);
                }
                case BLUE -> {
                    l = new Location(world, -7, -27, -13);
                    l.setYaw((float) -25.5);
                }
                case DARK_PURPLE -> {
                    l = new Location(world, 7, -27, -13);
                    l.setYaw((float) 25.5);
                }
                case LIGHT_PURPLE -> {
                    l = new Location(world, 14, -27, 0);
                    l.setYaw((float) 90);
                }
                default -> l = new Location(world, 0, -27, 0);
            }
            for (Participant p : t.getPlayers()) {
                p.getPlayer().playSound(p.getPlayer(), Sound.MUSIC_DISC_CAT,SoundCategory.RECORDS, 1, 1);
                p.getPlayer().teleport(l);
                p.getPlayer().getInventory().clear();
                if (!(t instanceof Spectator)) {
                    p.getPlayer().setGameMode(GameMode.ADVENTURE);
                } else {
                    p.getPlayer().setGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    @Override
    public void events() {
        if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                timeRemaining = 45;
                // if (!revealedGames) revealedGames = true; the old object doesn't seem to persist but i can't exactly prove that
                setGameState(GameState.ACTIVE);
            }

            if (!revealedGames) {
                if (timeRemaining == 47) {
                    Bukkit.broadcastMessage(ChatColor.GREEN+""+ChatColor.BOLD+"Time to reveal the games!");
                }

                if (timeRemaining % 5 == 0 && timeRemaining != 0) {
                    revealGame(currentSection % 8);
                    currentSection++;
                }
            } else {
                if (timeRemaining == 9) {
                    MBC.getInstance().incrementMultiplier();
                    Powerups();
                }
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            switch (timeRemaining) {
                case 0 -> {
                    for (Participant p : MBC.getInstance().getPlayers()) p.getPlayer().getInventory().clear();
                    setGameState(GameState.END_ROUND);
                    createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Deciding game...");
                    timeRemaining = 20;
                }
                case 44 -> startVoting();
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            switch (timeRemaining) {
                case 9 -> {
                    raiseWalls(true);
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Counting votes...");
                    createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Chosen game revealed: ");
                    winner = countVotes();
                }
                case 6 -> {
                    if (tie) {
                        Bukkit.broadcastMessage("The vote is tied, so we're weighting the chicken votes based on team scores...");
                    } else {
                        Bukkit.broadcastMessage("The next game...");
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }
                case 4 -> {
                    if (tie) {
                        Bukkit.broadcastMessage("If that is still tied, the chosen game is random...");
                    } else {
                        Bukkit.broadcastMessage("with " + winner.votes.size() + " votes...");
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 2);
                }
                case 2 -> {
                    if (tie) {
                        if (winner.random)
                            Bukkit.broadcastMessage("The " + ChatColor.BOLD + "randomly chosen" + ChatColor.RESET + " game is...");
                        else
                            Bukkit.broadcastMessage("And with a weighted score of " + winner.tiebreakerScore + ", the chosen game is...");
                    } else {
                        Bukkit.broadcastMessage(ChatColor.BOLD + "is...");
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 3);
                }
                case 0 -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(winner.game, "", 0, 15, 15);
                        p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                    }
                    Bukkit.broadcastMessage(ChatColor.BOLD + winner.game + "!");
                    createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Warping to game: ");
                    if (!revealedGames) revealedGames = true;
                    setGameState(GameState.END_GAME);
                    timeRemaining = 13;
                }
            }

        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining > 5 && timeRemaining % 2 != 0) {
                for (Player p : Bukkit.getOnlinePlayers())
                    p.sendTitle(winner.game,"", 15, 15, 15);
            }
            switch (timeRemaining) {
                case 0 -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.stopSound(Sound.MUSIC_DISC_CAT, SoundCategory.RECORDS);
                    }
                    // start game
                    stopTimer();
                    MBC.getInstance().startGame(winner.game);
                }
                case 5 -> WarpEffects();
                case 6 -> {
                    removeEntities();
                    resetSections();
                }
            }
        }
    }

    @Override
    public void createScoreboard(Participant p) {
        newObjective(p);
        createLine(21, ChatColor.RED + "" + ChatColor.BOLD + "Voting begins in:", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(18, ChatColor.GREEN + "" + ChatColor.BOLD + "Your Team:", p);
        createLine(17, p.getTeam().getChatColor() + p.getTeam().getTeamFullName(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);
        updatePlayerTotalScoreDisplay(p);

        displayTeamTotalScore(p.getTeam());
    }

    /**
     * @see Lobby updatePlayerTotalScoreDisplay
     */
    public void updatePlayerTotalScoreDisplay(Participant p) {
        createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getRawTotalScore(), p);
    }

    /**
     * Gives all players voting eggs
     * Provides graphics
     */
    public void startVoting() {
        Bukkit.broadcastMessage(ChatColor.GREEN+"Time to vote!");
        createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Voting ends:");
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().addItem(new ItemStack(Material.EGG, 1));
            p.getPlayer().sendTitle(p.getTeam().getChatColor() + "" + ChatColor.BOLD + "Vote!", "", 20, 60, 20);
        }
    }

    /**
     * TODO
     */
    public void Powerups() {
        //Bukkit.broadcastMessage("[Debug] Powerups not implemented yet!");
    }

    /**
     * Randomly choose a game and assign it to a section.
     * @param section The section which will represent that game during voting.
     */
    public void revealGame(int section) {
        if (gameNames.size() < 1) {
            setGameState(GameState.ACTIVE);
            timeRemaining = 45;
            return;
        }

        String randomGame = gameNames.get((int)(Math.random()*gameNames.size()));
        loadSection(section, randomGame);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(randomGame, "", 20, 60, 20);
            p.playSound(p, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, (int) (Math.random() * 5));
        }
        gameNames.remove(randomGame);
    }

    public void loadSection(int section, String game) {
        for (Section s : sections.values()) {
            if (s.num != section) continue;
            s.setGame(game);
            for (Location l : s.sectionLocs) {
                l.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
            }
            s.setGameDisplay();
            return;
        }
    }

    public void removeSection(Section section) {
        Material key = null;
        for (Section s : sections.values()) {
            if (section.equals(s)) {
                for (Location l : s.sectionLocs) {
                    l.getBlock().setType(Material.RED_GLAZED_TERRACOTTA);
                    if (key == null) {
                        key = world.getBlockAt(new Location(world, l.getBlockX(), l.getBlockY()-1, l.getBlockZ())).getType();
                    }
                }
                s.removeDisplay();
                break;
            }
        }
        // avoid concurrent modification exception
        sections.remove(key, section);
    }

    /**
     * Counts votes
     */
    public Section countVotes() {
        // count votes
        int currentMax = 0;
        for (VoteChicken chicken : chickens) {
            Location chickenLoc = chicken.getChicken().getLocation();
            Location matLoc = new Location(world, chickenLoc.getBlockX(), chickenLoc.getBlockY()-2, chickenLoc.getBlockZ());
            Location secLoc = new Location(world, chickenLoc.getBlockX(), chickenLoc.getBlockY()-1, chickenLoc.getBlockZ());

            // if the chicken is not on a section or the section is red, ignore
            if (!matLoc.getBlock().getType().toString().endsWith("WOOL") || !secLoc.getBlock().getType().equals(Material.WHITE_GLAZED_TERRACOTTA)) continue;
            Section s = sections.get(matLoc.getBlock().getType());
            s.votes.add(chicken);
            currentMax = Math.max(currentMax, s.votes.size());
        }

        // do full loop to determine ties
        List<Section> mostVotes = new ArrayList<>(1);
        for (Section s : sections.values()) {
            if (s.votes.size() == currentMax && s.game != null) {
                mostVotes.add(s);
            }
        }

        if (mostVotes.size() == 1) {
            if (mostVotes.get(0) == null) Bukkit.broadcastMessage("[Debug] Null 1");
            return mostVotes.get(0);
        } else if (mostVotes.size() > 1) {
            tie = true;
            return Tiebreaker(mostVotes);
        } else {
            tie = true;
            Bukkit.broadcastMessage("mostVotes.size() == " + mostVotes.size());
            Bukkit.broadcastMessage("[Debug] I'm pretty sure this isn't supposed to happen...?");
            return TiebreakerRandom(mostVotes); // this will error but hopefully we don't get there
        }
    }

    /**
     * Tiebreaker procedure: weight chicken votes based on team; note, does not recount ALL chickens, only
     * those in tied sections
     * WEIGHTING:
     *      MBC.MAX_TEAMS   (last, likely 6th or 4th) -> 1.4
     *      MBC.MAX_TEAMS-1 (#notlast, 5th or 3rd) -> 1.3
     *      MBC.MAX_TEAMS-2 (2nd or 4th) -> 1.1 (possibly temporary)
     * If this STILL ties, then the tiebreaker is random.
     *
     * @param tiedSections List of sections that have the same chicken count
     * @return The chosen section
     */
    public Section Tiebreaker(List<Section> tiedSections) {
        // in case of a tie, weight the chickens according to team scores
        float maxScore = -1;
        for (Section s : tiedSections) {
            for (VoteChicken c : s.votes) {
                switch (c.team.getPlace()) {
                    case MBC.MAX_TEAMS -> // 6th
                            s.tiebreakerScore += 1.4;
                    case MBC.MAX_TEAMS - 1 -> // 5th
                            s.tiebreakerScore += 1.3;
                    case MBC.MAX_TEAMS - 2 -> // 4th
                            s.tiebreakerScore += 1.1;
                    default -> s.tiebreakerScore += 1;
                }
            }
            maxScore = Math.max(maxScore, s.tiebreakerScore);
        }

        List<Section> stillTied = new ArrayList<>();
        for (Section s : tiedSections) {
            if (s.tiebreakerScore == maxScore) {
               stillTied.add(s);
            }
        }

        if (stillTied.size() == 1) {
            return stillTied.get(0);
        } else {
            return TiebreakerRandom(stillTied);
        }
    }

    public Section TiebreakerRandom(List<Section> tiedSections) {
        int rand = (int)(Math.random()*tiedSections.size());
        Section win = tiedSections.get(rand);
        win.random = true;
        return win;
    }

    /**
     * @param b True = raise walls ; False = lower walls
     */
    public void raiseWalls(boolean b) {
        Material block = b ? Material.REDSTONE_BLOCK : Material.AIR;
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], -38, coord[1]).setType(block);
            if (!b) {
                world.getBlockAt(coord[0], -34, coord[1]).setType(Material.AIR);
                Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().plugin, new Runnable() {
                    @Override
                    public void run() {
                        world.getBlockAt(coord[0], -35, coord[1]).setType(Material.POLISHED_BLACKSTONE_SLAB);
                    }
                }, 5L);
            }
        }
    }

    public void setSectionsRed() {
        if (!sections.isEmpty()) {
            for (Section s : sections.values()) {
                for (Location l : s.sectionLocs) {
                    l.getBlock().setType(Material.RED_GLAZED_TERRACOTTA);
                }
            }
            return;
        }

        // if the sections have not been initialized
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                Material block = world.getBlockAt(x, -37, z).getType();
                if (block.toString().endsWith("WOOL")) {
                    world.getBlockAt(x, -36, z).setType(Material.RED_GLAZED_TERRACOTTA);

                    if (revealedGames) continue; // assuming we don't get more than 8 games

                    sections.get(block).addLoc(new Location(world, x, -36, z));
                }
            }
        }
    }

    @EventHandler
    public void eggThrow(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            Bukkit.broadcastMessage(e.getEntity().getShooter().toString());
            return; // remove if powerup spawns a chicken turret or something
        }

        if (e.getHitEntity() != null && e.getHitEntity().getType().equals(EntityType.ARMOR_STAND)) {
            e.setCancelled(true);
        }

        if (e.getEntity().getType().equals(EntityType.EGG)) {
            Egg egg = (Egg) e.getEntity();
            Participant p = Participant.getParticipant(((Player) e.getEntity().getShooter()));
            if (p == null) return;
            if (!(voters.add(p.getPlayer()))) {
                return;
            }
            Chicken chicken = (Chicken) egg.getLocation().getWorld().spawnEntity(egg.getLocation(), EntityType.CHICKEN);
            chickens.add(new VoteChicken(p.getTeam(), chicken));
            egg.remove();
        }
    }

    @EventHandler
    public void chickenHatch(PlayerEggThrowEvent e) {
        e.setHatching(false);
        e.setNumHatches((byte) 0);
    }

    public void removeEntities() {
        for (Entity e : world.getEntities()) {
            if (e.getType().equals(EntityType.CHICKEN)) {
                ((Damageable) e).setHealth(0);
            }
            if (e.getType().equals(EntityType.DROPPED_ITEM)) {
                e.remove();
            }
        }
    }

    public void WarpEffects() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 110, 1, false, false));
            p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
        }
    }

    private void initSections() {
        // fuck it
        Section s0 = new Section(0);
        Section s1 = new Section(1);
        Section s2 = new Section(2);
        Section s3 = new Section(3);
        Section s4 = new Section(4);
        Section s5 = new Section(5);
        Section s6 = new Section(6);
        Section s7 = new Section(7);

        sections.put(Material.WHITE_WOOL, s0);
        sections.put(Material.ORANGE_WOOL, s1);
        sections.put(Material.MAGENTA_WOOL, s2);
        sections.put(Material.LIGHT_BLUE_WOOL, s3);
        sections.put(Material.YELLOW_WOOL, s4);
        sections.put(Material.LIME_WOOL, s5);
        sections.put(Material.PINK_WOOL, s6);
        sections.put(Material.GREEN_WOOL, s7);

        initLocs();

        s0.setDisplayLocation(new Location(world, 2.5, -33.5, 4.5));
        s1.setDisplayLocation(new Location(world, -1.5, -33.5, 4.5));
        s2.setDisplayLocation(new Location(world, -3.5, -33.5, 2.5));
        s3.setDisplayLocation(new Location(world, -3.5, -33.5, -1.5));
        s4.setDisplayLocation(new Location(world, -1.5, -33.5, -3.5));
        s5.setDisplayLocation(new Location(world, 2.5, -33.5, -3.5));
        s6.setDisplayLocation((new Location(world, 4.5, -33.5, -1.5)));
        s7.setDisplayLocation(new Location(world, 4.5, -33.5, 2.5));
    }

    private void initLocs() {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                Material block = world.getBlockAt(x, -37, z).getType();
                if (block.toString().endsWith("WOOL")) {
                    // hopefully 128 manually created locations is ok
                    sections.get(block).addLoc(new Location(world, x, -36, z));
                }
            }
        }
    }

    private void resetSections() {
        List<Section> toRemove = new ArrayList<Section>(1);
        for (Section s : sections.values()) {
            if (s.game == null || s == winner) {
                toRemove.add(s);
            } else {
                s.reset();
            }
        }
        raiseWalls(false);

        //Bukkit.broadcastMessage("[Debug] before: sections.size() == " + sections.size());
        for (Section s : toRemove) {
            removeSection(s);
        }
        //Bukkit.broadcastMessage("[Debug] after: sections.size() == " + sections.size());
    }
}

/**
 * Representation for each votable area
 */
class Section {
    public final int num;
    // why did i track *every* location corresponding to each section, you ask?
    // what an interesting design choice, what's the idea?
    // well, well. well well well
    public final List<Location> sectionLocs = new ArrayList<>(16);
    public String game = null;
    public ArmorStand gameDisplay = null;
    private Location displayLocation;
    public List<VoteChicken> votes = new ArrayList<>();
    public float tiebreakerScore = 0;
    public boolean random = false;

    public Section(int num) {
        this.num = num;
    }

    public void setGame(String name) {
        this.game = name;
    }

    public void addLoc(Location l) {
        sectionLocs.add(l);
    }
    public void setDisplayLocation(Location l) { this.displayLocation = l; }
    public void removeDisplay() { if (gameDisplay != null) gameDisplay.remove(); }
    public void setGameDisplay() {
        if (gameDisplay == null) {
            gameDisplay = (ArmorStand) displayLocation.getWorld().spawnEntity(displayLocation, EntityType.ARMOR_STAND);
            gameDisplay.setGravity(false);
            gameDisplay.setInvulnerable(true);
            gameDisplay.setInvisible(true);
            gameDisplay.setCustomName(ChatColor.BOLD+game);
            gameDisplay.setCustomNameVisible(true);
        }
    }
    public void reset() {
        votes.clear();
        tiebreakerScore = 0;
        random = false;
    }
}

class VoteChicken {
    public MBCTeam team;
    public Chicken chicken;

    public VoteChicken(MBCTeam team, Chicken chicken) {
        this.team = team;
        this.chicken = chicken;
    }

    public Chicken getChicken() {
        return chicken;
    }
}