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
    private boolean stupid = false;
    private List<String> transcript = new ArrayList<>();
    private List<String> individual = new ArrayList<>();
    private List<String> teamScores = new ArrayList<>();

    public StatLogger(Minigame game) {
        this.GAME = game;
        directory = MBC.getInstance().statDirectory();
        file = createFile();
    }

    public void logIndividual(String s) {
        if (MBC.getInstance().logStats()) {
            individual.add(s);
        }
    }

    public void logTeamScores(String s) {
        if (MBC.getInstance().logStats()) {
            teamScores.add(s);
        }
    }

    public void logStats() {
        if (!MBC.getInstance().logStats() || stupid) {
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp()) p.sendMessage(ChatColor.RED+"ERROR: IOException while logging stats!");
            }
            e.printStackTrace();
        }

        teamScores.clear();
        individual.clear();
        transcript.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(ChatColor.GREEN+"Successfully logged stats for " + GAME.name() + ".");
            }
        }
    }

    public void log(String s) {
        if (MBC.getInstance().logStats()) {
            transcript.add(s);
        }
    }

    private File createFile() {
        if (!MBC.getInstance().logStats()) return null;
        try {
            File file = new File(directory, GAME.name() +".txt");
            //Bukkit.broadcastMessage(file.getAbsolutePath());
            file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                return file;
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.isOp()) {
                        p.sendMessage(MBC.ADMIN_PREFIX + ChatColor.RED + "ERROR: File already exists!");
                        stupid = true;
                    }
                }
                return null;
            }
        } catch (IOException e) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp())
                    p.sendMessage(ChatColor.RED+"ERROR: IOException while creating files!");
            }
            e.printStackTrace();
            return null;
        }
    }
}
