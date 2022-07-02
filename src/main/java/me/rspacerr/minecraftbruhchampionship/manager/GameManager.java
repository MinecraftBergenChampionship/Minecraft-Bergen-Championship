package me.rspacerr.minecraftbruhchampionship.manager;

import me.rspacerr.minecraftbruhchampionship.MinecraftBruhChampionship;

public class GameManager {
    private final MinecraftBruhChampionship plugin;
    public GameState gameState = GameState.LOBBY;

    public GameManager(MinecraftBruhChampionship plugin) {
        this.plugin = plugin;
    }

    public void Start() {
        // Start Game
    }
}
