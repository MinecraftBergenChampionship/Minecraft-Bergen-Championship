package me.rspacerr.minecraftbruhchampionship.tgttosawap;

import me.rspacerr.minecraftbruhchampionship.MinecraftBruhChampionship;
import me.rspacerr.minecraftbruhchampionship.manager.GameManager;
import me.rspacerr.minecraftbruhchampionship.manager.GameState;

public class TGTTOSManager extends GameManager {

    public GameState gameState = GameState.LOBBY;


    public TGTTOSManager(MinecraftBruhChampionship plugin) {
        super(plugin);
    }

    @Override
    public void Start() {
        // Random Map Selection

    }
}
