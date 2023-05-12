package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.gameMaps.Map;
import me.kotayka.mbc.games.Skybattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;

// not sure if inheriting from Map is really necessary since theres only one commonality but just in case
public abstract class SkybattleMap extends Map {
    protected final Skybattle SKYBATTLE;
    private Location center;
    private int voidHeight;
    public int borderHeight;
    public Location[] spawns;


    public SkybattleMap(Skybattle skb) {
        super(Bukkit.getWorld("Skybattle"));

        this.SKYBATTLE = skb;
    }

    public void loadWorld(Location center, int yMin, int yMax) {
        this.center = center;
        this.voidHeight = yMin;
        this.borderHeight = yMax;

        resetMap();
    }

    /**
     * "Expensive" function called after each round and when loading the game;
     * Resets the map to a state copied from another area in the Skybattle World.
     * @implSpec should call removeEntities();
     */
    public abstract void resetMap();

    /**
     * Remove all extraneous entities lingering in the map when resetting.
     * There is almost certainly a better way to do this but personally,
     *
     * @see SkybattleMap resetMap()
     */
    public void removeEntities() {
        // Clear all floor items, primed tnt, creepers, pearls, minecarts ??? (you never know)
        // ... not gonna add boat removal until somebody actually does it. wood shouldn't be in maps unless shield crafting is disabled anyway
        for (Item item : getWorld().getEntitiesByClass(Item.class)) {
            item.remove();
        }
        for (Entity tnt : getWorld().getEntitiesByClass(TNTPrimed.class)) {
            tnt.remove();
        }
        for (Entity creeper : getWorld().getEntitiesByClass(Creeper.class)) {
            creeper.remove();
        }
        for (Entity pearl : getWorld().getEntitiesByClass(EnderPearl.class)) {
            pearl.remove();
        }
        for (Entity minecart : getWorld().getEntitiesByClass(Minecart.class)) {
            minecart.remove();
        }
    }

    /**
     * Uses array of spawns to spawn players with their team in a random spawn
     */
    public abstract void spawnPlayers();

    /**
     * Remove barriers around spawn locations
     */
    public abstract void removeBarriers();
}
