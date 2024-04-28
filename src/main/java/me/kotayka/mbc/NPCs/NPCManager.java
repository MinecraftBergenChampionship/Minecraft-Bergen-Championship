package me.kotayka.mbc.NPCs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Iterator;

import java.util.ArrayList;

public class NPCManager {
    public final JavaPlugin plugin;
    public static ArrayList<NPC> npcs = new ArrayList<>();

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public NPC createNPC(Player player, Location loc) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
        npcs.add(npc);
        npc.spawn(loc);
        return npc;
    }

    public boolean remove(NPC npc) {
        return npc.isSpawned() && npc.despawn();
    }

    public void removeAllNPCs() {
        CitizensAPI.getNPCRegistry().despawnNPCs(DespawnReason.PLUGIN);
    }
}

    /*
    public NPC createNPC(Player player, Location loc){
        NPC npc = new NPC(this.plugin, player, loc);
        npc.create(player.getName());
        npcs.add(npc);
        return npc;

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
 */