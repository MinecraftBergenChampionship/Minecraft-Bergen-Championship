package me.kotayka.mbc.commands;

import me.kotayka.mbc.Lobby;
import me.kotayka.mbc.MBC;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class scores implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (MBC.getInstance().getMinigame() instanceof Lobby && p.getGameMode().equals(GameMode.SPECTATOR)) {
                p.sendMessage("Wait until the cutscene is over!");
                return false;
            }

            MBC.getInstance().getTopIndividualAndPlacement(p);
            return true;
        }
        return false;
    }
}
