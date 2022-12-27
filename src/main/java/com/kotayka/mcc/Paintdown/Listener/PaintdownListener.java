package com.kotayka.mcc.Paintdown.Listener;

import com.kotayka.mcc.Paintdown.Paintdown;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Team;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PaintdownListener implements Listener {
    public final Paintdown paintdown;
    public final Plugin plugin;

    public PaintdownListener(Paintdown paintdown, Plugin plugin) {
        this.paintdown = paintdown;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK) && !(e.getAction() == Action.RIGHT_CLICK_AIR)) return;

        Player player = e.getPlayer();
        Participant p = Participant.findParticipantFromPlayer(player);
        assert p != null;

        // Prevent painted players from doing anything
        if (p.getIsPainted()) return;

        // Cooldown
        if (player.getInventory().getItemInMainHand().getType() == Material.IRON_HORSE_ARMOR ||
            player.getInventory().getItemInOffHand().getType() == Material.IRON_HORSE_ARMOR) {
            long timeLeft = System.currentTimeMillis() - p.getCooldown();
            if (timeLeft >= 500) {
                Snowball projectile = player.launchProjectile(Snowball.class);
                projectile.setVelocity(new Vector(projectile.getVelocity().getX() * 1.25, projectile.getVelocity().getY() * 1.25, projectile.getVelocity().getZ() * 1.25));
                projectile.setShooter(player); // Not sure if this is necessary
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 2);
                p.setCooldown(System.currentTimeMillis());
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
            }
        }

        // track potion usage + potion cooldown
        else if (player.getInventory().getItemInMainHand().getType() == Material.SPLASH_POTION ||
            player.getInventory().getItemInOffHand().getType() == Material.SPLASH_POTION) {
            p.availablePotions--;

            // If player has no potions, give them a potion after 15 seconds
            if (p.availablePotions == 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (paintdown.getState().equals("PLAYING") && player.getGameMode() == GameMode.SURVIVAL) {
                        ItemStack potion = new ItemStack(Material.SPLASH_POTION, 1);
                        PotionMeta meta = (PotionMeta)potion.getItemMeta();
                        assert meta != null;
                        meta.setColor(Color.BLUE);
                        meta.setDisplayName("Water Bomb");
                        potion.setItemMeta(meta);

                        p.player.getInventory().addItem(potion);
                        p.availablePotions++;
                    }
                }, 300);
            }
            //return;
        }

        // Telepickaxe
        else if (player.getInventory().getItemInMainHand().getType() == Material.WOODEN_PICKAXE) {
            Team t = p.team.getTeam();
            long timeLeft = System.currentTimeMillis() - paintdown.telepickCooldowns.get(t);
            // 15 second cooldown for telepick
            if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 15) {
                ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                assert itemMeta != null;
                itemMeta.setUnbreakable(true);
                itemStack.setItemMeta(itemMeta);
                player.getInventory().setItemInMainHand(itemStack);

                paintdown.telepickCooldowns.remove(t);
                paintdown.telepickCooldowns.put(t, System.currentTimeMillis());
                // remove telepick from other team member if applicable
                for (Participant indexP : p.team.getPlayers()) {
                    indexP.player.sendMessage(p.team.getIcon() + p.team.getChatColor() + p.ign + ChatColor.WHITE + " has claimed the telepickaxe.");
                    if (indexP.ign.equals(p.ign)) continue;
                    if (indexP.hasTelepick) {
                        for(int i = 0; i<indexP.player.getInventory().getSize()-1; ++i) {
                            ItemStack item = indexP.player.getInventory().getItem(i);
                            try {
                                if(item.getType().equals(Material.DIAMOND_PICKAXE)) {
                                    item.setType(Material.WOODEN_PICKAXE);
                                }
                            } catch(NullPointerException exception) {
                                continue;
                            }
                        }
                        indexP.hasTelepick = false;
                        // only one person should have telepick at a time
                        break;
                    }
                }
                // change telepick ownership
                // ...maybe that would've been better implementation...
                p.hasTelepick = true;
            } else {
                player.sendMessage(ChatColor.RED + "Telepickaxe is on cooldown for another " + (15 - TimeUnit.MILLISECONDS.toSeconds(timeLeft)) + " seconds!");
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        // for debugging ONLY (leave commented during events, although in theory nothing changes)
        // if (!(e.getEntity() instanceof Snowball) && !(e.getEntity() instanceof Arrow)) return;
        if (!(e.getEntity() instanceof Snowball)) return;

        // If we hit a terracotta block, paint it //
        if (e.getHitBlock() != null) {
            Block b = e.getHitBlock();
            assert e.getEntity().getShooter() instanceof Player;
            Player shooter = (Player) e.getEntity().getShooter();
            Participant participant = Participant.findParticipantFromPlayer(shooter);
            if (b.getType().toString().endsWith("TERRACOTTA")) {
                Material type = b.getType();

                // Not sure if runtime is same as if we use an arraylist,
                // might change later for memory conservation
                if (!(paintdown.paintedBlocks.containsKey(b.getLocation()))) {
                    // put <Location, Original Block Type>
                    paintdown.paintedBlocks.put(b.getLocation(), type);
                }

                switch (Objects.requireNonNull(participant).team.getTeam()) {
                    case RED_RABBITS -> type = Material.RED_GLAZED_TERRACOTTA;
                    case YELLOW_YAKS -> type = Material.YELLOW_GLAZED_TERRACOTTA;
                    case GREEN_GUARDIANS -> type = Material.LIME_GLAZED_TERRACOTTA;
                    case BLUE_BATS -> type = Material.BLUE_GLAZED_TERRACOTTA;
                    case PURPLE_PANDAS -> type = Material.PURPLE_GLAZED_TERRACOTTA;
                    case PINK_PIGLETS -> type = Material.PINK_GLAZED_TERRACOTTA;
                }
                // set block team's color
                b.setType(type);
            }
            return;
        }

        // Otherwise check if player was hit
        if (!(e.getHitEntity() instanceof Player)) return;

        Player hitPlayer = (Player) e.getHitEntity();
        assert e.getEntity().getShooter() instanceof Player;
        Player shooter = (Player) e.getEntity().getShooter();
        Participant participant = Participant.findParticipantFromPlayer(hitPlayer);
        Participant participantShooter = Participant.findParticipantFromPlayer(shooter);
        assert participantShooter != null;
        assert participant != null;

        // comment this line out ONLY when debugging solo (as in, you need to hit yourself with an arrow)
        if ((participant.team).equals(participantShooter.team)) return;

        Vector snowballVelocity = e.getEntity().getVelocity();

        shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 5);
        paintdown.paintHitPlayer(participant);

        // set painted by
        participant.setPaintedBy(shooter);
        // If player died
        if (hitPlayer.getHealth() - 10 <= 0) {
            // if player is in the air, teleport them to bottom
            /*
            double y_loc = hitPlayer.getLocation().getY();
            Location loc = new Location(hitPlayer.getWorld(), hitPlayer.getLocation().getX(), y_loc, hitPlayer.getLocation().getZ());
            while (loc.getBlock().getType().equals(Material.AIR)) {
                if (y_loc <= -[LOWEST LEVEL POSSIBLE]) {

                }

            }*/

            paintdown.mcc.scoreboardManager.addScore(paintdown.mcc.scoreboardManager.players.get(shooter.getUniqueId()), 15);

            String paintedString = ChatColor.RED + "P" + ChatColor.GOLD + "A" + ChatColor.YELLOW + "I" + ChatColor.GREEN + "N" +
                                    ChatColor.AQUA + "T" + ChatColor.BLUE + "E" + ChatColor.DARK_PURPLE + "D" + ChatColor.LIGHT_PURPLE + "!";
            hitPlayer.sendTitle(paintedString, null, 0, 40, 20);
            participant.setIsPainted(true);
            // paint all the armor
            ItemStack[] armor = participant.player.getInventory().getArmorContents();
            for (int i = 0; i < 4; i++) {
                ItemStack leatherPiece = paintdown.getPaintedLeatherArmor(armor[i]);
                switch (leatherPiece.getType()) {
                    case LEATHER_HELMET -> hitPlayer.getInventory().setHelmet(leatherPiece);
                    case LEATHER_CHESTPLATE -> hitPlayer.getInventory().setChestplate(leatherPiece);
                    case LEATHER_LEGGINGS -> hitPlayer.getInventory().setLeggings(leatherPiece);
                    case LEATHER_BOOTS -> hitPlayer.getInventory().setBoots(leatherPiece);
                }
            }
            // if player had telepick, remove it and reset the cooldown
            if (participant.hasTelepick) {
                participant.hasTelepick = false;

                for(int i = 0; i<participant.player.getInventory().getSize()-1; ++i) {
                    ItemStack item = participant.player.getInventory().getItem(i);
                    try {
                        if(item.getType().equals(Material.DIAMOND_PICKAXE)) {
                            item.setType(Material.WOODEN_PICKAXE);
                            break;
                        }
                    } catch(NullPointerException exception) {
                        continue;
                    }
                }
                // automatically allow team to use telepick again
                paintdown.telepickCooldowns.remove(participant.team.getTeam());
                paintdown.telepickCooldowns.put(participant.team.getTeam(), TimeUnit.SECONDS.toMillis(15));
            }

            // Check if whole team died
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

                @Override
                public void run() {
                    paintdown.checkFullTeamDeath(participant);
                }

            }, 60);
            // Died but teammates are alive
            hitPlayer.setHealth(20);

            // Only send death messages to involved players (shooter's team and hit player's team)
            for (Participant p : participantShooter.team.getPlayers()) {
                p.player.sendMessage(participant.team.getIcon() + participant.team.getChatColor() + participant.ign + ChatColor.WHITE + " was painted by "
                        + participantShooter.team.getIcon() + participantShooter.team.getChatColor() + participantShooter.ign);
            }
            for (Participant p : participant.team.getPlayers()) {
                p.player.sendMessage(participant.team.getIcon() + participant.team.getChatColor() + participant.ign + ChatColor.WHITE + " was painted by "
                        + participantShooter.team.getIcon() + participantShooter.team.getChatColor() + participantShooter.ign);
            }

        } else if (!(participant.getIsPainted())) {
            hitPlayer.damage(10);
            hitPlayer.setVelocity(new Vector(snowballVelocity.getX() * 0.1, 0.5, snowballVelocity.getZ() * 0.1));
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        Collection<LivingEntity> affected = e.getAffectedEntities();

        for (LivingEntity ent : affected) {
            if (ent instanceof Player) {
                Participant p = Participant.findParticipantFromPlayer((Player) ent);
                Participant potionThrower = Participant.findParticipantFromPlayer((Player) e.getPotion().getShooter());
                assert p != null;
                assert potionThrower != null;

                // prevent healing enemy team
                if (!(p.team.equals(potionThrower.team))) continue;

                // Restore armor
                ItemStack[] armor = p.player.getInventory().getArmorContents();
                for (int i = 0; i < 4; i++) {
                    ItemStack newArmor = p.getColoredLeatherArmor(armor[i]);
                    switch (newArmor.getType()) {
                        case LEATHER_HELMET -> p.player.getInventory().setHelmet(newArmor);
                        case LEATHER_CHESTPLATE -> p.player.getInventory().setChestplate(newArmor);
                        case LEATHER_LEGGINGS -> p.player.getInventory().setLeggings(newArmor);
                        default -> p.player.getInventory().setBoots(newArmor);
                    }
                }

                if (p.getIsPainted())  {
                    p.setIsPainted(false);

                    // broadcast the exiting news to the team
                    for (Participant temp : p.team.getPlayers()) {
                        temp.player.sendMessage(p.team.getIcon() + p.team.getChatColor() + p.ign + ChatColor.WHITE + " was revived!");
                    }

                    // broadcast the not very exciting news to the enemy team
                    Participant shotBy = Participant.findParticipantFromPlayer(p.getPaintedBy());
                    assert shotBy != null;
                    for (Participant temp : shotBy.team.getPlayers()) {
                        temp.player.sendMessage(p.team.getIcon() + p.team.getChatColor() + p.ign + ChatColor.WHITE + " was revived!");
                        // points are only for final kills unfortunately
                        if (temp.player.getUniqueId().equals(shotBy.player.getUniqueId())) {
                            paintdown.mcc.scoreboardManager.addScore(paintdown.mcc.scoreboardManager.players.get(shotBy.player.getUniqueId()), -15);
                        }
                    }

                    p.setPaintedBy(null);
                } else {
                    p.player.setHealth(20);
                    p.setPaintedBy(null);
                }
            }
        }
    }


    // Prevent moving when painted
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }

        if ((Objects.requireNonNull(Participant.findParticipantFromPlayer(e.getPlayer())).getIsPainted())) {
            e.setCancelled(true);
            e.setTo(e.getPlayer().getLocation());
            return;
        }
        Player p = e.getPlayer();
        Participant participant = Participant.findParticipantFromPlayer(p);
        assert participant != null;
        if (p.getLocation().getY() < -30 && p.getGameMode().equals(GameMode.SURVIVAL)) {
            p.setGameMode(GameMode.SPECTATOR);
            // if necessary in the future we will prevent
            // early spec'ing
            // p.setSpectatorTarget();
            if (participant.getPaintedBy() == null)
                Bukkit.broadcastMessage(participant.team.getIcon() + participant.team.getChatColor() + participant.ign + ChatColor.WHITE + " fell into molten lava");
            else {
                Participant lastPainted = Participant.findParticipantFromPlayer(participant.getPaintedBy());
                assert lastPainted != null;
                Bukkit.broadcastMessage(participant.team.getIcon() + participant.team.getChatColor() + participant.ign + ChatColor.WHITE + " fell into molten lava after being tagged by "
                        + lastPainted.team.getIcon() + lastPainted.team.getChatColor() + lastPainted.ign);
            }
            paintdown.deadList.add(participant.player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

                @Override
                public void run() {
                    paintdown.checkFullTeamDeath(participant);
                }

            }, 60);
        }
    }

    @EventHandler()
    public void onBlockBreak(BlockBreakEvent e) {
        if (!(paintdown.getState().equals("PLAYING")) && !(paintdown.getState().equals("STARTING")) && !(paintdown.getState().equals("END_ROUND"))) {
            return;
        }

        if (!(e.getBlock().getType().equals(Material.LODESTONE))) {
            e.setCancelled(true);
            return;
        }

        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE) {
            Participant brokeBlock = Participant.findParticipantFromPlayer(e.getPlayer());
            assert brokeBlock != null;
            for (Participant p : brokeBlock.team.getPlayers()) {
                paintdown.mcc.scoreboardManager.addScore(paintdown.mcc.scoreboardManager.players.get(p.player.getUniqueId()), 1);
            }
        } else {
            e.setCancelled(true);
        }
    }

    /* For BORDER DAMAGE */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getEntity() instanceof Player)) { return; }
        if (!(e.getEntity().getWorld().equals(paintdown.world))) return;

        // If border damage kills player
        if (e.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
            Player player = (Player) e.getEntity();
            if (player.getHealth() - e.getDamage() < 1) {
                e.setCancelled(true);
                player.setHealth(20);
                player.setGameMode(GameMode.SPECTATOR);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 2, false, false));

                Participant participant = Participant.findParticipantFromPlayer(player);
                assert participant != null;
                paintdown.deadList.add(participant.player.getUniqueId());

                if (participant.paintedBy != null) {
                    // give last player to tag them 15 coins
                    paintdown.mcc.scoreboardManager.addScore(paintdown.mcc.scoreboardManager.players.get(participant.paintedBy.getUniqueId()), 15);
                    participant.paintedBy = null;
                }
                Bukkit.broadcastMessage(participant.team.getIcon() + participant.team.getChatColor() + participant.ign + ChatColor.WHITE + " was stuck in a melting room");

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

                    @Override
                    public void run() {
                        paintdown.checkFullTeamDeath(participant);
                    }

                }, 60);
            }
        }
    }

    // Players can't take off armor
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if (!(paintdown.getState().equals("PLAYING")) && !(paintdown.getState().equals("STARTING")) && !(paintdown.getState().equals("END_ROUND"))) { return; }

        if(event.getSlotType() == InventoryType.SlotType.ARMOR)
        {
            event.setCancelled(true);
        }
    }

    // Prevent dropping items.
    @EventHandler
    public void handleItemDrop(PlayerDropItemEvent event) {
        if (!(paintdown.getState().equals("PLAYING")) && !(paintdown.getState().equals("STARTING")) && !(paintdown.getState().equals("END_ROUND"))) { return; }

        event.setCancelled(true);
        doInventoryUpdate(event.getPlayer(), plugin);
    }

    // Used in handleItemDrop() event.
    public static void doInventoryUpdate(final Player player, Plugin plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
                player.updateInventory();
            }

        }, 1L);
    }
}
