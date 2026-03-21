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
                        case ("TGTTOS") -> {p.playSound(p, "igm.tgttos", SoundCategory.RECORDS, 1, 1);}
                        case ("SurvivalGames") -> {p.playSound(p, "igm.survival_games", SoundCategory.RECORDS, 1, 1);}
                        case ("Skybattle") -> {p.playSound(p, "igm.skybattle", SoundCategory.RECORDS, 1, 1);}
                        case ("BuildMart") -> {p.playSound(p, "igm.build_mart", SoundCategory.RECORDS, 1, 1);}
                        case ("Spleef") -> {p.playSound(p, "igm.spleef", SoundCategory.RECORDS, 1, 1);}
                        case ("AceRace") -> {p.playSound(p, "igm.ace_race", SoundCategory.RECORDS, 1, 1);}
                        case ("DecisionDome") -> {p.playSound(p, "igm.decision_dome", SoundCategory.RECORDS, 1, 1);}
                        case ("Quickfire") -> {p.playSound(p, "igm.quickfire", SoundCategory.RECORDS, 1, 1);}
                        case ("Party") -> {send.sendMessage("No valid music - please select a specific party game");}
                        case ("PowerTag") -> {p.playSound(p, "igm.power_tag", SoundCategory.RECORDS, 1, 1);}
                        case ("Lockdown") -> {p.playSound(p, "igm.lockdown", SoundCategory.RECORDS, 1, 1);}
                        case ("OneShot") -> {p.playSound(p, "igm.oneshot", SoundCategory.RECORDS, 1, 1);}
                        case ("BeepSwitch") -> {p.playSound(p, "igm.beep_switch", SoundCategory.RECORDS, .75f, 1);}
                        case ("DiscoFever") -> {p.playSound(p, "igm.disco_fever", SoundCategory.RECORDS, 1, 1);}
                        case ("Dragons") -> {p.playSound(p, "igm.dragons", SoundCategory.RECORDS, 1, 1);}
                        case ("Drain") -> {p.playSound(p, "igm.drain", SoundCategory.RECORDS, 1, 1);}
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
                    case ("TGTTOS") -> {p.playSound(p, "igm.tgttos", SoundCategory.RECORDS, 1, 1);}
                    case ("SurvivalGames") -> {p.playSound(p, "igm.survival_games", SoundCategory.RECORDS, 1, 1);}
                    case ("Skybattle") -> {p.playSound(p, "igm.skybattle", SoundCategory.RECORDS, 1, 1);}
                    case ("BuildMart") -> {p.playSound(p, "igm.build_mart", SoundCategory.RECORDS, 1, 1);}
                    case ("Spleef") -> {p.playSound(p, "igm.spleef", SoundCategory.RECORDS, 1, 1);}
                    case ("AceRace") -> {p.playSound(p, "igm.ace_race", SoundCategory.RECORDS, 1, 1);}
                    case ("DecisionDome") -> {p.playSound(p, "igm.decision_dome", SoundCategory.RECORDS, 1, 1);}
                    case ("Quickfire") -> {p.playSound(p, "igm.quickfire", SoundCategory.RECORDS, 1, 1);}
                    case ("Party") -> {send.sendMessage("No valid music - please select a specific party game");}
                    case ("PowerTag") -> {p.playSound(p, "igm.power_tag", SoundCategory.RECORDS, 1, 1);}
                    case ("Lockdown") -> {p.playSound(p, "igm.lockdown", SoundCategory.RECORDS, 1, 1);}
                    case ("OneShot") -> {p.playSound(p, "igm.oneshot", SoundCategory.RECORDS, 1, 1);}
                    case ("BeepSwitch") -> {p.playSound(p, "igm.beep_switch", SoundCategory.RECORDS, 1, 1);}
                    case ("DiscoFever") -> {p.playSound(p, "igm.disco_fever", SoundCategory.RECORDS, 1, 1);}
                    case ("Dragons") -> {p.playSound(p, "igm.dragons", SoundCategory.RECORDS, 1, 1);}
                    case ("Drain") -> {p.playSound(p, "igm.drain", SoundCategory.RECORDS, 1, 1);}
                }
            }
            
        }
        return true;
    }

}