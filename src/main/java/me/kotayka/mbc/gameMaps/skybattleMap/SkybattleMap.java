package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.gameMaps.MBCMap;
import me.kotayka.mbc.games.Skybattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;

// not sure if inheriting from Map is really necessary since theres only one commonality but just in case
public abstract class SkybattleMap extends MBCMap {
    protected final Skybattle SKYBATTLE;
    private Location CENTER;
    private int voidHeight;
    private float borderRadius;
    private float borderHeight;
    private double BORDER_SHRINK_AMOUNT;
    private double VERTICAL_BORDER_SHRINK_AMOUNT;

    public Location[] spawns;


    public SkybattleMap(Skybattle skb) {
        super(Bukkit.getWorld("Skybattle"));

        this.SKYBATTLE = skb;
    }

    public void loadWorld(Location center, int yMin, int yMax, float borderRadius, double shrinkRate, double fallRate) {
        this.CENTER = center;
        this.voidHeight = yMin;
        this.borderHeight = yMax;
        this.borderRadius = borderRadius;

        this.BORDER_SHRINK_AMOUNT = shrinkRate;
        this.VERTICAL_BORDER_SHRINK_AMOUNT = fallRate;
        getWorld().setSpawnLocation(center);
        resetMap();
    }

    /**
     * Expensive function called after each round and when loading the game;
     * Resets the map to a state copied from another area in the Skybattle World.
     * @implSpec should call removeEntities() and resetKillMaps()
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
        for (Entity witch : getWorld().getEntitiesByClass(Witch.class)) {
            witch.remove();
        }
        for (Entity pearl : getWorld().getEntitiesByClass(EnderPearl.class)) {
            pearl.remove();
        }
        for (Entity minecart : getWorld().getEntitiesByClass(Minecart.class)) {
            minecart.remove();
        }
        for (Entity boat : getWorld().getEntitiesByClass(Boat.class)) {
            boat.remove();
        }
    }

    /**
     * Spawns particles at values of border
     */
    public abstract void Border();

    /**
     * Uses array of spawns to spawn players with their team in a random spawn
     * This is abstract to allow for customization of what items players spawn with.
     * TODO: could maybe standardize something and then override if necessary but I'm kinda lazy
     */
    public abstract void spawnPlayers();

    /**
     * For overtime events, mainly border
     */
    public abstract void Overtime();

    /**
     * Remove barriers around spawn locations
     */
    public abstract void removeBarriers();

    public int getVoidHeight() { return voidHeight; }
    public Location getCenter() { return CENTER; }

    public float getBorderHeight() { return borderHeight; }
    public float getBorderRadius() { return borderRadius; }
    public void reduceBorderHeight(double n) { borderHeight -= n; }
    public void reduceBorderRadius(double n) { borderRadius -= n; if(borderRadius < 0) borderRadius = 0; }
    public void setBorderRadius(float n) { borderRadius = n; }
    public void setBorderHeight(float n) { borderHeight = n; }
    public double getBorderShrinkRate() { return BORDER_SHRINK_AMOUNT; }
    public double getVerticalBorderShrinkRate() { return VERTICAL_BORDER_SHRINK_AMOUNT; }
}
