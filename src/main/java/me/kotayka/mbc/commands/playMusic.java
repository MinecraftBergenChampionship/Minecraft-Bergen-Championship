package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;

import org.bukkit.Bukkit;
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
            if (!(sender.isOp())) {
                sender.sendMessage("You do not have permission to execute this command!");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("Please provide 1 argument");
                return false;
            }
            if (!MBC.gameNameList.contains(args[0]) && !MBC.partyGameNameList.contains(args[0]) ) {
                sender.sendMessage("Please provide a valid game");
            }

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
                    case ("Party") -> {sender.sendMessage("No valid music - please select a specific party game");}
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