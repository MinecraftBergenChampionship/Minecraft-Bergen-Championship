package me.kotayka.mbc;

import me.kotayka.mbc.teams.Spectator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static java.util.Map.entry;

/**
 * DecisionDome is the representation of the Voting Phase of the tournament.
 * Chickens randomly walk into different quadrants, and the quadrant with the most chickens wins.
 *
 * Currently supports <= 8 games, a refactor will be needed for > 8 games.
 * Quadrants are mapped by the blocks that are hidden beneath them on the map.
 * DecisionDome also handles distributing powerups.
 *
 * Most quadrant/section code provided by @Jack-Crowley
 */
public class DecisionDome extends Minigame {
    private final World world = Bukkit.getWorld("DecisionDome");
    private boolean revealedGames;
    // Icon display code is kinda missy can probably be improved; a refactoring of game class may need to be made for cleanest icon support but not doing it yet @see Game
    public List<String> gameNames = new ArrayList<>(Arrays.asList("⑪ TGTTOS", "⑭ Ace Race", "⑬ Survival Games", "⑫ Skybattle", "⑯ Spleef", "⑮ Build Mart", "⑰ Party", "⑱ Power Tag"));
    private List<VoteChicken> chickens = new ArrayList<>(MBC.getInstance().getPlayers().size());
    private final Map<Material, Section> sections = new HashMap<>(8);

    private List<MBCTeam> powerupTeams = new ArrayList<>();
    private final Map<VotePowerup, Integer> weights = Map.ofEntries(
            entry(VotePowerup.DUNK, 4), entry(VotePowerup.MEGA_COW, 4), entry(VotePowerup.CROSSBOWS, 4),
            entry(VotePowerup.CHICKEN_SWAP, 3), entry(VotePowerup.HIDDEN, 3)
    );
    //removed eggstra votes for now bc it lame
    private Participant mega_cow_shooter = null;
    private Player dunker = null;
    private Player swapper = null;
    private Player hider = null;
    private ChatColor dunked_team = null;
    private boolean hidden = false;

    private Participant chooser = null;

    private final Location BOTTOM_CORNER = new Location(world, -17, -31, -16);
    private final Location TOP_CORNER = new Location(world, 18, -24, 16);

