package me.kotayka.mbc.partygames;

import me.kotayka.mbc.PartyGame;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyGameFactory {
    private static List<String> gameNames = Arrays.asList("DiscoFever", "BeepTest", "Dragons", "OneShot");
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
                case "Dragons":
                    games.put(name, Dragons.getInstance());
                    break;
                case "OneShot":
                    games.put(name, OneShot.getInstance());
                    break;
                default:
                    return null;
            }
        }
        return games.get(name);
    }

}
