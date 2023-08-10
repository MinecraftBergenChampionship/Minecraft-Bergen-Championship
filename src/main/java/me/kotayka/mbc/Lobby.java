package me.kotayka.mbc;

import me.kotayka.mbc.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Lobby extends Minigame {
    public static final Location LOBBY = new Location(Bukkit.getWorld("world"), 0, 1, 0, 180, 0);
    public final World world = Bukkit.getWorld("world");

    public Lobby() {
        super("Lobby");
        colorPodiumsWhite();
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

    @Override
    public void start() {
        MBC.getInstance().setCurrentGame(this);
        createScoreboard();
        loadPlayers();
        stopTimer();
        setTimer(120);
        setGameState(GameState.ACTIVE);
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

    public void prepareFinale() {
        MBC.getInstance().setCurrentGame(this);
        createScoreboard();
        stopTimer();
        setTimer(60);
        setGameState(GameState.END_ROUND);


    }

    public void colorPodiumsWhite() {
        for (int i = 0; i < 6; i++) {
            colorPodium(i, Material.WHITE_CONCRETE);
        }
    }
    public void colorPodiums() {
        for (MBCTeam t : MBC.getInstance().teamScores) {
            colorPodium(t.getPlace(), t.getConcrete().getType());
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
                for (int y = -2; y <= 0; y++) {
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
        world.getBlockAt(-26, 5, -32).setType(m);
        world.getBlockAt(-25, 5, -32).setType(m);
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
}
