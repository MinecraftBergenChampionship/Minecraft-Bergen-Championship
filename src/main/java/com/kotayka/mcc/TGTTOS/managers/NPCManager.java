package com.kotayka.mcc.TGTTOS.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kotayka.mcc.mainGame.manager.Players;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NPCManager {

    public final Plugin plugin;
    public List<EntityPlayer> npcs = new ArrayList<EntityPlayer>();
    public List<Location> npcsLocations = new ArrayList<Location>();
    public final Players players;
    Player p;

    public NPCManager(Plugin plugin, Players players) {
        this.plugin = plugin;
        this.players = players;
    }

    public void spawnNPC(Player player, Location loc){
        p = player;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Objects.requireNonNull(((Player) player).getWorld())).getHandle();

        String[] things = getRequest(player.getName());

        String texture = things[0];
        String signature = things[1];


        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
        EntityPlayer npcPlayer = new EntityPlayer(server, world, gameProfile);
        Location npcLocation = loc;
        npcPlayer.b(npcLocation.getX(), npcLocation.getY(), npcLocation.getZ(), npcLocation.getYaw(), npcLocation.getPitch());

        for (int i = 0; i < players.players.size(); i++) {
            PlayerConnection connection = ((CraftPlayer) players.players.get(i)).getHandle().b;
            connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, npcPlayer));
            connection.a(new PacketPlayOutNamedEntitySpawn(npcPlayer));
            connection.a(new PacketPlayOutEntityHeadRotation(npcPlayer, (byte) (npcLocation.getYaw() * 256 / 360)));
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, npcPlayer));
                }
            }, 20);

        }

        player.sendMessage(ChatColor.GREEN+"Spawned new NPC at "+npcLocation.getX()+", "+npcLocation.getY()+", "+npcLocation.getZ());
        npcs.add(npcPlayer);
        npcsLocations.add(npcLocation);
    }

    public int CheckIfValidID(int entityID) {
        for (int i = 0; i < npcs.size(); i++) {
            if (entityID == npcs.get(i).getBukkitEntity().getEntityId()) {
                return i;
            }
        }
        return -1;
    }

    public void removeNPC(int entityID) {
        for (int i = 0; i < players.players.size(); i++) {
            PlayerConnection connection = ((CraftPlayer) players.players.get(i)).getHandle().b;
            connection.a(new PacketPlayOutEntityDestroy(npcs.get(entityID).ae()));
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                Firework fw = new Firework();
                Location loc = npcsLocations.get(entityID);
                Location location = new Location(loc.getWorld(), loc.getX(), loc.getY()+2, loc.getZ());
                fw.spawnFirework(location);
            }
        }, 0);
    }

    public void removeAllNPC() {
        for (EntityPlayer npc : npcs) {
            for (int i = 0; i < players.players.size(); i++) {
                PlayerConnection connection = ((CraftPlayer) players.players.get(i)).getHandle().b;
                connection.a(new PacketPlayOutEntityDestroy(npc.ae()));
            }
        }
    }

    public void addPlayer(Player p) {
        Bukkit.getServer().getConsoleSender().sendMessage("Player Added");
        for (int i = 0; i < npcs.size(); i++) {
            PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
            connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, npcs.get(i)));
            connection.a(new PacketPlayOutNamedEntitySpawn(npcs.get(i)));
            connection.a(new PacketPlayOutEntityHeadRotation(npcs.get(i), (byte) (npcsLocations.get(i).getYaw() * 256 / 360)));
        }
    }

    public String[] getRequest(String name)  {
        try {
            URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
            String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new String[] {texture, signature};
        } catch (IOException e) {
            System.err.println("Could not get skin data from session servers!");
            e.printStackTrace();
            return null;
        }
    }
}
