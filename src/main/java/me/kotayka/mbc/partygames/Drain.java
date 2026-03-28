package me.kotayka.mbc.partygames;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.dragonsMap.Arrgh;
import me.kotayka.mbc.gameMaps.dragonsMap.ConchStreet;
import me.kotayka.mbc.gameMaps.dragonsMap.DragonsMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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

    private final Location[] SPAWN = { new Location(Bukkit.getWorld("Party"), 2013, 102, 1978, 30, 0),
            new Location(Bukkit.getWorld("Party"), 2025, 102, 2000, 90, 0),
            new Location(Bukkit.getWorld("Party"), 2013, 102, 2022, 150, 0),
            new Location(Bukkit.getWorld("Party"), 1987, 102, 2022, -150, 0),
            new Location(Bukkit.getWorld("Party"), 1975, 102, 2000, -90, 0),
            new Location(Bukkit.getWorld("Party"), 1987, 102, 1978, -30, 0)
    };

    private Participant[][] drainedBlocks = new Participant[31][31];

    private final int DRAIN_POINTS_18 = 200;
    private final int DRAIN_POINTS_24 = 320;
    private final int DRAIN_POINTS = DRAIN_POINTS_24;

    private final int PATTERN_POINTS_18 = 100;
    private final int PATTERN_POINTS_24 = 160;
    private final int PATTERN_POINTS = PATTERN_POINTS_24;

    private final int SPECIAL_POINTS_18 = 100;
    private final int SPECIAL_POINTS_24 = 160;
    private final int SPECIAL_POINTS = SPECIAL_POINTS_24;

    private final org.bukkit.World world = Bukkit.getWorld("Party");

    private final List<String> specialtasks = new ArrayList<>(
            Arrays.asList("1x1", "Mass", "Edge", "Center", "Team Lover", "Team Hater", "Open Area"));
    private String currentSpecialTask;
    private final Map<String, String> taskExplanations = Map.of("1x1",
            "Land in a 1x1 area surrounded by blocks for extra points!",
            "Mass", "Have the largest continuous land area for extra points!",
            "Edge", "Land next to the edges (white concrete) for extra points!",
            "Center", "Land in the center (black concrete) for extra points!",
            "Team Lover", "Land next to your team's wool for extra points!",
            "Team Hater", "Land away from your team's wool for extra points!",
            "Open Area", "Land away from everyone and everything for extra points!");
    private Map<Participant, Integer> taskCounter = new HashMap<Participant, Integer>();

    private int patternSelected;
    private String patternName;
    private String patternDisplayed;
    private int rotationsReflections;

    // private final List<String> specialConditions = new
    // ArrayList<>(Arrays.asList("Scattered Blocks", "Punching", "Full Dropper",
    // "Wind Charges", "Nothing", "Random Blindness"));
    // private String currentSpecialCondition = "";
    // private final Map<String, String> conditionExplanations = Map.of("Scattered
    // Blocks", "Avoid blocks on your way down...",
    // "Punching", "PVP will be turned on...",
    // "Full Dropper", "Start from the center...",
    // "Wind Charges", "Start with a windcharge every round...",
    // "Nothing", "A very normal round!",
    // "Random Blindness", "Every once and a while, lose vision..."
    // );

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new Drain();
        }
        return instance;
    }

    private Drain() {
        super("Drain", new String[] {
                "⑰ In Drain, your goal is to DRAIN the water at the bottom of the map.\n\n" +
                        "⑰ By jumping into water, the block will be replaced with your team's wool.",
                "⑰ Your general goal is to have as many tiles of your team's color as possible.\n\n" +
                        "⑰ However, you also get points for copying a pattern and performing specific tasks.",
                "⑰ Points are split proportionally based on how much of that task you completed.\n\n" +
                        "⑰ The more you can perform a task compared to other teams, the more points you get!",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +320 points, split proportionally by tiles drained\n" +
                        "⑰ +160 points, split proportionally by patterns created\n" +
                        "⑰ +160 points, split proportionally by specific task completed" });

    }

    @Override
    public void start() {
        super.start();

        teamsAlive.addAll(getValidTeams());

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setInvulnerable(false);
        }

        resetMap();
        Bukkit.getWorld("Party").setGameRule(GameRule.FALL_DAMAGE, true);

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    @Override
    public void endEvents() {

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().stopSound("igm.drain", SoundCategory.RECORDS);
        }

        logger.logStats();
        Bukkit.getWorld("Party").setGameRule(GameRule.FALL_DAMAGE, false);

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
        resetMap();
    }

    public void resetMap() {
        for (int x = 1985; x <= 2015; x++) {
            for (int z = 1985; z <= 2015; z++) {
                if (!Bukkit.getWorld("Party").getBlockAt(x, 1, z).getType().equals(Material.WATER)
                        && !Bukkit.getWorld("Party").getBlockAt(x, 1, z).getType().equals(Material.WHITE_CONCRETE)) {
                    Bukkit.getWorld("Party").getBlockAt(x, 1, z).setType(Material.WATER);
                }
            }
        }
        Barriers(true);
    }

    @Override
    public void loadPlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getInventory().clear();
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setFlying(false);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().teleport(getSpawnLocation(p));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE,
                    Team.OptionStatus.NEVER);

            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(boots));
        }
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
                if (timeRemaining == 25) {
                    pickSpecialTask();
                }
                if (timeRemaining == 20) {
                    pickPattern();
                }
                if (timeRemaining == 12) {
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getPlayer().playSound(p.getPlayer(), "igm.drain", SoundCategory.RECORDS, 1, 1);

                    }
                }
                if (timeRemaining == 0) {
                    Barriers(false);
                    setGameState(GameState.ACTIVE);
                    setTimer(150);
                }
                break;
            case ACTIVE:
                if (timeRemaining == 0) {
                    setGameState(GameState.END_ROUND);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Game over! Points will be distrbuted shortly...");
                    setTimer(30);
                }

                break;
            case END_ROUND:
                if (timeRemaining == 25) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, "sfx.kill_coins", SoundCategory.RECORDS, 0.5f, 0.9f);
                    }
                    specialPoints();
                }
                if (timeRemaining == 20) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, "sfx.kill_coins", SoundCategory.RECORDS, 0.5f, 1);
                    }
                    patternPoints();
                }
                if (timeRemaining == 15) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
                    }
                    generalPoints();
                }
                if (timeRemaining == 0) {
                    endEvents();
                    return;
                }
                break;
        }
    }

    /**
     * Calculates, gives, and broadcasts message concerning special task points.
     */
    public void specialPoints() {
        if (currentSpecialTask.equals("Mass")) {
            Map<MBCTeam, Integer> massPoints = massPoints();
            for (MBCTeam t : massPoints.keySet()) {
                for (Participant p : t.getPlayers()) {
                    taskCounter.put(p, massPoints.get(t) / t.getPlayers().size());
                }
            }
        }

        Map<Participant, Integer> pointMap = new HashMap<Participant, Integer>();
        Map<MBCTeam, Integer> pointMapTeam = new HashMap<MBCTeam, Integer>();
        int totalTasks = 0;
        for (Participant p : taskCounter.keySet()) {
            totalTasks += taskCounter.get(p);
        }

        for (Participant p : taskCounter.keySet()) {
            int tasks = taskCounter.get(p);
            int pointsGained = (int) (SPECIAL_POINTS
                    * Math.ceil(1.0 / (double) (MBC.getInstance().getPlayers().size())));
            if (totalTasks != 0) {
                pointsGained = (int) (SPECIAL_POINTS * Math.ceil(((double) tasks) / ((double) totalTasks)));
            }
            p.addCurrentScore(pointsGained);

            pointMap.put(p, pointsGained);
            if (pointMapTeam.containsKey(p.getTeam())) {
                pointMapTeam.replace(p.getTeam(), pointMapTeam.get(p.getTeam()) + pointsGained);
            } else {
                pointMapTeam.put(p.getTeam(), pointsGained);
            }
        }

        String message = "\n" + ChatColor.BOLD + "General Task Points By Team:\n";
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            if (!pointMapTeam.containsKey(t))
                pointMapTeam.put(t, 0);
            message = message + t.teamNameFormat() + ChatColor.BOLD + ": " + (pointMapTeam.get(t)) + " task points\n";
        }
        logger.log(message);
        Bukkit.broadcastMessage(message);

        for (Participant p : MBC.getInstance().getPlayers()) {
            String playerMessage = "";
            if (pointMap.containsKey(p)) {
                playerMessage = ChatColor.GREEN + "You scored " + ChatColor.BOLD + pointMap.get(p) + ChatColor.RESET
                        + "" + ChatColor.GREEN + " points for your team from your tasks!";
                p.getPlayer().sendMessage(playerMessage);
                logger.log(p.getFormattedName() + ": " + taskCounter.get(p) + " tasks, " + pointMap.get(p)
                        + " points gained");
            }
        }
    }

    /**
     * Uses DFS algorithm to find largest continuous area for each team, and returns map containing mass info for all teams. Only runs if drain task is mass.
     */
    public Map<MBCTeam, Integer> massPoints() {
        MBCTeam[][] drainedBlocksTeam = new MBCTeam[31][31];
        for (int i = 0; i < drainedBlocks.length; i++) {
            for (int j = 0; j < drainedBlocks[i].length; j++) {
                if (drainedBlocks[i][j] != null) {
                    Participant drainer = drainedBlocks[i][j];
                    MBCTeam drainerTeam = drainer.getTeam();
                    drainedBlocksTeam[i][j] = drainerTeam;
                }
            }
        }

        boolean[][] visited = new boolean[31][31];
        Map<MBCTeam, Integer> maxMass = new HashMap<MBCTeam, Integer>();

        for (int i = 0; i < drainedBlocksTeam.length; i++) {
            for (int j = 0; j < drainedBlocksTeam[i].length; j++) {
                if (drainedBlocksTeam[i][j] != null && !visited[i][j]) {
                    MBCTeam team = drainedBlocksTeam[i][j];
                    int size = drainDFS(drainedBlocksTeam, visited, i, j, team);

                    if (maxMass.containsKey(team)) {
                        maxMass.replace(team, Math.max(size, maxMass.get(team)));
                    } else {
                        maxMass.put(team, size);
                    }
                }
            }
        }

        return maxMass;
    }

    /**
     * DFS algorithm to find largest continuous area for any MBCTeam team.
     */
    public int drainDFS(MBCTeam[][] blocks, boolean[][] visited, int row, int column, MBCTeam team) {
        if (row < 0 || column < 0 || row >= blocks.length || column >= blocks.length) return 0;
        if (blocks[row][column] == null) return 0;
        if (visited[row][column] || !blocks[row][column].equals(team)) return 0;

        visited[row][column] = true;

        int size = 1;

        size += drainDFS(blocks, visited, row + 1, column, team);
        size += drainDFS(blocks, visited, row - 1, column, team);
        size += drainDFS(blocks, visited, row, column + 1, team);
        size += drainDFS(blocks, visited, row, column - 1, team);

        return size;
    }

    /**
     * Searches for and awards points for patterns in drain arena.
     */
    public void patternPoints() {
        Map<MBCTeam, Integer> patternsFound = new HashMap<>();
        int totalPatterns = 0;
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            for (int x = 1985; x <= 2015; x++) {
                for (int z = 1985; z <= 2015; z++) {
                    if (Bukkit.getWorld("Party").getBlockAt(x, 1, z).getType().equals(Material.WHITE_CONCRETE)) continue;
                    int patterns = checkPattern(x, z, 997+(6*patternSelected), 1000, t);

                    totalPatterns+=patterns;

                    if (patternsFound.containsKey(t)) {patternsFound.put(t, patternsFound.get(t)+patterns);}
                    else {patternsFound.put(t, patterns);}
                }
            }
            if (!patternsFound.containsKey(t)) patternsFound.put(t, 0);
        }

        Map<Participant, Integer> pointMap = new HashMap<Participant, Integer>();
        Map<MBCTeam, Integer> pointMapTeam = new HashMap<MBCTeam, Integer>();

        for (MBCTeam t : patternsFound.keySet()) {
            for (Participant p : t.getPlayers()) {
                int patterns = patternsFound.get(t) / t.getPlayers().size();
                int pointsGained = (int) (PATTERN_POINTS * Math.ceil(1.0 / (double) (MBC.getInstance().getPlayers().size())));
                if (totalPatterns != 0) {
                    pointsGained = (int) (PATTERN_POINTS * Math.ceil(((double) patterns) / ((double) totalPatterns)));
                }
                p.addCurrentScore(pointsGained);

                pointMap.put(p, pointsGained);
                if (pointMapTeam.containsKey(p.getTeam())) {
                    pointMapTeam.replace(p.getTeam(), pointMapTeam.get(p.getTeam()) + pointsGained);
                } else {
                    pointMapTeam.put(p.getTeam(), pointsGained);
                }
            }
            
        }

        String message = "\n" + ChatColor.BOLD + "General Pattern Points By Team:\n";
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            if (!pointMapTeam.containsKey(t))
                pointMapTeam.put(t, 0);
            message = message + t.teamNameFormat() + ChatColor.BOLD + ": " + (pointMapTeam.get(t)) + " pattern points\n";
        }
        logger.log(message);
        Bukkit.broadcastMessage(message);

        for (Participant p : MBC.getInstance().getPlayers()) {
            String playerMessage = "";
            if (pointMap.containsKey(p)) {
                playerMessage = ChatColor.GREEN + "Your team made " + patternsFound.get(p.getTeam()) + " patterns, scoring you "
                        + ChatColor.BOLD + pointMap.get(p) + ChatColor.RESET + "" + ChatColor.GREEN
                        + " points!";
                p.getPlayer().sendMessage(playerMessage);
                logger.log(p.getFormattedName() + ": " + patternsFound.get(p.getTeam()) + " blocks drained, " + pointMap.get(p)
                        + " points gained");
            }
        }
       
    }

    /**
     * Takes the x and z coordinates for the pattern and drained block being checked, along with a team. Returns number of patterns found for team. 
     */
    public int checkPattern(int xDrained, int zDrained, int xPattern, int zPattern, MBCTeam team) {
        Material teamWool = team.getColoredWool().getType();
        int xAdd = 0;
        int zAdd = 1;
        boolean[] check = {true, true, true, true};
        int counter = 0; 

        if (Bukkit.getWorld("Party").getBlockAt(xPattern, -60, zPattern).getType().equals(Material.WHITE_WOOL) &&
            Bukkit.getWorld("Party").getBlockAt(xDrained, 1, zDrained).getType().equals(teamWool)) return 0;
        if (Bukkit.getWorld("Party").getBlockAt(xPattern, -60, zPattern).getType().equals(Material.BLACK_WOOL) &&
            !Bukkit.getWorld("Party").getBlockAt(xDrained, 1, zDrained).getType().equals(teamWool)) return 0;
        

        while (xAdd <= 2) {
            while (zAdd <=2){
                if (Bukkit.getWorld("Party").getBlockAt(xPattern + xAdd, -60, zPattern + zAdd).getType().equals(Material.WHITE_WOOL)) {
                    if (Bukkit.getWorld("Party").getBlockAt(xDrained + xAdd, 1, zDrained + zAdd).getType().equals(teamWool)) {check[0] = false;}
                    if (Bukkit.getWorld("Party").getBlockAt(xDrained + zAdd, 1, zDrained - xAdd).getType().equals(teamWool)) {check[1] = false;}
                    if (Bukkit.getWorld("Party").getBlockAt(xDrained - xAdd, 1, zDrained - zAdd).getType().equals(teamWool)) {check[2] = false;}
                    if (Bukkit.getWorld("Party").getBlockAt(xDrained - zAdd, 1, zDrained + xAdd).getType().equals(teamWool)) {check[3] = false;}
                } 
                else if (Bukkit.getWorld("Party").getBlockAt(xPattern + xAdd, -60, zPattern + zAdd).getType().equals(Material.BLACK_WOOL)) {
                    if (!Bukkit.getWorld("Party").getBlockAt(xDrained + xAdd, 1, zDrained + zAdd).getType().equals(teamWool)) {check[0] = false;}
                    if (!Bukkit.getWorld("Party").getBlockAt(xDrained + zAdd, 1, zDrained - xAdd).getType().equals(teamWool)) {check[1] = false;}
                    if (!Bukkit.getWorld("Party").getBlockAt(xDrained - xAdd, 1, zDrained - zAdd).getType().equals(teamWool)) {check[2] = false;}
                    if (!Bukkit.getWorld("Party").getBlockAt(xDrained - zAdd, 1, zDrained + xAdd).getType().equals(teamWool)) {check[3] = false;}
                }  
                zAdd++;
            }
            xAdd++;
        }
        
        for (boolean b : check) {
            if (b) {
                counter++;
                Bukkit.broadcastMessage("pattern found at origin block x: " + xDrained + ", z:" + zDrained);
            }
        }
        return counter;

    }

    /**
     * Gives general points to all players for draining blocks.
     */
    public void generalPoints() {
        Map<Participant, Integer> drainMap = new HashMap<Participant, Integer>();
        Map<Participant, Integer> pointMap = new HashMap<Participant, Integer>();
        Map<MBCTeam, Integer> pointMapTeam = new HashMap<MBCTeam, Integer>();
        int totalDrained = 0;
        for (int i = 0; i < drainedBlocks.length; i++) {
            for (int j = 0; j < drainedBlocks[i].length; j++) {
                if (drainedBlocks[i][j] != null) {
                    Participant drainer = drainedBlocks[i][j];
                    if (drainMap.containsKey(drainer)) {
                        drainMap.replace(drainer, drainMap.get(drainer) + 1);
                    } else {
                        drainMap.put(drainer, 1);
                    }
                    totalDrained++;
                }
            }
        }

        for (Participant p : drainMap.keySet()) {
            int drained = drainMap.get(p);
            int pointsGained = (int) (DRAIN_POINTS * Math.ceil(1.0 / (double) (MBC.getInstance().getPlayers().size())));
            if (totalDrained != 0) {
                pointsGained = (int) (DRAIN_POINTS * Math.ceil(((double) drained) / ((double) totalDrained)));
            }
            p.addCurrentScore(pointsGained);

            pointMap.put(p, pointsGained);
            if (pointMapTeam.containsKey(p.getTeam())) {
                pointMapTeam.replace(p.getTeam(), pointMapTeam.get(p.getTeam()) + pointsGained);
            } else {
                pointMapTeam.put(p.getTeam(), pointsGained);
            }
        }

        String message = "\n" + ChatColor.BOLD + "General Drain Points By Team:\n";
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            if (!pointMapTeam.containsKey(t))
                pointMapTeam.put(t, 0);
            message = message + t.teamNameFormat() + ChatColor.BOLD + ": " + (pointMapTeam.get(t)) + " drain points\n";
        }
        logger.log(message);
        Bukkit.broadcastMessage(message);

        for (Participant p : MBC.getInstance().getPlayers()) {
            String playerMessage = "";
            if (pointMap.containsKey(p)) {
                playerMessage = ChatColor.GREEN + "For draining " + drainMap.get(p) + " blocks, you scored "
                        + ChatColor.BOLD + pointMap.get(p) + ChatColor.RESET + "" + ChatColor.GREEN
                        + " points for your team!";
                p.getPlayer().sendMessage(playerMessage);
                logger.log(p.getFormattedName() + ": " + drainMap.get(p) + " blocks drained, " + pointMap.get(p)
                        + " points gained");
            }
        }
    }

    /**
     * Will respawn any participant p in correct spawn location based on their team. If no correct team, respawns at red team spawn.
     */
    public Location getSpawnLocation(Participant p) {
        int number = MBC.getInstance().teams.indexOf(p.getTeam());

        if (number > SPAWN.length || number < 0)
            return SPAWN[0];
        return SPAWN[number];
    }

    /**
     * Picks and displays special task. Keeps result in String currentSpecialTask. 
     */
    public void pickSpecialTask() {
        currentSpecialTask = specialtasks.get((int) (specialtasks.size() * Math.random()));
        String taskExplanation = "";
        if (taskExplanations.get(currentSpecialTask) != null) {
            taskExplanation = taskExplanations.get(currentSpecialTask);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "This round of Drain will be affected by "
                    + currentSpecialTask + "!");
            p.sendMessage(ChatColor.GRAY + taskExplanation);
            createLine(19, ChatColor.GOLD + "" + ChatColor.BOLD + "Task: " + ChatColor.RESET + currentSpecialTask, Participant.getParticipant(p));
            p.sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + currentSpecialTask, ChatColor.GRAY + taskExplanation, 5, 70, 5);
            p.playSound(p, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.RECORDS, 1, 1);
        }
    }

    /**
     * Picks and displays pattern.
     */
    public void pickPattern() {
        int xCheck = 1000;
        int patterns = 0;
        while (Bukkit.getWorld("Party").getBlockAt(xCheck, -60, 1003).getType().equals(Material.DIAMOND_BLOCK)) {
            patterns++;
            xCheck+=6;
        }
        if (patterns == 0) return;

        patternSelected = (int)(Math.random()*patterns);

        Block diamondBlock = Bukkit.getWorld("Party").getBlockAt(1000+(6*patternSelected), -60, 1003);
        Block sign = Bukkit.getWorld("Party").getBlockAt(diamondBlock.getLocation().getBlockX()-4, diamondBlock.getLocation().getBlockY()+1, diamondBlock.getLocation().getBlockZ());

        if (sign.getType().equals(Material.OAK_SIGN)) {
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines[1].isBlank()) {
                String s = lines[0];
                patternName = s.trim();
            } else {
                StringBuilder str = new StringBuilder();
                for (String s : lines) {
                    str.append(s.trim()).append(" ");
                }
                str.replace(str.length()-1, str.length(), "");
                patternName = str.toString().trim();
            }
        } else {
            patternName = "Pattern";
        }

        patternDisplayed = "";
        for (int z = 1000; z <= 1002; z++) {
            for (int x = 997+(6*patternSelected); x <= 999 + (6*patternSelected); x++) {
                if (Bukkit.getWorld("Party").getBlockAt(x, -60, z).getType().equals(Material.BLACK_WOOL)) {
                    patternDisplayed += "X";
                }
                else {
                    patternDisplayed += "O";
                }
            }
        }

        if (Bukkit.getWorld("Party").getBlockAt(1000+(6*patternSelected), -60, 999).getType().equals(Material.EMERALD_BLOCK)) {rotationsReflections = 4;}
        else if (Bukkit.getWorld("Party").getBlockAt(1000+(6*patternSelected), -60, 999).getType().equals(Material.GOLD_BLOCK)) {rotationsReflections = 2;}
        else {rotationsReflections = 1;}

        for (Player p : Bukkit.getOnlinePlayers()) {
            String displayPlayer = "";
            ChatColor color = Participant.getParticipant(p).getTeam().getChatColor();
            for (int i = 0; i < patternDisplayed.length(); i++) {
                if (patternDisplayed.charAt(i) == 'X') {
                    String colorWool = color + "■";
                    displayPlayer += colorWool;
                }
                if (patternDisplayed.charAt(i) == 'O') {
                    String colorWool = ChatColor.WHITE + "■";
                    displayPlayer += colorWool;
                }
                if (i % 3 == 2) {
                    displayPlayer += "\n";
                }
            }
            p.sendMessage(displayPlayer);
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Make the " + patternName + " pattern to gain extra points!");
            p.sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "Pattern: " +patternName, ChatColor.GRAY + "Check chat or the outside of spawn to see the full pattern.", 5, 70, 5);
            createLine(18, ChatColor.GOLD + "" + ChatColor.BOLD + "Pattern: " + ChatColor.RESET + patternName, Participant.getParticipant(p));
            p.playSound(p, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.RECORDS, 1, 1);
        }

        for (int y = 4; y >= 2; y--) {
            int zStuck = 2040;
            for (int x = 3; x >= 1; x--) {
                Material nextBlock = Bukkit.getWorld("Party").getBlockAt(diamondBlock.getLocation().getBlockX()-x, y-60, diamondBlock.getLocation().getBlockZ()-5).getType();
                Bukkit.getWorld("Party").getBlockAt(x+1998, y+101, zStuck).setType(nextBlock);
            }

            zStuck = 1960;
            for (int x = 3; x >= 1; x--) {
                Material nextBlock = Bukkit.getWorld("Party").getBlockAt(diamondBlock.getLocation().getBlockX()-4+x, y-60, diamondBlock.getLocation().getBlockZ()-5).getType();
                Bukkit.getWorld("Party").getBlockAt(x+1998, y+101, zStuck).setType(nextBlock);
            }

            int xStuck = 2040;
            for (int z = 3; z >= 1; z--) {
                Material nextBlock = Bukkit.getWorld("Party").getBlockAt(diamondBlock.getLocation().getBlockX()-4+z, y-60, diamondBlock.getLocation().getBlockZ()-5).getType();
                Bukkit.getWorld("Party").getBlockAt(xStuck, y+101, z+1998).setType(nextBlock);
            }

            xStuck = 1960;
            for (int z = 3; z >= 1; z--) {
                Material nextBlock = Bukkit.getWorld("Party").getBlockAt(diamondBlock.getLocation().getBlockX()-z, y-60, diamondBlock.getLocation().getBlockZ()-5).getType();
                Bukkit.getWorld("Party").getBlockAt(xStuck, y+101, z+1998).setType(nextBlock);
            }
        }
    }
    //
    // public void pickSpecialCondition() {
    // currentSpecialCondition =
    // specialConditions.get((int)(specialConditions.size()*Math.random()));
    // String conditionExplanaiton = "";
    // if (conditionExplanations.get(currentSpecialTask) != null) {
    // conditionExplanaiton = conditionExplanations.get(currentSpecialTask);
    // }
    // for (Player p : Bukkit.getOnlinePlayers()) {
    // p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "The condition of this
    // round of Drain is " + currentSpecialCondition + " !");
    // p.sendMessage(ChatColor.RED + conditionExplanaiton);
    // p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + currentSpecialCondition,
    // ChatColor.RED + conditionExplanaiton, 5,70,5);
    // p.playSound(p, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.RECORDS,1, 1);
    // }
    // }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    /**
     * Players cannot take off armor.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i.equals(Material.LEATHER_HELMET))
            e.setCancelled(true);
        if (i.equals(Material.LEATHER_CHESTPLATE))
            e.setCancelled(true);
        if (i.equals(Material.LEATHER_LEGGINGS))
            e.setCancelled(true);
        if (i.equals(Material.LEATHER_BOOTS))
            e.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!isGameActive())
            return;

        event.setCancelled(true);
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(25, String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum,
                ChatColor.WHITE, "Party (" + name()) + ")", p);
        createLine(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW,
                MBC.getInstance().multiplier, ChatColor.RESET), p);
        createLine(17, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updateInGameTeamScoreboard();
    }

    /**
     * Determine whether or not to put up barrier walls for start of game.
     *
     * @param b Boolean for state of barriers: true = Barriers, false = Air
     */
    public void Barriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        for (int y = 102; y <= 105; y++) {
            for (int x = 1960; x <= 2040; x++) {
                for (int z = 1960; z <= 2040; z++) {
                    if (Bukkit.getWorld("Party").getBlockAt(x, 101, z).getType().equals(Material.SMOOTH_QUARTZ)
                            && !Bukkit.getWorld("Party").getBlockAt(x, y, z).getType()
                                    .equals(Material.WHITE_STAINED_GLASS)) {
                        Bukkit.getWorld("Party").getBlockAt(x, y, z).setType(m);
                    }
                }
            }
        }
    }
    /**
     * Detects drain and special task points.
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getY() <= 1.5 && Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), (int) p.getZ())
                .getType().equals(Material.WATER)) {
            Participant part = Participant.getParticipant(p);
            if (!getState().equals(GameState.ACTIVE) || !p.getGameMode().equals(GameMode.ADVENTURE)) {
                p.teleport(getSpawnLocation(part));
                return;
            }
            p.sendMessage(ChatColor.GREEN + "You drained a block!");
            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), (int) p.getZ())
                    .setType(part.getTeam().getColoredWool().getType());
            drainedBlocks[(int) p.getX() - 1985][(int) p.getZ() - 1985] = part;

            Material teamWool = part.getTeam().getColoredWool().getType();
            switch (specialtasks.indexOf(currentSpecialTask)) {
                case (0):
                    if (!Bukkit.getWorld("Party").getBlockAt((int) p.getX() + 1, (int) p.getY(), (int) p.getZ())
                            .getType().equals(Material.WATER) &&
                            !Bukkit.getWorld("Party").getBlockAt((int) p.getX() - 1, (int) p.getY(), (int) p.getZ())
                                    .getType().equals(Material.WATER)
                            &&
                            !Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), (int) p.getZ() + 1)
                                    .getType().equals(Material.WATER)
                            &&
                            !Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), (int) p.getZ() - 1)
                                    .getType().equals(Material.WATER)) {

                        p.sendMessage(ChatColor.GOLD + "By landing in the 1x1, you completed a task!");

                        if (!taskCounter.containsKey(part)) {
                            taskCounter.put(part, 1);
                        } else {
                            taskCounter.replace(part, taskCounter.get(part) + 1);
                        }
                    }
                    break;
                case (2):
                    if (Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) + 1, (int) p.getY(), (int) p.getZ())
                            .getType().equals(Material.WHITE_CONCRETE) ||
                            Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) - 1, (int) p.getY(), (int) p.getZ())
                                    .getType().equals(Material.WHITE_CONCRETE)
                            ||
                            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) + 1)
                                    .getType().equals(Material.WHITE_CONCRETE)
                            ||
                            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) - 1)
                                    .getType().equals(Material.WHITE_CONCRETE)) {

                        p.sendMessage(ChatColor.GOLD + "By landing on the edge, you completed a task!");

                        if (!taskCounter.containsKey(part)) {
                            taskCounter.put(part, 1);
                        } else {
                            taskCounter.replace(part, taskCounter.get(part) + 1);
                        }
                    }
                    break;
                case (3):
                    if (Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY() - 1, (int) p.getZ())
                            .getType().equals(Material.BLACK_CONCRETE)) {

                        p.sendMessage(ChatColor.GOLD + "By landing in the center, you completed a task!");

                        if (!taskCounter.containsKey(part)) {
                            taskCounter.put(part, 1);
                        } else {
                            taskCounter.replace(part, taskCounter.get(part) + 1);
                        }
                    }
                    break;
                case (4):
                    if (Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) + 1, (int) p.getY(), (int) p.getZ())
                            .getType().equals(teamWool) ||
                            Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) - 1, (int) p.getY(), (int) p.getZ())
                                    .getType().equals(teamWool)
                            ||
                            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) + 1)
                                    .getType().equals(teamWool)
                            ||
                            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) - 1)
                                    .getType().equals(teamWool)) {

                        p.sendMessage(ChatColor.GOLD + "By landing next to your team, you completed a task!");

                        if (!taskCounter.containsKey(part)) {
                            taskCounter.put(part, 1);
                        } else {
                            taskCounter.replace(part, taskCounter.get(part) + 1);
                        }
                    }
                    break;
                case (5):
                    if (!Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) + 1, (int) p.getY(), (int) p.getZ())
                            .getType().equals(teamWool) &&
                            !Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) - 1, (int) p.getY(), (int) p.getZ())
                                    .getType().equals(teamWool)
                            &&
                            !Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) + 1)
                                    .getType().equals(teamWool)
                            &&
                            !Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) - 1)
                                    .getType().equals(teamWool)) {

                        p.sendMessage(ChatColor.GOLD + "By landing away from your team, you completed a task!");

                        if (!taskCounter.containsKey(part)) {
                            taskCounter.put(part, 1);
                        } else {
                            taskCounter.replace(part, taskCounter.get(part) + 1);
                        }
                    }
                    break;
                case (6):
                    if (Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) + 1, (int) p.getY(), (int) p.getZ())
                            .getType().equals(Material.WATER) &&
                            Bukkit.getWorld("Party").getBlockAt(((int) p.getX()) - 1, (int) p.getY(), (int) p.getZ())
                                    .getType().equals(Material.WATER)
                            &&
                            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) + 1)
                                    .getType().equals(Material.WATER)
                            &&
                            Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), ((int) p.getZ()) - 1)
                                    .getType().equals(Material.WATER)
                            &&
                            Bukkit.getWorld("Party")
                                    .getBlockAt(((int) p.getX()) + 1, (int) p.getY(), ((int) p.getZ()) + 1).getType()
                                    .equals(Material.WATER)
                            &&
                            Bukkit.getWorld("Party")
                                    .getBlockAt(((int) p.getX()) - 1, (int) p.getY(), ((int) p.getZ()) - 1).getType()
                                    .equals(Material.WATER)
                            &&
                            Bukkit.getWorld("Party")
                                    .getBlockAt(((int) p.getX()) - 1, (int) p.getY(), ((int) p.getZ()) + 1).getType()
                                    .equals(Material.WATER)
                            &&
                            Bukkit.getWorld("Party")
                                    .getBlockAt(((int) p.getX()) + 1, (int) p.getY(), ((int) p.getZ()) - 1).getType()
                                    .equals(Material.WATER)) {

                        p.sendMessage(ChatColor.GOLD + "By landing away from everyone, you completed a task!");

                        if (!taskCounter.containsKey(part)) {
                            taskCounter.put(part, 1);
                        } else {
                            taskCounter.replace(part, taskCounter.get(part) + 1);
                        }
                    }
                    break;
            }
            p.teleport(getSpawnLocation(part));
            return;
        }
        if (p.getY() <= 1.5 && !Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY(), (int) p.getZ())
                .getType().equals(Material.WATER)) {
            Participant part = Participant.getParticipant(p);
            p.teleport(getSpawnLocation(part));
            return;
        }
        // if (p.getY() <= 2.5 && (!Bukkit.getWorld("Party").getBlockAt((int)p.getX(),
        // ((int)p.getY())-1, (int)p.getZ()).getType().equals(Material.WATER) &&
        // !Bukkit.getWorld("Party").getBlockAt((int)p.getX(), ((int)p.getY()),
        // (int)p.getZ()).getType().equals(Material.WATER))) {
        // Participant part = Participant.getParticipant(p);
        // p.teleport(getSpawnLocation(part));
        // return;
        // }
        List<Material> layerTwoMaterials = new ArrayList<>(Arrays.asList(Material.SMOOTH_QUARTZ, Material.RED_CONCRETE,
                Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.BLUE_CONCRETE, Material.PURPLE_CONCRETE,
                Material.PINK_CONCRETE));
        if (p.getY() <= 3.5 && layerTwoMaterials.contains(
                Bukkit.getWorld("Party").getBlockAt((int) p.getX(), (int) p.getY() - 1, (int) p.getZ()).getType())) {
            Participant part = Participant.getParticipant(p);
            p.teleport(getSpawnLocation(part));
            return;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getPlayer();
        EntityDamageEvent damageEvent = event.getPlayer().getLastDamageCause();
        if (damageEvent.getCause().equals(DamageCause.FALL)) {
            Participant part = Participant.getParticipant(p);
            p.teleport(getSpawnLocation(part));
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (getState() != GameState.ACTIVE) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player) {
            if (event.getCause().equals(DamageCause.FALL)) {
                Player play = (Player) event.getEntity();
                Participant part = Participant.getParticipant(play);
                play.teleport(getSpawnLocation(part));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (!getState().equals(GameState.ACTIVE))
            return;
        Participant p = Participant.getParticipant(e.getPlayer());
        Bukkit.broadcastMessage(p.getFormattedName() + " has disconnected!");
        logger.log(p.getPlayerName() + " disconnected!");

    }

    @Override
    public World world() {
        return world;
    }

}
