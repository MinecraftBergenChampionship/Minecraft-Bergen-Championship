package me.rspacerr.minecraftbruhchampionship;

import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftBruhChampionship extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("If you saw this something must be going right!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabling MinecraftBruhChampionship");
    }
}
