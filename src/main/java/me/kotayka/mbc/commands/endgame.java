package me.kotayka.mbc.commands;

import me.kotayka.mbc.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class endgame implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;

        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "This command is admin restricted!");
            return false;
        }

        if (!(MBC.getInstance().getMinigame() instanceof Game)) {
            sender.sendMessage(ChatColor.RED+"There is no current game active!");
            return false;
        }

        Minigame game = MBC.getInstance().getMinigame();

        game.stopTimer();
        HandlerList.unregisterAll(game);
        game.setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayers()) {
            if (p.getPlayer().getAllowFlight()) {
                ((Game) game).removeWinEffect(p);
            }
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);

            p.resetCurrentScores();
        }
        for (MBCTeam t : MBC.getInstance().teams) {
            t.resetCurrentScores();
        }

        ((Game) game).onRestart();
        MBC.getInstance().lobby.start();
        return true;
    }
}
