package com.kotayka.mcc.fullGame;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class Instructions {
    String[] tgttosInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    String[] bsabmInstructions = {
            "Build Mart is a game where you are trying to recreate each build as fast as possible by gathering materials from the islands outside your base.",
            "Inside your base (the nether portal) you’ll find three builds and an empty square next to each build.",
            "Use the fan, the boost pads and your elytra to fly around to each island to gather resources. All the resources you need will be somewhere on these islands.",
            "Once you have all the resources, come back to your base and recreate the build as fast as you can. For some builds, you may need to use the crafting table or the furnace in your base.",
            "If you don’t know whats wrong with a build, do /checkbuild inside the builds plot to find out what is wrong with it.",
            "You earn points through completing builds faster than other teams.",
            "As soon as the game starts, youll be instantly teleported to the center of the map, so be ready to check your builds."
    };

    String[] aceRaceInstructions = {
            "Ace Race is a game where your objective is to complete 3 laps as fast as you can.",
            "You’ll see boost pads across the map which do different things; if you don’t know what each one does now, you’ll find out in about a minute.",
            "You spawn in with a riptide trident, so if you’re in water, just hold right click and let go to go flying!",
            "If you fall into the void or into lava, don’t worry; youll be transported to the start of the island that you fell on.",
            "If you’ve never practiced the course before, your first lap will probably suck.",
            "You earn points through completing laps and completing the course as quick as you can, and youll earn points if your team is the first full team to complete the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run (and make sure you go the right way)! "
    };

    String[] sgInstructions = {
            "Anyone remember this place?",
            "Survival Games is a game all about kills.",
            "You spawn in with nothing, and you have to gain items through chests scattered across the map.",
            "You’ll find other things across the map as well, like enchanting tables and supply drops, but watch the border coming behind you.",
            "Watch the scoreboard to see when the next events happen, like grace period, supply drops, and chest refill.",
            "The best way to get points is through kills and through winning, but you do gain a few points through outliving other players.",
            "As soon as the game starts, youll be instantly teleported to the map, so be ready to loot! "
    };

    String[] skyBattleInstructions = {
            "Skybattle is a game similar to sky wars which is all about kills.",
            "Make your way to the middle of the map to avoid the border behind you, and make sure to watch the border above you as well.",
            "You spawn in with an iron chestplate, a stone sword, and an iron pickaxe, along with infinite blocks and a few pieces of steak.",
            "Items will be at each island you go to, whether it be iron, bows, or ender pearls; make sure to use these items to the fullest to kill other players.",
            "One island lies in the middle with the best loot in the game, but its a very dangerous island to stay at.",
            "The best way to get points is through kills and through winning, but you do gain a few points through outliving other players.",
            "As soon as the game starts, youll be instantly teleported to the map, so be ready to loot!"
    };

    String[] paintdownInstructions = {
            "This game kinda reminds me of a birthday party I went to in sixth grade.",
            "Paintdown is a game where you are trying to eliminate the other players with your paintball gun while also collecting coin crates across the map.",
            "Shoot someone twice with your paintball gun to freeze them. While frozen, they cant move or use any items. Once an entire team is frozen, they are eliminated.",
            "If one of your teammates is frozen, splash your potion on them to unfreeze them. Be careful though, the potion has a cooldown.",
            "Coin crates are scattered across the map. Mining them will earn you a small amount of coins. Some rooms will have a large amount of coin crates in the center of the room.",
            "The border slowly shrinks towards the very center, where a large amount of coin crates lies.",
            "You earn points mainly through kills and winning, although outliving other teams and mining coin crates also gives you coins."
    };

    Map<String, Location> spawnCoords = new HashMap<>();
    Map<String, String[]> instructions = new HashMap<>();
    private final MCC mcc;
    private final Game game;

    String stage;

    public final int[] taskId = {-1};
    int timer = 0;

    public Instructions(MCC mcc, Game game) {
        this.mcc = mcc;
        this.game = game;
    }

    public void loadCoords() {
        spawnCoords.put("TGTTOS", new Location(mcc.tgttos.world, 688, 77, 215));
        spawnCoords.put("AceRace", new Location(mcc.aceRace.world, 2, 45, 110));
        spawnCoords.put("SG", new Location(mcc.sg.world, -57, 48, 63));
        spawnCoords.put("BSABM", new Location(mcc.bsabm.world, 0, 161, 0));
        spawnCoords.put("Skybattle", new Location(mcc.skybattle.world, -156, 51, -265));
        spawnCoords.put("Paintdown", new Location(mcc.tgttos.world, 62, 32, 64));

        instructions.put("TGTTOS", tgttosInstructions);
        instructions.put("AceRace", aceRaceInstructions);
        instructions.put("SG", sgInstructions);
        instructions.put("BSABM", bsabmInstructions);
        instructions.put("Skybattle", skyBattleInstructions);
        instructions.put("Paintdown", paintdownInstructions);
    }

    public void timerEnds() {
        game.changeToActGame(stage);
    }

    public void starting(String game) {
        stage = game;
        if (taskId[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
        }
        timer = 0;
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.teleport(spawnCoords.get(game));
            mcc.scoreboardManager.createInstructionScoreboard(p);
        }
        mcc.scoreboardManager.startTimerForGame(60, "game");
        taskId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(mcc.plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.WHITE+""+ChatColor.BOLD+"["+ChatColor.GOLD+"INSTRUCTION"+ChatColor.WHITE+"] "+ChatColor.RESET+ChatColor.WHITE+instructions.get(game)[timer]);
                if (timer >= 7) {
                    Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
                }
                timer++;
            }
        }, 100, 100);
    }
}
