package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.partygames.PartyGameFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class partygame implements CommandExecutor, TabCompleter {
    List<String> partyGames = Arrays.asList("DiscoFever", "BeepTest", "Dragons", "OneShot");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return partyGames;
        }

        return new ArrayList<>();
    }
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
            if (!partyGames.contains(args[0])) {
                sender.sendMessage("Please provide a valid game");
                return false;
            }

            for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
                p.getPlayer().setMaxHealth(20);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
                p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
                p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
                p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
                p.getPlayer().getInventory().clear();
                p.getPlayer().setExp(0);
                p.getPlayer().setLevel(0);
                p.getPlayer().setInvulnerable(true);
            }
            PartyGameFactory.getPartyGame(args[0]).start();
        }
        return true;
    }

}
