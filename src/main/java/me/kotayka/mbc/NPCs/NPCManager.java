package me.kotayka.mbc.NPCs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kotayka.mbc.Plugin;
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
    public static List<NPC> npcs = new ArrayList<>();

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public NPC createNPC(Player player, Location loc){
        NPC npc = new NPC(this.plugin, player, loc);
        npc.create(player.getName());
        npcs.add(npc);
        return npc;
    }

    public void show(NPC npc, Player p) {
        npc.show(p);
    }

    public void showAll(NPC npc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.show(npc, p);
        }
    }

    public void addPlayer(Player p) {
        for (NPC npc : npcs) {
            npc.show(p);
        }
    }

    public void remove(NPC npc, Player p) {
        npc.remove(p);
    }

    public void removeAll(NPC npc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.remove(npc, p);
        }
    }
}