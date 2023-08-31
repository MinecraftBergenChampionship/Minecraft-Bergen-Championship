package me.kotayka.mbc.NPCs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class NPCManager {
    public final JavaPlugin plugin;
    public Map<Integer, NPC> npcs = new HashMap<>();

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int createNPC(Player player, Location loc){
        NPC npc = new NPC(this.plugin, player, loc);
        int entityID = npc.create();
        npcs.put(entityID, npc);
        return entityID;
    }

    public void show(int id, Player p) {
        if (npcs.containsKey(id)) {
            npcs.get(id).show(p);
        }
    }

    public void showAll(int id) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.show(id, p);
        }
    }

    public void addPlayer(Player p) {
        for (Map.Entry<Integer, NPC> entry : npcs.entrySet()) {
            entry.getValue().show(p);
        }
    }

    public void remove(int id, Player p) {
        if (npcs.containsKey(id)) {
            npcs.get(id).show(p);
        }
    }

    public void removeAll(int id) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.remove(id, p);
        }
    }
}