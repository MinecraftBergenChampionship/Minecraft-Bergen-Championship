package me.kotayka.mbc.games;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.sgMaps.BCA;
import me.kotayka.mbc.gameMaps.sgMaps.SurvivalGamesMap;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

public class SurvivalGames extends Game {
    private final SurvivalGamesMap map = new BCA();
    private List<SurvivalGamesItem> items;
    private final File CHEST_FILE = new File("survival_games_items.json");

    public SurvivalGames() {
        super(5, "SurvivalGames");


        try {
            readItems();
        } catch(IOException | ParseException e) {
            Bukkit.broadcastMessage(ChatColor.YELLOW+ e.getMessage());
            Bukkit.broadcastMessage(ChatColor.RED+"Unable to parse " + CHEST_FILE.getAbsolutePath());
        }

        loadPlayers();

    }

    private void readItems() throws IOException, ParseException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<SurvivalGamesItem>>() {}.getType();
        Reader reader = new FileReader(CHEST_FILE);
        items = gson.fromJson(reader, listType);
    }

    @Override
    public void loadPlayers() {
        map.setBarriers(true);
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 10, false, false));
            map.spawnPlayers();
        }
        regenChest();
    }

    @Override
    public void start() {
        super.start();

        // setGameState(TUTORIAL);
        setGameState(GameState.STARTING);

        setTimer(30);
    }

    @Override
    public void events() {

    }

    /**
     * Regenerates the loot within every chest in the map.
     */
    public void regenChest() {
        // TEMP
        /*
        int totalWeight = 0;
        for (int i = 0; i < items.size(); i++) {
            items.get(i).
        }*/

        Random rand = new Random();
        Chunk[] c = map.getWorld().getLoadedChunks();
        for (Chunk chunk : c) {//loop through loaded chunks
            for (int x = 0; x < chunk.getTileEntities().length; x++) {//loop through tile entities within loaded chunks
                if (chunk.getTileEntities()[x] instanceof Chest) {
                    Chest chest = (Chest) chunk.getTileEntities()[x];
                    chest.getInventory().clear();
                    int chestItems = rand.nextInt(2) + 6;
                    for (int b = 0; b < chestItems; b++) {
                        int lootNum = rand.nextInt(items.size());
                        chest.getInventory().setItem(rand.nextInt(27), items.get(lootNum).getItem());
                    }
                }
            }
        }
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Game: "+ MBC.getInstance().gameNum+"/6:" + ChatColor.WHITE + " Survival Games", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);
        createLine(2, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.MAX_PLAYERS);
        createLine(1, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
        createLine(0, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+"0");

        teamRounds();
    }

    @EventHandler
    public void onEatStew(PlayerInteractEvent e) {
        if (!isGameActive()) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK) && !(e.getAction() == Action.RIGHT_CLICK_AIR)) return;
        Player p = e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() != Material.MUSHROOM_STEW && p.getInventory().getItemInMainHand().getType() != Material.MUSHROOM_STEW) {
            return;
        }

        boolean mainHand = p.getInventory().getItemInMainHand().getType() == Material.MUSHROOM_STEW;
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, true));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        map.getWorld().spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 3, Material.DIRT);

        if (mainHand) {
            p.getInventory().setItemInMainHand(null);
        } else {
            p.getInventory().setItemInOffHand(null);
        }
    }
}

class SurvivalGamesItem {
    private Material material;
    private int stack_max;
    private int weight;

    public SurvivalGamesItem() {}

    public ItemStack getItem() {
        return new ItemStack(material, (int) (Math.random() * stack_max) +1);
    }
}