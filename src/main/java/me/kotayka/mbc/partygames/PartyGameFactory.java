package me.kotayka.mbc.partygames;

import me.kotayka.mbc.PartyGame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyGameFactory {
    private static List<String> gameNames = Arrays.asList("Disco Fever", "Beep Test", "Knockout");
    private static Map<String, PartyGame> games = new HashMap<>();

    private PartyGameFactory() {}

    public static PartyGame getPartyGame(String name) {
        if (!(gameNames.contains(name))) {
            return null;
        }

        if (games.containsKey(name)) {
            return games.get(name);
        } else {
            switch (name) {
                case "Disco Fever":
                    games.put("Disco Fever", DiscoFever.getInstance());
                    break;
                default:
                    return null;
            }
        }
        return null;
    }
}
