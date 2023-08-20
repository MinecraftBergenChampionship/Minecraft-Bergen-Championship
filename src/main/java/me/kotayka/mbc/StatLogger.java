package me.kotayka.mbc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
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
        // log stats
        write(ChatColor.BOLD+"Team (Multiplied) Scores: "+ChatColor.RESET+"\n");
        for (String s : teamScores) {
            write(s+ChatColor.RESET);
        }

        write(ChatColor.BOLD+"Individual Scores: " + ChatColor.RESET+"\n");
        for (String s : individual) {
            write(s+ChatColor.RESET);
        }

        // transcript
        write("\n"+ChatColor.BOLD+"Transcript:"+ChatColor.RESET);
        for (String s : transcript) {
            write(s+ChatColor.RESET+"\n");
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

    private void write(String s) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file.getName());
            byte[] bytes = s.getBytes();
            outputStream.write(bytes);
        } catch (IOException e) {
            error();
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch(IOException e) {
                    System.err.print(e.getMessage());
                    e.printStackTrace();
                }
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
            Bukkit.broadcastMessage("directory == " + directory);
            File file = new File(directory, GAME.gameName);
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
