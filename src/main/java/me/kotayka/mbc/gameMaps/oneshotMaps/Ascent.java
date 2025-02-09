package me.kotayka.mbc.gameMaps.oneshotMaps;

import me.kotayka.mbc.partygames.OneShot;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Ascent extends OneShotMap {
    public Ascent(OneShot oneshot) {
        super(oneshot);
        spawnpoints = new Location[48];
        addSpawnpoints();
        DEATH_Y = -20;
    }

    private void addSpawnpoints() {
        //attackers spawn
        spawnpoints[0] = new Location(Bukkit.getWorld("Party"), -4984, 0, 4918);
        spawnpoints[1] = new Location(Bukkit.getWorld("Party"), -4962, -2, 4928);
        spawnpoints[2] = new Location(Bukkit.getWorld("Party"), -5002, 0, 4945);
        spawnpoints[3] = new Location(Bukkit.getWorld("Party"), -5031, 2, 4933);
        
        //attackers a
        spawnpoints[4] = new Location(Bukkit.getWorld("Party"), -5046, 2, 4962);
        spawnpoints[5] = new Location(Bukkit.getWorld("Party"), -5039, 2, 4978);

        //attackers mid
        spawnpoints[6] = new Location(Bukkit.getWorld("Party"), -5007, 2, 4959);
        spawnpoints[7] = new Location(Bukkit.getWorld("Party"), -4984, 0, 4983);

        //attackers b
        spawnpoints[8] = new Location(Bukkit.getWorld("Party"), -4938, -2, 4971);
        spawnpoints[9] = new Location(Bukkit.getWorld("Party"), -4950, -2, 4986);

        //defenders spawn
        spawnpoints[10] = new Location(Bukkit.getWorld("Party"), -4996, 1, 5070);
        spawnpoints[11] = new Location(Bukkit.getWorld("Party"), -5017, 1, 5059);
        spawnpoints[12] = new Location(Bukkit.getWorld("Party"), -4984, 1, 5047);
        spawnpoints[13] = new Location(Bukkit.getWorld("Party"), -5025, 1, 5048);
        
        //defenders a
        spawnpoints[14] = new Location(Bukkit.getWorld("Party"), -5043, 7, 5052);
        spawnpoints[15] = new Location(Bukkit.getWorld("Party"), -5038, 6, 5035);
        spawnpoints[16] = new Location(Bukkit.getWorld("Party"), -5039, 3, 5019);
        spawnpoints[17] = new Location(Bukkit.getWorld("Party"), -5026, 3, 5030);

        //defenders mid
        spawnpoints[18] = new Location(Bukkit.getWorld("Party"), -5011, 1, 5026);
        spawnpoints[19] = new Location(Bukkit.getWorld("Party"), -4998, 1, 5042);
        spawnpoints[20] = new Location(Bukkit.getWorld("Party"), -4991, 0, 5009);
        spawnpoints[21] = new Location(Bukkit.getWorld("Party"), -5009, 0, 5003);

        //defenders b
        spawnpoints[22] = new Location(Bukkit.getWorld("Party"), -4986, 1, 5020);
        spawnpoints[23] = new Location(Bukkit.getWorld("Party"), -4972, -2, 5046);
        spawnpoints[24] = new Location(Bukkit.getWorld("Party"), -4976, 1, 5033);
        spawnpoints[25] = new Location(Bukkit.getWorld("Party"), -4959, -2, 5052);

        //a main
        spawnpoints[26] = new Location(Bukkit.getWorld("Party"), -5068, 0, 4989);
        spawnpoints[27] = new Location(Bukkit.getWorld("Party"), -5050, 2, 4988);
        spawnpoints[28] = new Location(Bukkit.getWorld("Party"), -5043, 2, 4994);

        //b main
        spawnpoints[29] = new Location(Bukkit.getWorld("Party"), -4938, -2, 5010);
        spawnpoints[30] = new Location(Bukkit.getWorld("Party"), -4956, -2, 5002);
        spawnpoints[31] = new Location(Bukkit.getWorld("Party"), -4970, -2, 5009);

        //a site and tree
        spawnpoints[32] = new Location(Bukkit.getWorld("Party"), -5056, 3, 5040);
        spawnpoints[33] = new Location(Bukkit.getWorld("Party"), -5070, 7, 5041);
        spawnpoints[34] = new Location(Bukkit.getWorld("Party"), -5051, 3, 5026);
        spawnpoints[35] = new Location(Bukkit.getWorld("Party"), -5042, 2, 5010);
        spawnpoints[36] = new Location(Bukkit.getWorld("Party"), -5018, 2, 4995);
        spawnpoints[37] = new Location(Bukkit.getWorld("Party"), -5027, 2, 4990);

        //b site
        spawnpoints[38] = new Location(Bukkit.getWorld("Party"), -4976, -2, 5020);
        spawnpoints[39] = new Location(Bukkit.getWorld("Party"), -4953, -2, 5034);
        spawnpoints[40] = new Location(Bukkit.getWorld("Party"), -4943, -4, 5022);
        spawnpoints[41] = new Location(Bukkit.getWorld("Party"), -4924, -1, 5016);
        spawnpoints[42] = new Location(Bukkit.getWorld("Party"), -4933, -4, 5040);
        spawnpoints[43] = new Location(Bukkit.getWorld("Party"), -4914, -4, 5037);

        //mid
        spawnpoints[44] = new Location(Bukkit.getWorld("Party"), -5008, 0, 4969);
        spawnpoints[45] = new Location(Bukkit.getWorld("Party"), -4991, 0, 4981);
        spawnpoints[46] = new Location(Bukkit.getWorld("Party"), -4991, 0, 4997);
        spawnpoints[47] = new Location(Bukkit.getWorld("Party"), -5008, 0, 4998);
        
        
    }
}
