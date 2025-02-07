package me.kotayka.mbc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.*;

public class Leaderboard {
    public Map<Participant, Integer> scores = new HashMap<>();
    private List<Participant> participants;
    private List<ArmorStand> leaderboardStands = new ArrayList<>();
    private static Location[] INDIVIDUAL_LEADERBOARDS = new Location[]{
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 51.5),
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 75.5),
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 71.5),
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 67.5),
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 63.5),
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 59.5),
            new Location(MBC.getInstance().lobby.world, -3.5, -1, 55.5),
    };

    private int index;

    public Leaderboard(List<Participant> participants, int index) {
        this.participants = participants;

        for (Participant p : participants) {
            scores.put(p, p.getRawTotalScore());
        }

        this.index = index;
    }

    public Leaderboard(List<Participant> participants, Leaderboard lastBoard, int index) {
        this.participants = participants;

        for (Participant p : participants) {
            scores.put(p, p.getRawTotalScore()-lastBoard.scores.get(p));
        }

        this.index = index;
    }

    public void spawnLeaderboard() {
        //Bukkit.broadcastMessage("Spawning Leaderboard");
        List<String> leaderboardLines = new ArrayList<>();

        leaderboardLines.add(ChatColor.AQUA + "Individual Leaderboard");
        //Bukkit.broadcastMessage(""+participants.size());
        for (int i = 0; i < 8; i++) {
            Participant p;

            if (i < participants.size()) {
                p = participants.get(i);
            } else {
                //Bukkit.broadcastMessage("Returning: "+i);
                break;
            }
            leaderboardLines.add(""+(i+1)+". "+p.getFormattedName()+" - "+scores.get(p));
            //Bukkit.broadcastMessage(leaderboardLines.getLast());
        }

        Location loc = INDIVIDUAL_LEADERBOARDS[index];

        double yOffset = 0;
        for (String line : leaderboardLines) {
            //Bukkit.broadcastMessage(line);
            spawnFloatingText(loc.clone().add(0, yOffset, 0), line);
            yOffset -= 0.3;
        }
    }

    private void spawnFloatingText(Location location, String text) {
        // Spawn an invisible armor stand with custom name
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);

        leaderboardStands.add(armorStand);
    }

    public void RemoveStands() {
        for (ArmorStand a : leaderboardStands) {
            a.remove();
        }
    }
}