    private final int[][] coordsForBorder = {
            {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}, {0, 7}, {0, -1}, {0, -2}, {0, -3}, {0, -4}, {0, -5}, {0, -6}, {0, -7},
            {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {-1, 0}, {-2, 0}, {-3, 0}, {-4, 0}, {-5, 0}, {-6, 0}, {-7, 0},
            {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {1, -1}, {2, -2}, {3, -3}, {4, -4}, {5, -5}, {-1, 1}, {-2, 2}, {-3, 3}, {-4, 4},
            {-5, 5}, {-1, -1}, {-2, -2}, {-3, -3}, {-4, -4}, {-5, -5}
    };
    private int currentSection = (int)(Math.random()*gameNames.size());
    private Section winner;
    private boolean tie = false;
    private int doubleTime = (int)(Math.random()*20) + 10;
    private boolean doubled = true;

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
        doubled = true;
        winner = null;

        // Powerups
        if (!powerupTeams.isEmpty()) powerupTeams.clear();
        if (dunker != null) dunker = null;
        if (swapper != null) swapper = null;
        if (hider != null) hider = null;
        if (hidden == true) hidden = false;
        if (mega_cow_shooter != null) mega_cow_shooter = null;

        if (chooser != null) chooser = null;

        // Deal with players
        loadPlayers();
        createScoreboard();

        if (sections.size() == 1) {
            // start only game
            for (Section s : sections.values()) {
                winner = s;
            }
            MBC.getInstance().incrementMultiplier();
            createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Warping to game: ");
            setGameState(GameState.END_GAME);
            setTimer(13);
        } else {
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

    // decision dome with winner of sumo / other minigame. no other players recieve chickens and player stands in 
    public void start(Participant p) {
        chooser = p;

        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);

        // Initialize variables and map
        removeEntities();
        if (!revealedGames) {
            initSections();
        }
        chickens.clear();
        tie = false;
        doubled = false;
        winner = null;

        // Powerups
        if (!powerupTeams.isEmpty()) powerupTeams.clear();
        if (dunker != null) dunker = null;
        if (swapper != null) swapper = null;
        if (hider != null) hider = null;
        if (hidden == true) hidden = false;
        if (mega_cow_shooter != null) mega_cow_shooter = null;

        // Deal with players
        loadPlayers();
        createScoreboard();

        if (sections.size() == 1) {
            // start only game
            for (Section s : sections.values()) {
                winner = s;
            }
            MBC.getInstance().incrementMultiplier();
            createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Warping to game: ");
            setGameState(GameState.END_GAME);
            setTimer(13);
        } else {
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
        replaceAll();
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            Location l = getTeleportLocation(t.getChatColor());
            for (Participant p : t.getPlayers()) {
                p.getPlayer().teleport(l);
                p.getPlayer().setVelocity(new Vector(0, 0, 0));
                p.getPlayer().removePotionEffect(PotionEffectType.SPEED);
                p.getPlayer().getInventory().clear();
                if (!(t instanceof Spectator)) {
                    p.getPlayer().setGameMode(GameMode.ADVENTURE);
                } else {
                    p.getPlayer().setGameMode(GameMode.SPECTATOR);
                }
                p.getPlayer().playSound(p.getPlayer(), Sound.MUSIC_DISC_CAT,SoundCategory.RECORDS, 1, 1);
            }
        }

        if (chooser != null) chooser.getPlayer().teleport(new Location(world, 0, -34.5, 0));
    }

    private Location getTeleportLocation(ChatColor color) {
        Location l;
        switch (color) {
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
            default -> {
                l = new Location(world, 0, -5, 0);
            }
        }
        return l;
    }

        @Override
        public void events() {
            if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                timeRemaining = 40;
                // if (!revealedGames) revealedGames = true; the old object doesn't seem to persist but i can't exactly prove that
                setGameState(GameState.ACTIVE);
                /*
                this was for restarting but idr. also shouldn't go here
                if (MBC.getInstance().gam3ddeNum != 1) {
                    MBC.getInstance().incrementMultiplier();
                }
                 */
            }

            if (!revealedGames) {
                if (timeRemaining == 47) {
                    Bukkit.broadcastMessage(ChatColor.GREEN+""+ChatColor.BOLD+"Time to reveal the games!");
                    deleteOldNames();
                }

                if (timeRemaining % 5 == 0 && timeRemaining != 0) {
                    revealGame(currentSection % 8);
                    currentSection++;
                }
            } else {
                if (timeRemaining == 9) {
                    MBC.getInstance().incrementMultiplier();
                } else if (timeRemaining == 7) {
                    Powerups();
                }
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == doubleTime && chooser == null) {
                doubled = false;
                Bukkit.broadcastMessage(ChatColor.GREEN+"Double Chicken Time has ended!");
            }
            switch (timeRemaining) {
                case 0 -> {
                    for (Participant p : MBC.getInstance().getPlayers()) p.getPlayer().getInventory().clear();
                    setGameState(GameState.END_ROUND);
                    createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Deciding game...");
                    timeRemaining = 17;
                }
                case 39 -> startVoting();
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            switch (timeRemaining) {
                case 11 -> {
                    for (Participant p : MBC.getInstance().getPlayers()) p.getPlayer().getInventory().clear();
                    raiseWalls(true);
                }
                case 8 -> {
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
                        if(chooser != null) {
                            Bukkit.broadcastMessage("chosen by " + chooser.getFormattedName() + "...");
                        }
                        else if (winner.votes.size() == 1) {
                            Bukkit.broadcastMessage("with 1 vote...");
                        } else {
                            Bukkit.broadcastMessage("with " + winner.votes.size() + " votes...");
                        }
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
                        p.sendTitle(winner.game.substring(0,2) + ChatColor.BOLD + winner.game.substring(2), "", 0, 15, 15);
                        p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                    }
                    Bukkit.broadcastMessage(ChatColor.BOLD + winner.game + "!");
                    createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Warping to game: ");
                    setSectionGreen(winner);
                    if (!revealedGames) revealedGames = true;
                    setGameState(GameState.END_GAME);
                    HandlerList.unregisterAll(this);
                    timeRemaining = 13;
                }
            }

        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining > 5 && timeRemaining % 2 != 0) {
                for (Player p : Bukkit.getOnlinePlayers())
                    p.sendTitle(winner.game.substring(0,2) + ChatColor.BOLD + winner.game.substring(2),"", 15, 15, 15);
            }
            switch (timeRemaining) {
                case 0 -> {
                    // start game
                    stopTimer();
                    MBC.getInstance().startGame(winner.game.substring(2));
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
        createLine(19, " ", p);
        createLine(18, ChatColor.GREEN + "" + ChatColor.BOLD + "Your Team:", p);
        createLine(17, p.getTeam().getChatColor() + p.getTeam().teamNameFormat(), p);
        createLine(16, ChatColor.RESET.toString(), p);
        createLine(15, "Games Played: " + ChatColor.AQUA + (MBC.getInstance().gameNum-1) + "/6", p);
        createLine(14, String.format("Point Multiplier: %s%.1f", ChatColor.YELLOW, MBC.MULTIPLIERS[MBC.getInstance().gameNum-1]), p);

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
        if (chooser != null) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Because " + ChatColor.RESET + "" + chooser.getFormattedName() + ChatColor.GOLD + "" + ChatColor.BOLD +  " won, they get to select the next game!");
            createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Voting ends:");
            for (Participant p : MBC.getInstance().getPlayers()) {
                p.getPlayer().sendTitle(chooser.getFormattedName() + "" + ChatColor.BOLD + " is selecting...", "", 20, 60, 20);
            }
            chooser.getPlayer().sendMessage(ChatColor.BOLD + "Select a game by standing in the section.");
            return;
        } 
        Bukkit.broadcastMessage(ChatColor.GREEN+"Time to vote!");
        createLineAll(21, ChatColor.RED+""+ChatColor.BOLD+"Voting ends:");
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().addItem(new ItemStack(Material.EGG, 1));
            p.getPlayer().sendTitle(p.getTeam().getChatColor() + "" + ChatColor.BOLD + "Vote!", "", 20, 60, 20);
        }

        if (powerupTeams == null || powerupTeams.isEmpty()) return;

        // add powerups by weight amount, then add to set to ensure only one is chosen
        List<VotePowerup> powerups = new ArrayList<>();
        for (Map.Entry<VotePowerup, Integer> entry : weights.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                powerups.add(entry.getKey());
            }
        }

