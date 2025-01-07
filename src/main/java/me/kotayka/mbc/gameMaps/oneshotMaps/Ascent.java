package me.kotayka.mbc.gameMaps.oneshotMaps;

import me.kotayka.mbc.partygames.OneShot;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Ascent extends OneShotMap {
    public Ascent(OneShot oneshot) {
        super(oneshot);
        spawnpoints = new Location[50];
        addSpawnpoints();
        DEATH_Y = -20;
    }

    private void addSpawnpoints() {
        spawnpoints[0] = new Location(Bukkit.getWorld("Party"), -5064.987586, 3.000000, 5038.514596);
        spawnpoints[1] = new Location(Bukkit.getWorld("Party"), -5054.485625, 3.000000, 5039.943378);
        spawnpoints[2] = new Location(Bukkit.getWorld("Party"), -5063.388545, 3.000000, 5031.770454);
        spawnpoints[3] = new Location(Bukkit.getWorld("Party"), -5050.490402, 3.000000, 5025.408838);
        spawnpoints[4] = new Location(Bukkit.getWorld("Party"), -5067.236012, 2.000000, 5004.890227);
        spawnpoints[5] = new Location(Bukkit.getWorld("Party"), -5046.356975, 2.000000, 5005.313848);
        spawnpoints[6] = new Location(Bukkit.getWorld("Party"), -5027.696542, 2.000000, 4992.007647);
        spawnpoints[7] = new Location(Bukkit.getWorld("Party"), -5056.797167, 2.000000, 4989.603370);
        spawnpoints[8] = new Location(Bukkit.getWorld("Party"), -5066.333295, 0.000000, 4989.030950);
        spawnpoints[9] = new Location(Bukkit.getWorld("Party"), -5041.187928, 2.000000, 4995.612042);
        spawnpoints[10] = new Location(Bukkit.getWorld("Party"), -5039.219537, 2.000000, 4977.777945);
        spawnpoints[11] = new Location(Bukkit.getWorld("Party"), -5041.040296, 2.000000, 4962.430247);
        spawnpoints[12] = new Location(Bukkit.getWorld("Party"), -5023.382625, 2.000000, 4963.945564);
        spawnpoints[13] = new Location(Bukkit.getWorld("Party"), -5005.358742, 2.000000, 4958.966341);
        spawnpoints[14] = new Location(Bukkit.getWorld("Party"), -5007.353747, 0.000000, 4968.935796);
        spawnpoints[15] = new Location(Bukkit.getWorld("Party"), -5029.208574, 2.000000, 4940.271750);
        spawnpoints[16] = new Location(Bukkit.getWorld("Party"), -5014.866224, 0.000000, 4938.626785);
        spawnpoints[17] = new Location(Bukkit.getWorld("Party"), -5016.311946, 1.000000, 5047.928608);
        spawnpoints[18] = new Location(Bukkit.getWorld("Party"), -5009.566777, 1.000000, 5027.151281);
        spawnpoints[19] = new Location(Bukkit.getWorld("Party"), -4987.472724, 1.000000, 5022.419237);
        spawnpoints[20] = new Location(Bukkit.getWorld("Party"), -4974.094609, 1.000000, 5026.357974);
        spawnpoints[21] = new Location(Bukkit.getWorld("Party"), -4974.250196, -1.937500, 5020.485249);
        spawnpoints[22] = new Location(Bukkit.getWorld("Party"), -4955.687322, -2.000000, 5003.429942);
        spawnpoints[23] = new Location(Bukkit.getWorld("Party"), -4971.103982, -2.000000, 5010.079348);
        spawnpoints[24] = new Location(Bukkit.getWorld("Party"), -4937.968680, -2.000000, 5010.858271);
        spawnpoints[25] = new Location(Bukkit.getWorld("Party"), -4946.565212, -2.000000, 5016.956409);
        spawnpoints[26] = new Location(Bukkit.getWorld("Party"), -4922.433193, -1.000000, 5016.352865);
        spawnpoints[27] = new Location(Bukkit.getWorld("Party"), -4923.719898, -4.000000, 5023.617477);
        spawnpoints[28] = new Location(Bukkit.getWorld("Party"), -4915.169324, -4.000000, 5031.516242);
        spawnpoints[29] = new Location(Bukkit.getWorld("Party"), -4932.883181, -4.000000, 5041.057970);
        spawnpoints[30] = new Location(Bukkit.getWorld("Party"), -4943.137406, -4.000000, 5021.611409);
        spawnpoints[31] = new Location(Bukkit.getWorld("Party"), -4945.729514, -4.000000, 5036.372234);
        spawnpoints[32] = new Location(Bukkit.getWorld("Party"), -4963.872736, -2.000000, 5039.066556);
        spawnpoints[33] = new Location(Bukkit.getWorld("Party"), -4964.816262, -2.000000, 5052.735778);
        spawnpoints[34] = new Location(Bukkit.getWorld("Party"), -4996.240085, 1.000000, 5067.816426);
        spawnpoints[35] = new Location(Bukkit.getWorld("Party"), -4998.027466, 1.000000, 5048.687527);
        spawnpoints[36] = new Location(Bukkit.getWorld("Party"), -5016.311946, 1.000000, 5047.928608);
        spawnpoints[37] = new Location(Bukkit.getWorld("Party"), -5014.577486, 1.000000, 5065.480329);
        spawnpoints[38] = new Location(Bukkit.getWorld("Party"), -5023.333482, 1.000000, 5050.535210);
        spawnpoints[39] = new Location(Bukkit.getWorld("Party"), -5042.959246, 7.000000, 5050.832147);
        spawnpoints[40] = new Location(Bukkit.getWorld("Party"), -5034.064068, 6.000000, 5037.806697);
        spawnpoints[41] = new Location(Bukkit.getWorld("Party"), -5064.987586, 3.000000, 5038.514596);
        spawnpoints[42] = new Location(Bukkit.getWorld("Party"), -5054.485625, 3.000000, 5039.943378);
        spawnpoints[43] = new Location(Bukkit.getWorld("Party"), -5063.388545, 3.000000, 5031.770454);
        spawnpoints[44] = new Location(Bukkit.getWorld("Party"), -5050.490402, 3.000000, 5025.408838);
        spawnpoints[45] = new Location(Bukkit.getWorld("Party"), -5067.236012, 2.000000, 5004.890227);
        spawnpoints[46] = new Location(Bukkit.getWorld("Party"), -5046.356975, 2.000000, 5005.313848);
        spawnpoints[47] = new Location(Bukkit.getWorld("Party"), -5027.696542, 2.000000, 4992.007647);
        spawnpoints[48] = new Location(Bukkit.getWorld("Party"), -5056.797167, 2.000000, 4989.603370);
        spawnpoints[49] = new Location(Bukkit.getWorld("Party"), -5066.333295, 0.000000, 4989.030950);
    }
}
