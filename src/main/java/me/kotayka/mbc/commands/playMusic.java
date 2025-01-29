package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class playMusic implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player send = (Player) sender;
            if (!(send.isOp())) {
                send.sendMessage("You do not have permission to execute this command!");
                return true;
            }

            if (args.length > 2 || args.length < 1) {
                send.sendMessage("Please provide 1 or 2 arguments");
                return false;
            }
            String game = args[0];
            if (!MBC.gameNameList.contains(args[0]) && !MBC.partyGameNameList.contains(args[0]) ) {
                send.sendMessage("Please provide a valid game");
            }

            if (args.length == 1) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    switch(args[0]){
                        case ("TGTTOS") -> {p.playSound(p, Sound.MUSIC_DISC_OTHERSIDE, SoundCategory.RECORDS, 1, 1);}
                        case ("SurvivalGames") -> {p.playSound(p, Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS, 1, 1);}
                        case ("Skybattle") -> {p.playSound(p, Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS, 1, 1);}
                        case ("BuildMart") -> {p.playSound(p, Sound.MUSIC_DISC_MALL, SoundCategory.RECORDS, 1, 1);}
                        case ("Spleef") -> {p.playSound(p, Sound.MUSIC_DISC_PIGSTEP, SoundCategory.RECORDS, 1, 1);}
                        case ("AceRace") -> {p.playSound(p, Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 1, 1);}
                        case ("DecisionDome") -> {p.playSound(p, Sound.MUSIC_DISC_CAT, SoundCategory.RECORDS, 1, 1);}
                        case ("Quickfire") -> {p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);}
                        case ("Party") -> {send.sendMessage("No valid music - please select a specific party game");}
                        case ("PowerTag") -> {p.playSound(p, Sound.MUSIC_DISC_CREATOR, SoundCategory.RECORDS, 1, 1);}
                        case ("OneShot") -> {p.playSound(p, Sound.MUSIC_DISC_BLOCKS, SoundCategory.RECORDS, 1, 1);}
                        case ("BeepTest") -> {p.playSound(p, Sound.MUSIC_DISC_13, SoundCategory.RECORDS, .75f, 1);}
                        case ("DiscoFever") -> {p.playSound(p, Sound.MUSIC_DISC_MELLOHI, SoundCategory.RECORDS, 1, 1);}
                        case ("Dragons") -> {p.playSound(p, Sound.MUSIC_DISC_RELIC, SoundCategory.RECORDS, 1, 1);}
                    }
                }
            }
            else if (args.length == 2) {
                Participant part = Participant.getParticipant(args[1]);
                if (part == null) {
                    send.sendMessage(ChatColor.RED + "Please Provide a valid player name");
                    return false;
                }
                Player p = part.getPlayer();
                switch(args[0]){
                    case ("TGTTOS") -> {p.playSound(p, Sound.MUSIC_DISC_OTHERSIDE, SoundCategory.RECORDS, 1, 1);}
                    case ("SurvivalGames") -> {p.playSound(p, Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS, 1, 1);}
                    case ("Skybattle") -> {p.playSound(p, Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS, 1, 1);}
                    case ("BuildMart") -> {p.playSound(p, Sound.MUSIC_DISC_MALL, SoundCategory.RECORDS, 1, 1);}
                    case ("Spleef") -> {p.playSound(p, Sound.MUSIC_DISC_PIGSTEP, SoundCategory.RECORDS, 1, 1);}
                    case ("AceRace") -> {p.playSound(p, Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 1, 1);}
                    case ("DecisionDome") -> {p.playSound(p, Sound.MUSIC_DISC_CAT, SoundCategory.RECORDS, 1, 1);}
                    case ("Quickfire") -> {p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);}
                    case ("Party") -> {send.sendMessage("No valid music - please select a specific party game");}
                    case ("PowerTag") -> {p.playSound(p, Sound.MUSIC_DISC_CREATOR, SoundCategory.RECORDS, 1, 1);}
                    case ("OneShot") -> {p.playSound(p, Sound.MUSIC_DISC_BLOCKS, SoundCategory.RECORDS, 1, 1);}
                    case ("BeepTest") -> {p.playSound(p, Sound.MUSIC_DISC_13, SoundCategory.RECORDS, 1, 1);}
                    case ("DiscoFever") -> {p.playSound(p, Sound.MUSIC_DISC_MELLOHI, SoundCategory.RECORDS, 1, 1);}
                    case ("Dragons") -> {p.playSound(p, Sound.MUSIC_DISC_RELIC, SoundCategory.RECORDS, 1, 1);}
                }
            }
            
        }
        return true;
    }

}