        Collections.shuffle(powerups);
        Set<VotePowerup> pool = new HashSet<>();
        for (int i = 0; i < powerups.size(); i++) {
            pool.add(powerups.remove(0));
        }

        for (MBCTeam t : powerupTeams) {
            // Randomly get powerup
            VotePowerup powerup = powerups.remove(0);
            // whole team powerups
            if (powerup == VotePowerup.CROSSBOWS || powerup == VotePowerup.EGGSTRA_VOTES) {
                for (Participant p : t.getPlayers()) {
                    // TODO: improve format of powerup string
                    p.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Your team got the powerup: " + powerup);
                    getPowerup(powerup, p);
                }
            } else {
                // single player powerups
                Participant p = t.getPlayers().get((int)(Math.random()*t.getPlayers().size()));
                for (Participant pl : t.getPlayers()) {
                    if (pl.getPlayer().getUniqueId().equals(p.getPlayer().getUniqueId())) {
                        p.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD+"You got the powerup: " + powerup.toString());
                        getPowerup(powerup, p);
                    } else {
                        p.getPlayer().sendMessage(p.getFormattedName() + ChatColor.LIGHT_PURPLE+" received the powerup: " + powerup);
                    }
                }
            }
            powerups.remove(powerup);
        }
    }

    private void getPowerup(VotePowerup pu, Participant p) {
        switch (pu) {
            case EGGSTRA_VOTES -> {
                p.getPlayer().getInventory().addItem(new ItemStack(Material.EGG, 1));
            }
            case CROSSBOWS -> {
                p.getPlayer().getInventory().addItem(new ItemStack(Material.CROSSBOW, 1));
                p.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 1));
            }
            case DUNK -> {
                dunker = p.getPlayer();
                p.getPlayer().getInventory().addItem(new ItemStack(Material.BOW, 1));
                p.getPlayer().getInventory().addItem(new ItemStack(Material.TIPPED_ARROW, 1));
            }
            case MEGA_COW -> {
                mega_cow_shooter = p;
                for (ItemStack i : p.getPlayer().getInventory()) {
                    if (i != null && i.getType().equals(Material.EGG)) {
                        i.addUnsafeEnchantment(Enchantment.FORTUNE, 5);
                    }
                }
            }
            case CHICKEN_SWAP -> {
                swapper = p.getPlayer();
                p.getPlayer().getInventory().addItem(new ItemStack(Material.BOW, 1));
                p.getPlayer().getInventory().addItem(new ItemStack(Material.TIPPED_ARROW, 1));
            }
            case HIDDEN -> {
                hider = p.getPlayer();
                p.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_EYE, 1));
            }
        }
    }

    /**
     * TODO: make this less uggers holy
     */
    public void Powerups() {
        if (chooser != null) return;
        // TODO: make this look better/more efficient (maybe assign when game scores are given in previous game)
        List<MBCTeam> teams = MBC.getInstance().getValidTeams();
        if (teams.size() <= 4) {
            // give last place a powerup
            for (MBCTeam t : teams) {
                if (t.getPlace() == teams.size()) {
                    powerupTeams.add(t);
                }
            }
        } else {
            List<MBCTeam> candidates = new ArrayList<>();
            for (MBCTeam t : MBC.getInstance().getValidTeams()) {
                if (t.getPlace() >= teams.size()-2) {
                    candidates.add(t);
                }
            }
            // randomly choose either team 6th, 5th, or 4th
            int r = (int)(Math.random()*candidates.size());
            powerupTeams.add(candidates.get(r));
            candidates.remove(r);
            int r2 = (int)(Math.random()*candidates.size());
            powerupTeams.add(candidates.get(r2));
        }

        for (MBCTeam t : powerupTeams) {
            Bukkit.broadcastMessage(t.teamNameFormat() + ChatColor.LIGHT_PURPLE + " were chosen to receive a powerup!");
            for (Participant p : t.getPlayers()) {
                p.getPlayer().spawnParticle(Particle.HAPPY_VILLAGER, p.getPlayer().getLocation().add(0.5, 0.75, 0.5), 3);
            }
        }
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

    public void removeSection(String gameName) {
        Material key = null;
        for (Section s : sections.values()) {
            if (s.game.equals(gameName)) {
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

        if (chooser != null) {
            Location chooserLoc = chooser.getPlayer().getLocation();
            Location matLoc = new Location(world, chooserLoc.getBlockX(), -37, chooserLoc.getBlockZ());
            Location secLoc = new Location(world, chooserLoc.getBlockX(), -36, chooserLoc.getBlockZ());
            if (!matLoc.getBlock().getType().toString().endsWith("WOOL") || !secLoc.getBlock().getType().equals(Material.WHITE_GLAZED_TERRACOTTA)) {
                List<Section> mostVotes = new ArrayList<>(1);
                for (Section s : sections.values()) {
                    mostVotes.add(s);
                }
                tie = true;
                return Tiebreaker(mostVotes);
            }
            else {
                return sections.get(matLoc.getBlock().getType());
            }
        }

        if (swapper != null) {
            chickens.add(new VoteChicken(Participant.getParticipant(swapper).getTeam(), swapper.getLocation()));
            chickens.add(new VoteChicken(Participant.getParticipant(swapper).getTeam(), swapper.getLocation()));
            chickens.add(new VoteChicken(Participant.getParticipant(swapper).getTeam(), swapper.getLocation()));
        }

        for (VoteChicken chicken : chickens) {
            Location chickenLoc = chicken.getLocation();
            Location matLoc = new Location(world, chickenLoc.getBlockX(), chickenLoc.getBlockY()-2, chickenLoc.getBlockZ());
            Location secLoc = new Location(world, chickenLoc.getBlockX(), chickenLoc.getBlockY()-1, chickenLoc.getBlockZ());
            if (chicken.chicken != null) {
                chicken.chicken.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            else if (chicken.cow != null) {
                chicken.cow.removePotionEffect(PotionEffectType.INVISIBILITY);
            }

            // if the chicken is not on a section or the section is red, kill and ignore
            if (!matLoc.getBlock().getType().toString().endsWith("WOOL") || !secLoc.getBlock().getType().equals(Material.WHITE_GLAZED_TERRACOTTA)) {
                if (chicken.chicken != null) {
                    chicken.chicken.setHealth(0);
                }
                else if (chicken.cow != null) {
                    chicken.cow.setHealth(0);
                }
                continue;
            }
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

    private void setSectionGreen(Section s) {
        for (Location l : s.sectionLocs) {
            l.getBlock().setType(Material.LIME_GLAZED_TERRACOTTA);
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
    public void onProjHit(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            //Bukkit.broadcastMessage(e.getEntity().getShooter().toString());
            e.setCancelled(true);
            return; // remove if powerup spawns a chicken turret or something
        }

        if (e.getHitEntity() != null && e.getHitEntity().getType().equals(EntityType.ARMOR_STAND)) {
            e.setCancelled(true);
            return;
        }

        if (e.getEntity().getType().equals(EntityType.EGG)) {
            Egg egg = (Egg) e.getEntity();
            egg.remove();
            Participant p = Participant.getParticipant(((Player) e.getEntity().getShooter()));
            if (p == null) return;

            if (mega_cow_shooter != null && p.getPlayer().getUniqueId().equals(mega_cow_shooter.getPlayer().getUniqueId())) {
                Bukkit.broadcastMessage(mega_cow_shooter.getFormattedName() + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " unleashed the mega cow!");
                Cow cow = (Cow) egg.getLocation().getWorld().spawnEntity(egg.getLocation(), EntityType.COW);
                cow.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
                if (hidden) {
                    cow.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 2, false, false));
                }
                VoteChicken mega_cow = new VoteChicken(p.getTeam(), cow);
                chickens.add(mega_cow);
                chickens.add(mega_cow);
                chickens.add(mega_cow);
                return;
            }

            Chicken chicken = (Chicken) egg.getLocation().getWorld().spawnEntity(egg.getLocation(), EntityType.CHICKEN);
            chicken.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
            if (hidden) {
                chicken.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 2, false, false));
            }
            chickens.add(new VoteChicken(p.getTeam(), chicken));
            if(doubled) {
                Chicken chickenTwo = (Chicken) egg.getLocation().getWorld().spawnEntity(egg.getLocation(), EntityType.CHICKEN);
                chickenTwo.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 2, false, false));
                if (hidden) {
                    chickenTwo.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 2, false, false));
                }
                chickens.add(new VoteChicken(p.getTeam(), chickenTwo));
            }
            
        } else if (e.getEntity().getType().equals(EntityType.ARROW)) {
            if (!(e.getEntity().getShooter() instanceof Player) || e.getEntity().getShooter() == null) return;
            if (e.getHitEntity() != null) {
                Participant shooter = Participant.getParticipant((Player) e.getEntity().getShooter());
                if (swapper != null && swapper.getUniqueId() == shooter.getPlayer().getUniqueId() && !(e.getHitEntity() instanceof Player)) {
                    e.setCancelled(true);
                    Location shooterLoc = shooter.getPlayer().getLocation();
                    Location chickenLoc = e.getHitEntity().getLocation();
                    e.getHitEntity().teleport(shooterLoc);
                    shooter.getPlayer().teleport(chickenLoc);
                    Bukkit.broadcastMessage(shooter.getFormattedName() + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " swapped places with a chicken!");
                } else {
                    // shootVotes also handles if a player is hit by dunk
                    shootVotes(e.getHitEntity(), (Player) e.getEntity().getShooter());
                }
                e.getEntity().remove();
                return;
            }

            if (e.getHitBlock() == null) {
                return;
            }
            if (e.getHitBlock().getLocation().getY() <= -32) return;
            if (e.getHitBlock().getLocation().getY() >= -23) return;
            String type = e.getHitBlock().getType().toString();
            Participant shooter = Participant.getParticipant((Player) e.getEntity().getShooter());
            if (dunker == null || !(shooter.getPlayer().getUniqueId().equals(dunker.getUniqueId()))) {
                return;
            }
            if (type.contains("RED_")) {
                Dunk(MBCTeam.getTeam("red"), shooter);
                dunked_team = ChatColor.RED;
            } else if (type.contains("YELLOW_")) {
                Dunk(MBCTeam.getTeam("yellow"), shooter);
                dunked_team = ChatColor.YELLOW;
            } else if (type.contains("GREEN_")) {
                Dunk(MBCTeam.getTeam("green"), shooter);
                dunked_team = ChatColor.GREEN;
            } else if (type.contains("BLUE_")) {
                Dunk(MBCTeam.getTeam("blue"), shooter);
                dunked_team = ChatColor.BLUE;
            } else if (type.contains("PURPLE_")) {
                Dunk(MBCTeam.getTeam("purple"), shooter);
                dunked_team = ChatColor.DARK_PURPLE;
            } else if (type.contains("PINK_")) {
                Dunk(MBCTeam.getTeam("pink"), shooter);
                dunked_team = ChatColor.LIGHT_PURPLE;
            }
            e.getEntity().remove();
        }
    }

    private void Dunk(MBCTeam team, Participant damager) {
        if (team == null) {
            Bukkit.broadcastMessage("Dunked was null!");
            Bukkit.broadcastMessage("This should hopefully never happen in an actual event.");
            return;
        }

        Bukkit.broadcastMessage(team.teamNameFormat() + ChatColor.RED + " were dunked by " + damager.getFormattedName() + ChatColor.RED +  "!");
        Location l1, l2;
        switch (team.getChatColor()) {
            case RED -> {
                l1 = new Location(world, 10, -23, 16);
                l2 = new Location(world, 4, -30, 10);
            }
            case YELLOW -> {
                l1 = new Location(world, -4, -23, 16);
                l2 = new Location(world, -10, -30, 10);
            }
            case GREEN -> {
                l1 = new Location(world, -11, -23, 3);
                l2 = new Location(world, -17, -30, -3);
            }
            case BLUE -> {
                l1 = new Location(world, -4, -23, -10);
                l2 = new Location(world, -10, -30, -16);
            }
            case DARK_PURPLE -> {
                l1 = new Location(world, 10, -23, -10);
                l2 = new Location(world, 4, -30, -16);
            }
            default -> {
                l1 = new Location(world, 17, -23, 3);
                l2 = new Location(world, 11, -30, -3);
            }
        }
        removeTube(l1, l2);

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().sendTitle(" ", team.teamNameFormat() + ChatColor.RESET + " were dunked!", 0, 60, 30);
            p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 1);
            if (p.getTeam().equals(team)) {
                p.getPlayer().teleport(getTeleportLocation(p.getTeam().getChatColor()));
                p.getPlayer().setVelocity(new Vector(0, 0, 0));
            }
        }
    }

    private void removeTube(Location topCorner, Location bottomCorner) {
        int pasteToX = 68;
        int pasteToY = -24;
        int pasteToZ = -3;
        for (int x = bottomCorner.getBlockX(); x <= topCorner.getBlockX(); x++) {
            for (int y = bottomCorner.getBlockY(); y <= topCorner.getBlockY(); y++) {
                for (int z = bottomCorner.getBlockZ(); z <= topCorner.getBlockZ(); z++) {
                    Block copyFrom = world.getBlockAt(x,y,z);
                    Block pasteTo = world.getBlockAt(pasteToX, pasteToY, pasteToZ);
                    pasteTo.setType(copyFrom.getType());
                    world.getBlockAt(x,y,z).breakNaturally(new ItemStack(Material.WOODEN_SHOVEL));
                    pasteToZ++;
                }
                pasteToY++;
                pasteToZ = -3;
            }
            pasteToX++;
            pasteToY = -24;
        }
    }


    private void replaceAll() {
        int copyFromX = 114;
        int copyFromY = -30;
        int copyFromZ = -16;
        for (int x = BOTTOM_CORNER.getBlockX(); x <= TOP_CORNER.getBlockX(); x++) {
            for (int y = BOTTOM_CORNER.getBlockY(); y <= TOP_CORNER.getBlockY(); y++) {
                for (int z = BOTTOM_CORNER.getBlockZ(); z <= TOP_CORNER.getBlockZ(); z++) {
                    Block copyFrom = world.getBlockAt(copyFromX, copyFromY, copyFromZ);
                    Block pasteTo = world.getBlockAt(x,y,z);
                    if (copyFrom.getType() != pasteTo.getType()) {
                        pasteTo.setType(copyFrom.getType());
                        pasteTo.setBlockData(copyFrom.getBlockData());
                    }
                    copyFromZ++;
                }
                copyFromY++;
                copyFromZ = -16;
            }
            copyFromX++;
            copyFromY = -30;
        }

    }

    @EventHandler
    public void chickenHatch(PlayerEggThrowEvent e) {
        e.setHatching(false);
        e.setNumHatches((byte) 0);
    }

    public void shootVotes(Entity e, Player damager) {
        if (e instanceof Chicken) {
            // Crossbows Powerup used on Chickens
            Chicken c = (Chicken) e;
            c.setHealth(0);
            Participant shooter = Participant.getParticipant(damager);
            Bukkit.broadcastMessage(shooter.getFormattedName() + ChatColor.RED + " shot a chicken with a crossbow!");
            VoteChicken rm = null;
            for (VoteChicken vc : chickens) {
                if (vc.chicken != null && vc.chicken.equals(c)) {
                    rm = vc;
                    break;
                }
            }
            chickens.remove(rm);
        } else if (e instanceof Cow) {
            // Crossbows Powerup used on Mega Cow
            Cow c = (Cow) e;
            c.setHealth(0);
            Participant shooter = Participant.getParticipant(damager);
            Bukkit.broadcastMessage(shooter.getFormattedName() + ChatColor.RED + " shot a Mega Cow with a crossbow!");
            VoteChicken rm = null;
            for (VoteChicken vc : chickens) {
                if (vc.cow != null && vc.cow.equals(c)) {
                    rm = vc;
                    break;
                }
            }
            chickens.remove(rm);
        } else if (e instanceof Player) {
            // Check if the player that shot had the powerup
            if (dunker == null || !(damager.getUniqueId().equals(dunker.getUniqueId()))) {
                return;
            }

            Participant hit = Participant.getParticipant((Player) e);
            Dunk(hit.getTeam(), Participant.getParticipant(damager));
        }
    }

    public void removeEntities() {
        for (Entity e : world.getEntities()) {
            if (e.getType().equals(EntityType.CHICKEN) || e.getType().equals(EntityType.COW)) {
                ((Damageable) e).setHealth(0);
            }
            if (e.getType().equals(EntityType.ITEM)) {
                e.remove();
            }
        }
    }

    public void WarpEffects() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_PORTAL_TRAVEL, SoundCategory.RECORDS, 1, 1);
            if (p.getTeam().getChatColor().equals(dunked_team)) continue;
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 110, 1, false, false));
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

    public void deleteOldNames() {
        for (ArmorStand a : world.getEntitiesByClass(ArmorStand.class)) {
            a.remove();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.ENDER_EYE && !hidden && e.getPlayer().equals(hider)) {
                hidden = true;
                Bukkit.broadcastMessage(Participant.getParticipant(hider).getFormattedName() + ChatColor.BOLD + " has obscured the arena - chickens now used will be hidden from view!");
                e.getPlayer().getInventory().remove(Material.ENDER_EYE);
                e.setCancelled(true);

            }
            else if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.ENDER_EYE) {
                e.getPlayer().getInventory().remove(Material.ENDER_EYE);
                e.setCancelled(true);
            }
        }
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
            gameDisplay.setCustomName(game.substring(0, 2) + ChatColor.BOLD + game.substring(2));
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
    public Cow cow;
    public Location loc;

    public VoteChicken(MBCTeam team, Chicken chicken) {
        this.team = team;
        this.chicken = chicken;
        this.cow = null;
    }

    public VoteChicken(MBCTeam team, Cow cow) {
        this.team = team;
        this.cow = cow;
        this.chicken = null;
    }

    public VoteChicken(MBCTeam team, Location loc) {
        this.cow = null;
        this.chicken = null;
        this.team = team;
        this.loc = loc;
    }

    public Location getLocation() {
        if (this.loc != null) {
            return loc;
        } else if (chicken != null){
            return chicken.getLocation();
        } else {
            return cow.getLocation();
        }
    }
}