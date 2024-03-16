package me.kotayka.mbc;

import me.kotayka.mbc.NPCs.NPCManager;
import me.kotayka.mbc.comparators.TeamScoreSorter;
import me.kotayka.mbc.comparators.TotalIndividualComparator;
import me.kotayka.mbc.games.*;
import me.kotayka.mbc.teams.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class MBC implements Listener {
    // singleton for event
    public static MBC mbc = null;
    private boolean enable_stat_logging = false;

    // event specifics
    public static final int MAX_PLAYERS_PER_TEAM = 4;
    public static final int MAX_TEAMS = 6;
    public static final int MAX_PLAYERS = MAX_PLAYERS_PER_TEAM * MAX_TEAMS;
    public static final int GAME_COUNT = 6;

    /**
     * `players` represents all current active players.
     * Players are added to the list when they join a team, if that team is not Spectator.
     * Players are removed from the list when they log off.
     */
    public List<Participant> players = new ArrayList<>(16); // every player
    public List<Participant> participants = new ArrayList<>(16); // every player + spectators
    //public TreeSet<Participant> individual = new TreeSet<>(Participant.rawTotalScoreComparator);
    //public TreeSet<MBCTeam> teamScores = new TreeSet<>(new TeamScoreSorter());

    public Red red = new Red();
    public Yellow yellow = new Yellow();
    public Green green = new Green();
    public Blue blue = new Blue();
    public Purple purple = new Purple();
    public Pink pink = new Pink();
    public Spectator spectator = new Spectator();

    public List<MBCTeam> teams = new ArrayList<>(Arrays.asList(red, yellow, green, blue, purple, pink, spectator));
    public Set<MBCTeam> ready = new HashSet<>();
    //public List<MBCTeam> validTeams = new ArrayList<>();
    //public List<String> teamNamesFull = new ArrayList<>(Arrays.asList("Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets", "Spectator"));
    public static List<String> teamNames = new ArrayList<>(Arrays.asList("RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets", "Spectator"));
    public ScoreboardManager manager =  Bukkit.getScoreboardManager();
    public final Scoreboard board = manager.getNewScoreboard();

    private Minigame currentGame;
    public int gameNum = 1; // REVERT THIS AFTER TEST

    public Plugin plugin;
    public final Lobby lobby = new Lobby();
    public DecisionDome decisionDome = null;
    public AceRace aceRace = null;
    public TGTTOS tgttos = null;
    public BuildMart bsabm = null;
    public Skybattle skybattle = null;
    public SurvivalGames sg = null;
    public Spleef spleef = null;
    //public Dodgebolt dodgebolt = null;
    public Quickfire quickfire = null;
    public boolean finalGame = false;
    public boolean readyCheck = false;

    //public static final List<String> gameNameList = new ArrayList<>(Arrays.asList("DecisionDome","AceRace","TGTTOS","BuildMart","Skybattle", "SurvivalGames", "Spleef","Dodgebolt","Quickfire"));
    public static final List<String> gameNameList = new ArrayList<>(Arrays.asList("DecisionDome","AceRace","TGTTOS","BuildMart","Skybattle", "SurvivalGames", "Spleef","Quickfire"));
    //public final List<Game> gameList = new ArrayList<Game>(6);
    public static final String MBC_STRING_PREFIX = ChatColor.BOLD + "[" + ChatColor.GOLD + "" + ChatColor.BOLD + "MBC" + ChatColor.WHITE + "" + ChatColor.BOLD + "]: ";
    private List<String> mutedMessages = new LinkedList<String>();

    // Define Special Blocks
    // NOTE: ALWAYS USE `getBlock().getRelative(BlockFace.DOWN)` or equivalent
    public static final Material SPEED_PAD = Material.OBSERVER;
    public static final Material BOOST_PAD = Material.WAXED_EXPOSED_CUT_COPPER;
    public static final Material MEGA_BOOST_PAD = Material.WAXED_WEATHERED_CUT_COPPER;
    public static final Material JUMP_PAD = Material.WAXED_WEATHERED_COPPER;
    public double multiplier = 1;

    public static NPCManager npcManager;

    private MBC(Plugin plugin) {
        this.plugin = plugin;
        currentGame = lobby;
        npcManager = new NPCManager((JavaPlugin) plugin);
    }

    // ensure singular instance to remove static overuse
    public static MBC getInstance(Plugin plugin) {
        if (mbc == null) {
            mbc = new MBC(plugin);
        }
        return mbc;
    }

    // ensure singular instance to remove static overuse
    public static MBC getInstance() {
        return Objects.requireNonNull(mbc);
    }

    public Minigame gameInstance(int gameNum) {
        switch(gameNameList.get(gameNum)) {
            case "DecisionDome":
                if (decisionDome == null) {
                    //Bukkit.broadcastMessage("Making new decisiondome!");
                    decisionDome = new DecisionDome(gameNum > 1); // NOTE: if we get > 8 games available per event, this must change.
                    // NOTE NOTE: this is kind of redundant now
                }
                return decisionDome;
            case "AceRace":
                if (aceRace == null) {
                    aceRace = new AceRace();
                }
                return aceRace;
            case "TGTTOS":
                if (tgttos == null) {
                    tgttos = new TGTTOS();
                }
                return tgttos;
            case "BuildMart":
                if (bsabm == null) {
                    bsabm = new BuildMart();
                }
                return bsabm;
            case "Skybattle":
                if (skybattle == null) {
                    skybattle = new Skybattle();
                }
                return skybattle;
            case "SurvivalGames":
                if (sg == null) {
                    sg = new SurvivalGames();
                }
                return sg;
            case "Spleef":
                if (spleef == null) {
                    spleef = new Spleef();
                }
                return spleef;
                /*
            case "Dodgebolt":
                if (dodgebolt == null) {
                    if (getValidTeams().size() < 2) {
                        dodgebolt = new Dodgebolt();
                    } else {
                        // TODO: this is a sin against man
                        List<MBCTeam> temp = getValidTeams();
                        temp.sort(new TeamScoreSorter());
                        dodgebolt = new Dodgebolt(temp.get(temp.size()-1), temp.get(temp.size()-2));
                    }
                }
                return dodgebolt;
                 */
            case "Quickfire":
                if (quickfire == null) {
                    if (getValidTeams().size() < 2 ) {
                        quickfire = new Quickfire();
                    } else {
                        // TODO: this is a sin against man
                        List<MBCTeam> temp = getValidTeams();
                        temp.sort(new TeamScoreSorter());
                        quickfire = new Quickfire(temp.get(temp.size()-1), temp.get(temp.size()-2));
                    }
                }
                return quickfire;
            default:
                return lobby;
        }
    }

    // todo: this is kinda lazy and using enums would probably be way better if possible down the line
    public Minigame gameInstance(String gameName) {
        return gameInstance(gameNameList.indexOf(gameName));
    }

    public Game getGame(String name) {
        return game(gameNameList.indexOf(name));
    }

    private Game game(int num) {
        switch (gameNameList.get(num)) {
            case "AceRace":
                return aceRace;
            case "TGTTOS":
                return tgttos;
            case "SurvivalGames":
                return sg;
            case "Skybattle":
                return skybattle;
            case "BuildMart":
                return bsabm;
            case "Spleef":
                return spleef;
            default:
                return null;
        }
    }

    public static String getScoring(String str) {
        switch(str) {
            case "AceRace":
                return ChatColor.BOLD + "Scoring:\n" + ChatColor.RESET +
                    "- +1 point for completing a lap\n" +
                    "- +1 point for every player beaten on a lap\n" +
                    "- +8 points for finishing the course\n" +
                    "- +4 points for every player beaten on the final lap\n" +
                    "- Top 8 Bonuses- 1st:+25, 2nd,3rd:+15, 4th,5th:+10, 6th-8th:+5";
            case "BuildMart":
                return ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                    "- +3 points **per player** for completing a build\n" +
                    "- +3 points **per player** for each team outplaced\n" +
                    "- Max +3 points **per player** for each build partially completed at game end";
            case "Skybattle":
                return ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                    "- +15 points for eliminations\n" +
                    "- +15 points for winning the round\n" +
                    "- +1 point for outliving another player";
            case "Spleef":
                return ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "- +2 points for outliving another player\n" +
                        "- +2 points for spleefing another player\n" +
                        "- +4 points for every player on the last fully alive team\n" +
                        "- Placement Bonuses - 1st: +20, 2nd: +15, 3rd: +10, 4th,5th: +8, 6th,7th: +5, 8th,9th: +3";
            case "SurvivalGames":
                return ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "- +10 points for eliminations\n" +
                        "- +2 points for every player outlived\n" +
                        "- Team Bonuses (split amongst team):\n" +
                        "     - 1st: +10 points, 2nd:+8 points, 3rd: +7 points, 4th: +6 points, 5th: +5 points, 6th: +4 points";
            case "TGTTOS":
                return ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                                "- +1 point for completing the course\n" +
                                "- +1 point for every player outplaced\n" +
                                "- +4 points for each player on the first full team to finish a course\n" +
                                "- +2 points for each player on the second full team to finish a course\n" +
                                "- +5 bonus points for placing Top 3 in a course\n";
            default:
                return ChatColor.RED + "Invalid game!\n" + ChatColor.RESET;
        }
    }

    /**
     * Access to current event activity
     * @return Minigame currentGame
     */
    public Minigame getMinigame() {
        return currentGame;
    }

    /**
     * For use exclusively if there is no minigame active
     * @return Game currentGame
     */
    public Game getGame() {
        if (!(currentGame instanceof Game)) {
            System.err.print("Tried to access Game when no game was active!");
            return null;
        }
        return (Game) currentGame;
    }

    public void incrementMultiplier() {
        /*
         * Game 1: 1x
         * Game 2: 1.5x
         * Game 3, 4: 2x
         * Game 5: 2.5x
         * Game 6: 3x
         */
        if (gameNum != 4) {
            Bukkit.broadcastMessage(ChatColor.BOLD + "The point multiplier has increased from " + multiplier + " to " + (multiplier+0.5) + "!");
            multiplier+=0.5;
        }
    }

    public void setCurrentGame(Minigame game) {
       currentGame = game;
    }

    private MBC() {}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Participant.contains(event.getPlayer())) { // new login this instance
            Participant newPlayer = new Participant(event.getPlayer());
            players.add(newPlayer);

            if (newPlayer.objective == null || !Objects.equals(newPlayer.gameObjective, currentGame.gameName)) {
                currentGame.createScoreboard(newPlayer);
                newPlayer.gameObjective = currentGame.gameName;
            }
        } else { // relog
            Participant p = Participant.getParticipant(event.getPlayer());
            p.setPlayer(event.getPlayer());
            Scoreboard newBoard = manager.getNewScoreboard();
            p.board = newBoard;
            p.getPlayer().setScoreboard(newBoard);
            p.changeTeam(p.getTeam());
            p.setupScoreboardTeams();
            p.gameObjective = currentGame.gameName;
            currentGame.newObjective(p);
            currentGame.createScoreboard(p);
            if (currentGame instanceof Game) {
                currentGame.createLineAll(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, currentGame.gameName));
                currentGame.createLineAll(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET));
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());

        // no need to pause for non-full games
        if (currentGame instanceof Game) {
            switch (currentGame.getState()) {
                case TUTORIAL:
                case END_ROUND:
                    ((Game) currentGame).disconnect = true;
                    Bukkit.broadcastMessage("[Debug] disconnect during transition state!");
                    break;
                case STARTING:
                    currentGame.Pause();
                    break;
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equalsIgnoreCase("Flying is not enabled on this server"))
            e.setCancelled(true);
    }

    /**
     * @return List of all non-spectator teams with at least one player
     */
    public List<MBCTeam> getValidTeams() {
        List<MBCTeam> newTeams = new ArrayList<>();
        for (MBCTeam t : teams) {
            if (!(t instanceof Spectator) && t.teamPlayers.size() > 0) {
                newTeams.add(t);
            }
        }
        return newTeams;
    }

    /**
     * Handles formatting player messages
     * @param e PlayerChatEvent
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent e) {
        String msg = e.getMessage();
        for (Participant p : players) {
            if (e.getPlayer().getUniqueId().equals(p.getPlayer().getUniqueId())) {
                msg = msg.replace("%", "%%");
                msg = p.getFormattedName() + ": " + msg;
                e.setFormat(msg);
                break;
            }
        }
        if (currentGame != null && (currentGame != lobby && currentGame.getState().equals(GameState.TUTORIAL)) ||
                (currentGame == lobby && lobby.getState().equals(GameState.END_ROUND) && lobby.timeRemaining <= 80 && lobby.timeRemaining > 69)) {
            mutedMessages.add(msg);
            e.getPlayer().sendMessage(ChatColor.RED + "Chat is currently muted, your message will send after!");
            e.setCancelled(true);
        }
    }

    public void sendMutedMessages() {
        for (String s : mutedMessages) {
            Bukkit.broadcastMessage(s);
        }
        mutedMessages.clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            Player p = e.getPlayer();
            Location l = p.getLocation();
            l.setPitch(-30);
            Vector d = l.getDirection();
            p.setVelocity(d.multiply(4));
            p.setVelocity(new Vector(p.getVelocity().getX(), 1.65, p.getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.BOOST_PAD) {
            Player p = e.getPlayer();
            Location l = p.getLocation();
            l.setPitch(-30);
            Vector d = l.getDirection();
            p.setVelocity(d.multiply(2.5));
            p.setVelocity(new Vector(p.getVelocity().getX(), 1.25, p.getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.JUMP_PAD) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.SPEED_PAD) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
        }
    }

    // Prevent breaking paintings
    @EventHandler
    public void hangingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player && ((Player) e.getRemover()).getGameMode() == GameMode.CREATIVE) return;

        if (e.getEntity() instanceof Painting) {
            e.setCancelled(true);
        }
    }

    /**
     * No Drowning, I guess?
     */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevent players from taking damage from fireworks
     * Until this ever becomes a feature in a game
     */
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        // players or chickens should never get damaged by fireworks
        if (e.getDamager() instanceof Firework) {
            e.setCancelled(true);
        }

        if (!(e.getEntity() instanceof Player)) return;
        if (!(currentGame instanceof Game)) { e.setCancelled(true); return; }
        if (getGame().PVP()) {
            // I'm not sure if Team Attack was on before, but just in case
            if (!(e.getDamager() instanceof Player)) return;
            if (!(e.getEntity() instanceof Player)) return;

            Player p1 = (Player) e.getDamager();
            Player p2 = (Player) e.getEntity();
            if (Participant.getParticipant(p1).getTeam().equals(Participant.getParticipant(p2).getTeam())) {
                e.setCancelled(true);
            }
            return;
        }
        if (!(getGame().PVP()) && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevent Players from taking damage from player fired projectiles if PVP is off
     */
    @EventHandler
    public void onProjHit(ProjectileHitEvent e) {
        // I don't even know if this is necessary
        if (e.getHitEntity() instanceof Painting || e.getHitEntity() instanceof ItemFrame) {
            e.setCancelled(true);
            return;
        }

        if (!(currentGame instanceof Game)) return;
        if (getGame().PVP()) return;
        if (e.getEntity().getShooter() instanceof Player && e.getHitEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    public void startGame(Minigame game) {
        if (currentGame instanceof Game) {
            Bukkit.broadcastMessage(ChatColor.RED+"ERROR: " + currentGame.gameName + " is in progress!");
            return;
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + game.gameName + ChatColor.WHITE + " has started");
        currentGame = game;
        currentGame.start();
    }

    public void startGame(int game) {
        // prevent another game from starting if a non-lobby game is active
        if (currentGame instanceof Game) {
            Bukkit.broadcastMessage(ChatColor.RED+"ERROR: " + currentGame.gameName + " is in progress!");
            return;
        }

        startGame(gameInstance(game));
    }

    public void startGame(String gameName) {
        gameName = gameName.replaceAll("\\s", "");
        startGame(gameInstance(gameName));
    }

    public void cancelEvent(int taskID) {
        // for some reason this was not cancelling the event so i commented it out for now, probably worth another look in the future
        //if (Bukkit.getScheduler().isCurrentlyRunning(taskID)) {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    /**
     * Spawn firework on a given player
     * @param p Participant on which the firework should spawn
     */
    public static void spawnFirework(Participant p) {
        Location l = p.getPlayer().getLocation();
        l.setY(l.getY()+1);
        org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.addEffect(FireworkEffect.builder().withColor(p.getTeam().getColor()).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    /**
     * Spawn Firework at a given location with a given color
     * @param l Location to spawn the firework
     * @param c Color for the firework to have
     */
    public static void spawnFirework(Location l, Color c) {
        org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder().withColor(c).build());
        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    /**
     * ArrayList of all Participants not on Team Spectator.
     * @return Copy of list of current players without spectators.
     */
    public List<Participant> getPlayers() {
        List<Participant> newList = new ArrayList<>();
        for (Participant p : getInstance().players) {
            if (!Objects.equals(p.getTeam().fullName, "Spectator")) {
                newList.add(p);
            }
        }

        return newList;
    }

    public void updatePlacings() {
        int i = 1;
        //int lastScore = -1;

        List<Participant> individual = getPlayers();
        individual.sort(new TotalIndividualComparator());

        // TODO: using an auto sort ds maybe a set would be better
        for (Participant p : individual) {
            //if (p.getRawTotalScore() == lastScore) {
                //p.setPlacement(i);
            //} else {
            p.setPlacement(i++);
                //lastScore = p.getRawTotalScore();
            //}
        }
    }

    public void getPlacementInfo(Player sender) {
        Participant p = Participant.getParticipant(sender);

        if (p == null) return;

        if (p.getTeam() instanceof Spectator) {
            sender.sendMessage(ChatColor.GREEN+"[placement] " + ChatColor.RESET+"You are Spectating, and do not have a score!");
            return;
        }

        if (currentGame instanceof Game) {
            sender.sendMessage(ChatColor.GREEN+"[placement] " + ChatColor.RESET+"Your scores will update after the game has finished!");
            return;
        }

        int placement = p.getPlacement();
        if (placement < 0) {
            sender.sendMessage(ChatColor.GREEN+"[placement] " + ChatColor.RESET+"The event has not started yet!");
            return;
        }

        StringBuilder msg = new StringBuilder(ChatColor.AQUA+"+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=\n"+ChatColor.RESET);
        // TODO: make ties not look stupid
        msg.append(ChatColor.YELLOW+"Your placement: " + Game.getColorStringFromPlacement(placement) + Game.getPlace(placement) + ChatColor.RESET + "\n");
        msg.append(ChatColor.YELLOW+"Your score: " + ChatColor.RESET+ p.getRawTotalScore()+"\n");
        if (placement != 1) {
            List<Participant> aheadPlayers = Participant.getParticipant(placement-1);
            msg.append("The player one place above you has " + (aheadPlayers.get(0).getRawTotalScore() - p.getRawTotalScore()) + " more coins.\n");
        }
        msg.append(ChatColor.AQUA+"+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=");
        sender.sendMessage(msg.toString());
    }

    public void getTopIndividual(Player sender) {
        List<Participant> individual = getPlayers();
        individual.sort(new TotalIndividualComparator());
        int placement;
        StringBuilder msg = new StringBuilder(ChatColor.AQUA.toString()+ChatColor.BOLD+"Player scores: \n"+ChatColor.RESET);
        for (Participant p : individual) {
            placement = p.getPlacement();
            if (placement < 9) {
                msg.append(placement).append(". ").append(p.getFormattedName()).append(": ").append(p.getRawTotalScore()).append("\n");
            } else {
                break;
            }
        }
        sender.sendMessage(msg.toString());
    }

    public void getTopIndividualAndPlacement(Player sender) {
        List<Participant> individual = getPlayers();
        individual.sort(new TotalIndividualComparator());
        int placement;
        StringBuilder msg = new StringBuilder(ChatColor.AQUA.toString()+ChatColor.BOLD+"Player scores: \n"+ChatColor.RESET);
        StringBuilder yourPlace = new StringBuilder(ChatColor.AQUA+"+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=\n"+ChatColor.RESET);
        for (Participant p : individual) {
            placement = p.getPlacement();
            if (placement < 9) {
                msg.append(placement).append(". ").append(p.getFormattedName()).append(": ").append(p.getRawTotalScore()).append("\n");
            }
            if (p.getPlayer().equals(sender)) {
                yourPlace.append(ChatColor.YELLOW+"Your placement: " + Game.getColorStringFromPlacement(placement) + Game.getPlace(placement) + ChatColor.RESET + "\n");
                yourPlace.append(ChatColor.YELLOW+"Your score: " + ChatColor.RESET+ p.getRawTotalScore()+"\n");
                // TODO: make ties not look stupid
                if (placement != 1) {
                    List<Participant> aheadPlayers = Participant.getParticipant(placement-1);
                    yourPlace.append("The player one place above you has " + (aheadPlayers.get(0).getRawTotalScore() - p.getRawTotalScore()) + " more coins.\n");
                }
                yourPlace.append(ChatColor.AQUA+"+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=");
            }
        }
        sender.sendMessage(msg + "\n" + yourPlace);
    }

    public static void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void setGameNum(int num) {
        if (finalGame && num != 6) {
            finalGame = false;
        } else if (num == 6) {
            finalGame = true;
        }
        gameNum = num;
    }

    /**
     * Create an announcement with string s.
     * @param s String to be broadcasted; should not end with a newline.
     */
    public void announce(String s) {
        StringBuilder str = new StringBuilder();
        str.append(ChatColor.GREEN + "\n+=+=+=+=+=+=+=+=+=+=" + MBC.MBC_STRING_PREFIX + ChatColor.GREEN + "+=+=+=+=+=+=+=+=+=+\n");
        str.append("\n" + ChatColor.RESET + s);
        str.append(ChatColor.GREEN + "\n\n+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+\n");
        Bukkit.broadcastMessage(str.toString());
    }

    public void ready(MBCTeam t, Player p) {
        if (ready.add(t)) {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.GREEN + " Successfully readied up!");
        } else {
            p.sendMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + " Your team was already ready!");
        }


        if (ready.size() == getValidTeams().size()) {
            if (getValidTeams().size() == 0) {
                // awwwwwww
                announce("There are no teams! You may want to assign teams first!");
            } else {
                // WOOOOOOO
                startEvent();
            }
        }
    }

    private void startEvent() {
        lobby.setGameState(GameState.TUTORIAL);
        announce(ChatColor.BOLD + "The event is starting!" + ChatColor.RESET + "\nYou may want to turn JUKEBOX sounds down.");
        lobby.setTimer(65);
    }

    public void setLogStats(boolean b) { enable_stat_logging = b; }
    public boolean logStats() { return enable_stat_logging; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    public static String statDirectory() { return "stat_archive"+File.separator+"MBC2";}
    /**
     * ArrayList of all Participants including Spectators.
     * @return Copy of list of current players with spectators.
     */
    public List<Participant> getPlayersAndSpectators() {
        return new ArrayList<>(getInstance().participants);
    }
}
