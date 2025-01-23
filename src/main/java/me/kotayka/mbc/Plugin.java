package me.kotayka.mbc;

import me.kotayka.mbc.commands.*;
import me.kotayka.mbc.commands.tab.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class Plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        MBC.getInstance(this);

        MBC.getInstance().showAllPlayers();
        for (Player p : Bukkit.getOnlinePlayers()) {
            MBC.getInstance().players.add(new Participant(p));
            p.setMaxHealth(20);
            p.setInvulnerable(false);
        }
        Bukkit.getLogger().info("MBC enabled");
        Bukkit.broadcastMessage("Enable stat logging with /statlogs set true");
        Bukkit.broadcastMessage("If this is after a reset, do /gamenum now!");

        getServer().getPluginManager().registerEvents(MBC.getInstance(), this);
        getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, this);
        MBC.getInstance().lobby.createScoreboard();


        getCommand("mbchelp").setExecutor(new mbchelp());

        getCommand("changeTeam").setExecutor(new changeTeam());
        getCommand("changeTeam").setTabCompleter(new changeTeamTabCompletion());

        getCommand("playMusic").setExecutor(new playMusic());
        getCommand("playMusic").setTabCompleter(new playMusicTabCompletion());

        getCommand("ready").setExecutor(new ready());
        getCommand("notready").setExecutor(new notready());
        getCommand("readycheck").setExecutor(new readycheck());
        getCommand("startevent").setExecutor(new startevent());
        getCommand("announcement").setExecutor(new announcement());

        getCommand("start").setExecutor(new start());
        getCommand("start").setTabCompleter(new startTabCompletion());

        getCommand("pause").setExecutor(new pause());
        getCommand("minibeepstop").setExecutor(new minibeepstop());
        getCommand("unpause").setExecutor(new unpause());
        getCommand("gamenum").setExecutor(new gamenum());

        getCommand("ping").setExecutor(new ping());

        getCommand("checkbuild").setExecutor(new checkbuild());

        getCommand("placement").setExecutor(new placement());
        getCommand("individual").setExecutor(new individual());
        getCommand("scores").setExecutor(new scores());

        getCommand("playerscore").setExecutor(new playerscore());
        getCommand("playerscore").setTabCompleter(new playerscoreTabCompletion());
        getCommand("teamscore").setExecutor(new teamscore());
        getCommand("teamscore").setTabCompleter(new teamscoreTabCompletion());
        getCommand("endgame").setExecutor(new endgame());

        getCommand("statlogs").setExecutor(new statlogs());
        getCommand("statlogs").setTabCompleter(new statlogsTabCompleter());

        getCommand("spawnNPC").setExecutor(new spawnNPC());
        getCommand("despawnNPCS").setExecutor(new despawnNPCs());
        getCommand("invincible").setExecutor(new invincible());

        getCommand("removeSection").setExecutor(new removeSection());

        getCommand("scoring").setExecutor(new scoring());
        getCommand("scoring").setTabCompleter(new startTabCompletion());

        partygame pg = new partygame();
        getCommand("partygame").setExecutor(pg);
        getCommand("partygame").setTabCompleter(pg);

        getCommand("skip").setExecutor(new skip());

        getCommand("loadPlayers").setExecutor(new loadPlayers());

        // prevent crafting wooden axes (worldedit)
        Iterator<Recipe> it = getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe == null) continue;
            if (recipe.getResult().getType() == Material.WOODEN_AXE) {
                it.remove();
            } else if (recipe.getResult().getType() == Material.SNOW_BLOCK) {
                it.remove();
            } else if (recipe.getResult().getType() == Material.SHIELD) {
                it.remove();
            }
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("MBC disabled");
        for (@NotNull Iterator<KeyedBossBar> it = Bukkit.getBossBars(); it.hasNext(); ) {
            BossBar b = it.next();
            b.removeAll();
            b.setVisible(false);
        }

        for (Leaderboard leaderboards : Lobby.individualLeaderboards) {
            leaderboards.RemoveStands();
        }
    }
}
