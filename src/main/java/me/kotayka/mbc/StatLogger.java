package me.kotayka.mbc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatLogger {
    private final Minigame GAME;
    private final String directory;
    private File file;
    private List<String> transcript = new ArrayList<>();
    private List<String> individual = new ArrayList<>();
    private List<String> teamScores = new ArrayList<>();

    public StatLogger(Minigame game) {
        this.GAME = game;
        directory = MBC.statDirectory();
        file = createFile();
    }

    public void logIndividual(String s) {
        individual.add(s);
    }

    public void logTeamScores(String s) {
        teamScores.add(s);
    }

    public void logStats() {
        if (!MBC.getInstance().logStats()) {
            return;
        }

        // log stats
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(ChatColor.BOLD+"Team (Multiplied) Scores: " + ChatColor.RESET+"\n");
            for (String s : teamScores) {
                writer.write(s+ChatColor.RESET);
            }

            writer.write(ChatColor.BOLD+"Individual Scores: " + ChatColor.RESET+"\n");
            for (String s : individual) {
                writer.write(s+ChatColor.RESET);
            }

            // transcript
            writer.write("\n"+ChatColor.BOLD+"Transcript:"+ChatColor.RESET);
            for (String s : transcript) {
                writer.write(s+ChatColor.RESET+"\n");
            }
            writer.close();
        } catch (IOException e) {
            error();
            e.printStackTrace();
        }

        teamScores.clear();
        individual.clear();
        transcript.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(ChatColor.GREEN+"Successfully logged stats for " + GAME.gameName+ ".");
            }
        }
    }

    public void log(String s) {
        if (MBC.getInstance().logStats()) {
            transcript.add(s);
        }
    }

    private File createFile() {
        try {
            File file = new File(directory, GAME.gameName+".txt");
            file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                return file;
            } else {
                error();
                return null;
            }
        } catch (IOException e) {
            error();
            e.printStackTrace();
            return null;
        }
    }

    private void error() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(ChatColor.RED+"An error occurred while logging stats!");
            }
        }
    }
}
