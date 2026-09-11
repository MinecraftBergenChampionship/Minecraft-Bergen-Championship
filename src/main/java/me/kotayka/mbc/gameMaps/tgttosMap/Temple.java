package me.kotayka.mbc.gameMaps.tgttosMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Temple extends TGTTOSMap {
    public Temple() {
        super("Temple", "bigkirbypuff_", new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS)});
        super.loadMap(
            new Location[]{new Location(getWorld(), -505, 75, -487, 90, 0), new Location(getWorld(), -505, 75, -492, 90, 0), new Location(getWorld(), -505, 75, -497, 90, 0), new Location(getWorld(), -505, 75, -502, 90, 0), new Location(getWorld(), -505, 75, -507, 90, 0), new Location(getWorld(), -505, 75, -512, 90, 0)}, 
            new Location[]{new Location(getWorld(), -657, 62, -494), new Location(getWorld(), -657, 62, -505)},
               0
        );
        randomDoorsGenerate();
    }

    /**
     * Set air or barriers at the start of each tgttos round.
     * @param barriers TRUE = Barriers, FALSE = Air
     */
    @Override
    public void Barriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 75; y <= 80; y++) {
            for (int z = -515; z <= -485; z++) {
                getWorld().getBlockAt(-508, y, z).setType(block);
            }
        }
    }

    // generates four random numbers such that doors in temple map open randomly (3 open for first crossroad, then 2, then 1)
    public void randomDoorsGenerate(){
        int doorOneFailure = (int)(Math.random()*4);
        int doorThreeSuccess = (int)(Math.random()*4);

        int doorTwoFirstSuccess = (int)(Math.random()*4);
        int doorTwoSecondSuccess = (int)(Math.random()*4);
        while (doorTwoSecondSuccess == doorTwoFirstSuccess) {
            doorTwoSecondSuccess = (int)(Math.random()*4);
        }

        doorOne(doorOneFailure);
        doorTwo(doorTwoFirstSuccess, doorTwoSecondSuccess);
        doorThree(doorThreeSuccess);
    }

    // takes randomly generated int i from 0 to 3. based on number, allows 3 doors to open and door i+1 not to.
    public void doorOne(int i) {
        getWorld().getBlockAt(-526, 73, -492).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-526, 73, -499).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-526, 73, -506).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-526, 73, -513).setType(Material.POLISHED_GRANITE);

        getWorld().getBlockAt(-523, 69, -491).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-523, 69, -498).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-523, 69, -505).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-523, 69, -512).setType(Material.GOLD_BLOCK);


        switch (i) {
            case 0:
                getWorld().getBlockAt(-526, 73, -492).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-523, 69, -491).setType(Material.OAK_WOOD);
                break;
            case 1:
                getWorld().getBlockAt(-526, 73, -499).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-523, 69, -498).setType(Material.OAK_WOOD);
                break;
            case 2:
                getWorld().getBlockAt(-526, 73, -506).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-523, 69, -505).setType(Material.OAK_WOOD);
                break;
            case 3:
            default:
                getWorld().getBlockAt(-526, 73, -513).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-523, 69, -512).setType(Material.OAK_WOOD);
                break;
        }
    }

    // takes randomly generated int i and int j from 0 to 3. based on number, allows 2 doors to open and door i+1, j+1 not to.
    public void doorTwo(int i, int j) {
        getWorld().getBlockAt(-556, 73, -492).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-556, 73, -499).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-556, 73, -506).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-556, 73, -513).setType(Material.POLISHED_GRANITE);

        getWorld().getBlockAt(-553, 69, -491).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-553, 69, -498).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-553, 69, -505).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-553, 69, -512).setType(Material.GOLD_BLOCK);


        switch (i) {
            case 0:
                getWorld().getBlockAt(-556, 73, -492).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -491).setType(Material.OAK_WOOD);
                break;
            case 1:
                getWorld().getBlockAt(-556, 73, -499).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -498).setType(Material.OAK_WOOD);
                break;
            case 2:
                getWorld().getBlockAt(-556, 73, -506).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -505).setType(Material.OAK_WOOD);
                break;
            case 3:
            default:
                getWorld().getBlockAt(-556, 73, -513).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -512).setType(Material.OAK_WOOD);
                break;
        }

        switch (j) {
            case 0:
                getWorld().getBlockAt(-556, 73, -492).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -491).setType(Material.OAK_WOOD);
                break;
            case 1:
                getWorld().getBlockAt(-556, 73, -499).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -498).setType(Material.OAK_WOOD);
                break;
            case 2:
                getWorld().getBlockAt(-556, 73, -506).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -505).setType(Material.OAK_WOOD);
                break;
            case 3:
            default:
                getWorld().getBlockAt(-556, 73, -513).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-553, 69, -512).setType(Material.OAK_WOOD);
                break;
        }
    }

    // takes randomly generated int i from 0 to 3. based on number, allows 1 door to open and door i+1 will be that door.
    public void doorThree(int i) {
        getWorld().getBlockAt(-586, 73, -492).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-586, 73, -499).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-586, 73, -506).setType(Material.POLISHED_GRANITE);
        getWorld().getBlockAt(-586, 73, -513).setType(Material.POLISHED_GRANITE);

        getWorld().getBlockAt(-583, 69, -491).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-583, 69, -498).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-583, 69, -505).setType(Material.GOLD_BLOCK);
        getWorld().getBlockAt(-583, 69, -512).setType(Material.GOLD_BLOCK);
        
        switch (i) {
            case 0:
                getWorld().getBlockAt(-586, 73, -499).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -506).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -513).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-583, 69, -498).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -505).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -512).setType(Material.OAK_WOOD);
                break;
            case 1:
                getWorld().getBlockAt(-586, 73, -492).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -506).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -513).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-583, 69, -491).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -505).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -512).setType(Material.OAK_WOOD);
                break;
            case 2:
                getWorld().getBlockAt(-586, 73, -492).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -499).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -513).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-583, 69, -491).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -498).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -512).setType(Material.OAK_WOOD);
                break;
            case 3:
            default:
                getWorld().getBlockAt(-586, 73, -492).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -499).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-586, 73, -506).setType(Material.REDSTONE_BLOCK);
                getWorld().getBlockAt(-583, 69, -491).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -498).setType(Material.OAK_WOOD);
                getWorld().getBlockAt(-583, 69, -505).setType(Material.OAK_WOOD);
                break;
        }
    }
}
