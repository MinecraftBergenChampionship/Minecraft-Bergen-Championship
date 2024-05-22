package me.kotayka.mbc.partygames;

import me.kotayka.mbc.PartyGame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class PartyGameFactory {
    private static List<String> gameNames = Arrays.asList("DiscoFever", "BeepTest", "DiscoFever");
    private static Map<String, PartyGame> games = new HashMap<>();

    public PartyGameFactory() {}

    public static PartyGame getPartyGame(String name) {
        if (!(gameNames.contains(name))) {
            return null;
        }

        if (games.containsKey(name)) {
            return games.get(name);
        } else {
            switch (name) {
                case "DiscoFever":
                    games.put(name, DiscoFever.getInstance());
                    break;
                case "BeepTest":
                    games.put(name, BeepTest.getInstance());
                    break;
                default:
                    return null;
            }
        }
        return games.get(name);
    }

    public String getRandomGame() {
        return gameNames.get((int)(Math.random()*gameNames.size()));
    }
    
}
