package me.kotayka.mbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import me.kotayka.mbc.gameMaps.quickfireMap.Mansion;
import me.kotayka.mbc.gameMaps.quickfireMap.QuickfireMap;
import me.kotayka.mbc.gamePlayers.QuickfirePlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;



public class Quickfire extends FinaleGame {
    private final QuickfireMap map = new Mansion(this);
    public static final ItemStack CROSSBOW = new ItemStack(Material.CROSSBOW);
    public static final ItemStack BOOTS = new ItemStack(Material.LEATHER_BOOTS);
    public Map<UUID, QuickfirePlayer> quickfirePlayers = new HashMap<>();
    public static final int MAX_DIST_FROM_CENTER = 13225;
    private World world = Bukkit.getWorld("Quickfire");
    private final Location TEAM_ONE_SPAWN = map.getTeamOneSpawn();
    private final Location TEAM_TWO_SPAWN = map.getTeamTwoSpawn();

    private Map<UUID, String> playerWalkoutSongs = new HashMap<>();
    private Map<UUID, String> playerWalkoutBlurbs = new HashMap<>();
    private Map<UUID, String> playerWalkoutSongNames = new HashMap<>();
    private String defaultWalkoutSong = "walkout.default";
    private List<Participant> firstPlaceWalkoutOrder = new ArrayList<>();
    private List<Participant> secondPlaceWalkoutOrder = new ArrayList<>();
    private List<Participant> alreadyWalkedOut = new ArrayList<>();
    private int currentWalkoutNumber = 0;
    private Participant currentlyWalkingOut;

    private final int finalePlayers;
    private final int firstPlacePlayers;
    private final int secondPlacePlayers;
    private final Location SPAWN = map.getSpawn();
    private int[] playersAlive;
    private int timeElapsed = 0;
    private int timeUntilGlowing = map.getTimeUntilGlowing();
    private int roundNum = 0;
    private boolean disconnect = false;
    public Quickfire() {
        super("Quickfire", new String[] {
                "Welcome to the Finale of MBC; Quickfire!\n Only the top two teams will play this round.\n",
                "Quickfire is a shooter game where every player has a crossbow, infinite arrows, and 4 hearts.\n",
                "Shooting another player will deal exactly one heart of health.\n" + ChatColor.RED + "Shoot someone four times to eliminate them.\n",
                "Eliminate the entire opposing team to win a round.\n" + ChatColor.YELLOW + "Quickfire is best of 5 rounds.\n",
                "Once the timer exceeds a minute and a half, all players will receive the " + ChatColor.BOLD + "glowing" + ChatColor.RESET + " effect.\n",
                "Each crossbow has the " + ChatColor.BOLD + "Quick Charge" + ChatColor.RESET + " enchantment, so be sure to fire fast!\n",
                "No points are awarded for this game.\n" + ChatColor.YELLOW + "The winning team will win the Minecraft Bergen Championship!\n"
        });

        /*
        for (Participant p : firstPlace.teamPlayers) {
            teamOnePlayers.add(new DodgeboltPlayer(p, true));
        }
        for (Participant p : secondPlace.teamPlayers) {
            teamTwoPlayers.add(new DodgeboltPlayer(p, false));
        }
         */

        ItemMeta bowMeta = CROSSBOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        CROSSBOW.setItemMeta(bowMeta);
        CROSSBOW.addEnchantment(Enchantment.QUICK_CHARGE, 3);

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
        firstPlacePlayers = firstPlace.teamPlayers.size();
        secondPlacePlayers = secondPlace.teamPlayers.size();
        finalePlayers = firstPlacePlayers+secondPlacePlayers;

        fillMaps();
    }

    public Quickfire(@NotNull MBCTeam firstPlace, @NotNull MBCTeam secondPlace) {
        super("Quickfire", firstPlace, secondPlace, new String[] {
                "⑩ Welcome to the Finale of MBC; Quickfire!\n Only the top two teams will play this round.\n",
                "⑩ Quickfire is a shooter game where every player has a crossbow, infinite arrows, and 4 hearts.\n",
                "⑩ Shooting another player will deal exactly one heart of health.\n" + ChatColor.RED + "Shoot someone four times to eliminate them.\n",
                "⑩ Eliminate the entire opposing team to win a round.\n" + ChatColor.YELLOW + "Quickfire is best of 5 rounds.\n",
                "⑩ Once the timer exceeds a minute and a half, all players will receive the " + ChatColor.BOLD + "glowing" + ChatColor.RESET + " effect.\n",
                "⑩ Each crossbow has the " + ChatColor.BOLD + "Quick Charge" + ChatColor.RESET + " enchantment, so be sure to fire fast!\n",
                "⑩ No points are awarded for this game.\n" + ChatColor.YELLOW + "The winning team will win the Minecraft Bergen Championship!\n"
        });

        if (logger == null) {
            initLogger();
            //Bukkit.broadcastMessage("logger bad :(");
        }

        ItemMeta bowMeta = CROSSBOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        CROSSBOW.setItemMeta(bowMeta);
        CROSSBOW.addEnchantment(Enchantment.QUICK_CHARGE, 3);

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
        firstPlacePlayers = firstPlace.teamPlayers.size();
        secondPlacePlayers = secondPlace.teamPlayers.size();
        finalePlayers = firstPlacePlayers+secondPlacePlayers;

        fillMaps();
    }

    public void fillMaps() {
        UUID cam = UUID.fromString("5618582d-edf2-41db-8549-56319cd45a98");
        UUID collin = UUID.fromString("2ca6218b-02b0-4d02-8935-34126ee15d4f");
        UUID caleb = UUID.fromString("31de7d72-2000-474c-a583-e193d85cfbcc");
        UUID devin = UUID.fromString("aadabd61-0548-4b3b-8c49-6efac9ca1f55");
        UUID nikola = UUID.fromString("86db6c06-b38d-430c-a572-75afe051f482");
        UUID eclipse = UUID.fromString("351e2396-70ca-453f-8415-695e33061da3");
        UUID terri = UUID.fromString("0961f44c-0f50-41de-ad24-bb00692811dd");
        UUID edward = UUID.fromString("ae32acfb-9446-485f-bf79-4262d8458a1c");
        UUID cookie = UUID.fromString("1134e9f1-5daa-4078-b2d7-b79f784ea3db");
        UUID tony = UUID.fromString("7bb2f9e8-e030-44cb-9141-5c419dd09b21");
        UUID carrie = UUID.fromString("478eb5a6-adc9-40f0-80a1-e82fd3fb4561");
        UUID cres = UUID.fromString("84c20807-2a17-4f9f-9c9b-15cd71b5633d");
        UUID mango = UUID.fromString("6fa5b228-c821-451c-b936-52dec38923a1");
        UUID liz = UUID.fromString("5c58b780-daf9-4558-926f-b89ff18a4858");
        UUID esther = UUID.fromString("bb684862-ad93-4373-b89c-4151116acd06");
        UUID janzelle = UUID.fromString("f1794b6e-2c6c-4d43-84b2-16c1eb15c7ab");
        UUID pickle = UUID.fromString("acd494ee-3ba3-4de2-9474-6a21261d9413");
        UUID noah = UUID.fromString("80b6f6d7-afbd-4c78-befa-55bc26579b19");
        UUID queakie = UUID.fromString("355562aa-2235-4838-9afb-87fc23aec431");

        playerWalkoutSongs.put(cam, "walkout.bigkirbypuff");
        playerWalkoutSongs.put(collin, "walkout.rspacerr");
        playerWalkoutSongs.put(caleb, "walkout.mrcarb");
        playerWalkoutSongs.put(devin, "walkout.idrg");
        playerWalkoutSongs.put(nikola, "walkout.nikesauce");
        playerWalkoutSongs.put(eclipse, "walkout.remtolka");
        playerWalkoutSongs.put(terri, "walkout.aesoney");
        playerWalkoutSongs.put(edward, "walkout.bapplebusiness");
        playerWalkoutSongs.put(cookie, "walkout.cookie");
        playerWalkoutSongs.put(tony, "walkout.atrackpadplayer");
        playerWalkoutSongs.put(carrie, "walkout.sandwichmasterxx");
        playerWalkoutSongs.put(cres, "walkout.cres_uwu");
        playerWalkoutSongs.put(mango, "walkout.mango3139");
        playerWalkoutSongs.put(liz, "walkout.mastersvetlana");
        playerWalkoutSongs.put(esther, "walkout.zero");
        playerWalkoutSongs.put(janzelle, "walkout.janeru");
        playerWalkoutSongs.put(pickle, "walkout.picklepvp");
        playerWalkoutSongs.put(noah, "walkout.trapshadow");
        playerWalkoutSongs.put(queakie, "walkout.queakie");

        playerWalkoutBlurbs.put(cam, "Representing the South Side of Chicago...");
        playerWalkoutBlurbs.put(collin, "Don't ask him how he eats his Chicken Nuggets...");
        playerWalkoutBlurbs.put(caleb, "With his mouse in one hand and an extra large boba in the other...");
        playerWalkoutBlurbs.put(devin, "The powerless president and fraudulent dictator of Ethipia...");
        playerWalkoutBlurbs.put(nikola, "Watch out for his railroad tunnels underneath the Quickfire arena...");
        playerWalkoutBlurbs.put(eclipse, "She's immortal until proven mortal...");
        playerWalkoutBlurbs.put(terri, "The Queen of Build Mart herself...");
        playerWalkoutBlurbs.put(edward, "Known to many as \"Wikipedia's most Wanted\"...");
        playerWalkoutBlurbs.put(cookie, "One could say she's a \"tough cookie\"...");
        playerWalkoutBlurbs.put(tony, "When his pants turn brown... run...");
        playerWalkoutBlurbs.put(carrie, "A woman of many middle names...");
        playerWalkoutBlurbs.put(cres, "Everybody, raise your ya ya ya for...");
        playerWalkoutBlurbs.put(mango, "AHHHHHHHHHHH!");
        playerWalkoutBlurbs.put(liz, "To everyone's shock, awe, and disbelief...");
        playerWalkoutBlurbs.put(esther, "Don't ask her about her time working at Paris Baguette...");
        playerWalkoutBlurbs.put(janzelle, "She is not in danger - she IS the danger...");
        playerWalkoutBlurbs.put(pickle, "Pickle, puh Pickle, puh, puh, puh, Pickle Pickle...");
        playerWalkoutBlurbs.put(noah, "Just the name \"Noah Paul Gaming\" strikes fear in his competitors...");
        playerWalkoutBlurbs.put(queakie, "Ready to find your entire family genealogy..");

        playerWalkoutSongNames.put(cam, "Sleepyhead, Passion Pit");
        playerWalkoutSongNames.put(collin, "Radioactive, Imagine Dragons");
        playerWalkoutSongNames.put(caleb, "HOT TO GO!, Chappell Roan");
        playerWalkoutSongNames.put(devin, "Desenfocao', Rauw Alejandro");
        playerWalkoutSongNames.put(nikola, "Boss, Plok!");
        playerWalkoutSongNames.put(eclipse, "Immortals, Fall Out Boy");
        playerWalkoutSongNames.put(terri, "Daydreamin', Ariana Grande");
        playerWalkoutSongNames.put(edward, "Requiem: II, György Ligeti");
        playerWalkoutSongNames.put(cookie, "Low, Flo Rida & T-Pain");
        playerWalkoutSongNames.put(tony, "Home Depot Theme Song");
        playerWalkoutSongNames.put(carrie, "Like a G6, Far East Movement");
        playerWalkoutSongNames.put(cres, "Gameboy, KATSEYE");
        playerWalkoutSongNames.put(mango, "The Devourer of Gods, Terraria Calamity Mod");
        playerWalkoutSongNames.put(liz, "Fight Song, Sister Sin");
        playerWalkoutSongNames.put(esther, "SHYNE, Travis Scott");
        playerWalkoutSongNames.put(janzelle, "Next Level Charli, Charli XCX");
        playerWalkoutSongNames.put(pickle, "Pickle Song");
        playerWalkoutSongNames.put(noah, "Steve Intro, Super Smash Bros. Ultimate");
        playerWalkoutSongNames.put(queakie, "We Like to Party, Vengaboys");
    }

    @Override
    public void start() {
        // stopTimer() and the commands to deregister lobby/dd should prob be moved to super.start()
        stopTimer();
        if (firstPlace == null) { return; }
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
        changeColor();
        loadPlayers();

        if (logger == null) {
            Bukkit.broadcastMessage("logger bad :( elsewhere wtf");
            initLogger();
        }


        setGameState(GameState.TUTORIAL);
        setTimer(finalePlayers*12 + 31);
    }

    @Override
    public void loadPlayers() {
        if (firstPlace == null) return;

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().setGameMode(GameMode.SPECTATOR);
            p.getPlayer().getInventory().clear();
            p.getPlayer().teleport(SPAWN);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 255, false, false));
            if (p.getTeam().equals(firstPlace) || p.getTeam().equals(secondPlace)) {
                quickfirePlayers.put(p.getPlayer().getUniqueId(), new QuickfirePlayer(p));
                p.board.getTeam(firstPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                p.board.getTeam(secondPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            }
        }
    }

    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining < 0) {
                Bukkit.broadcastMessage("tutorial negative");
                timeRemaining = finalePlayers*12 + 31;
            }
            if (timeRemaining == 0) {
                startRound();
                setGameState(GameState.STARTING);

                timeRemaining = 35;
            } //else if (timeRemaining % 7 == 0) {
                //Introduction();
            //}
            if (timeRemaining == finalePlayers*12 + 30) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.BOLD + "Quickfire", firstPlace.teamNameFormat() + " vs. " + secondPlace.teamNameFormat(), 5, 60, 35);
                    p.playSound(p, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.RECORDS,1, 1);
                    playerIntroOrder();
                }

            }
            if (timeRemaining == finalePlayers*12 + 25) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Welcome to the finale of MBC, " + ChatColor.RESET + "⑩" + ChatColor.BOLD + " Quickfire!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }
            }
            if (timeRemaining == finalePlayers*12 + 20) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "The first place " + ChatColor.RESET + firstPlace.teamNameFormat() + 
                                            ChatColor.BOLD + " will be playing the second place " +  ChatColor.RESET + secondPlace.teamNameFormat() +
                                            ChatColor.BOLD + " for the MBC "+ ChatColor.RESET + "④" +ChatColor.GOLD + ChatColor.BOLD  + " CROWN!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }
            }
            if (timeRemaining == finalePlayers*12 + 15) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.teleport(map.getTeamTwoIntro());
                }
            }
            if (timeRemaining == finalePlayers*12 + 14) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Introducing the second place team: the " + ChatColor.RESET + secondPlace.teamNameFormat() + ChatColor.BOLD + "!");
                currentWalkoutNumber = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,SoundCategory.RECORDS, 1, 1);
                }
            }
            if (timeRemaining > firstPlacePlayers*12 + 10 && timeRemaining <= finalePlayers*12 + 10) {
                switch(timeRemaining%12) {
                    case (11) -> {
                        currentWalkoutNumber++;
                    }
                    case (10) -> {
                        if (currentWalkoutNumber >= secondPlaceWalkoutOrder.size()) currentWalkoutNumber = secondPlaceWalkoutOrder.size()-1;
                        currentlyWalkingOut = secondPlaceWalkoutOrder.get(currentWalkoutNumber);
                        UUID playerUUID = currentlyWalkingOut.getPlayer().getUniqueId();
                        String song = playerWalkoutSongs.get(playerUUID);
                        if (song == null) {
                            song = defaultWalkoutSong;
                        }
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p, song, 1, 1);
                        }
                    }
                    case (7) -> {
                        UUID playerUUID = currentlyWalkingOut.getPlayer().getUniqueId();
                        String blurb = playerWalkoutBlurbs.get(playerUUID);
                        if (blurb == null) {
                            blurb = ChatColor.BOLD + "From the " + ChatColor.RESET + secondPlace.teamNameFormat() + ChatColor.BOLD + "...";
                        }
                        Bukkit.broadcastMessage(ChatColor.BOLD + blurb);
                    }
                    case (4) -> {
                        currentlyWalkingOut.getPlayer().teleport(TEAM_TWO_SPAWN);
                        currentlyWalkingOut.getPlayer().setGameMode(GameMode.ADVENTURE);
                        MBC.spawnFirework(currentlyWalkingOut);
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle(currentlyWalkingOut.getFormattedName(), "", 20, 60, 20);
                        }
                        Bukkit.broadcastMessage(currentlyWalkingOut.getFormattedName() + "!");
                    }
                }
            }
            if (timeRemaining == firstPlacePlayers*12 + 10) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.teleport(map.getTeamOneIntro());
                }
            }
            if (timeRemaining == firstPlacePlayers*12 + 9) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Introducing the first place team: the " + ChatColor.RESET + firstPlace.teamNameFormat() + ChatColor.BOLD + "!");
                currentWalkoutNumber = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,SoundCategory.RECORDS, 1, 1);
                }
            }
            if (timeRemaining > 5 && timeRemaining <= firstPlacePlayers*12 + 5) {
                switch(timeRemaining%12) {
                    case (10) -> {
                        currentlyWalkingOut.getPlayer().teleport(TEAM_ONE_SPAWN);
                        currentlyWalkingOut.getPlayer().setGameMode(GameMode.ADVENTURE);
                        MBC.spawnFirework(currentlyWalkingOut);
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendTitle(currentlyWalkingOut.getFormattedName(), "", 20, 60, 20);
                        }
                        Bukkit.broadcastMessage(currentlyWalkingOut.getFormattedName() + "!");
                    }
                    case (5) -> {
                        currentWalkoutNumber++;
                    }
                    case (4) -> {
                        if (currentWalkoutNumber >= firstPlaceWalkoutOrder.size()) currentWalkoutNumber = firstPlaceWalkoutOrder.size()-1;
                        currentlyWalkingOut = firstPlaceWalkoutOrder.get(currentWalkoutNumber);
                        UUID playerUUID = currentlyWalkingOut.getPlayer().getUniqueId();
                        String song = playerWalkoutSongs.get(playerUUID);
                        if (song == null) {
                            song = defaultWalkoutSong;
                        }
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p, song, 1, 1);
                        }
                    }
                    case (1) -> {
                        UUID playerUUID = currentlyWalkingOut.getPlayer().getUniqueId();
                        String blurb = playerWalkoutBlurbs.get(playerUUID);
                        if (blurb == null) {
                            blurb = ChatColor.BOLD + "From the " + ChatColor.RESET + firstPlace.teamNameFormat() + ChatColor.BOLD + "...";
                        }
                        Bukkit.broadcastMessage(ChatColor.BOLD + blurb);
                    }
                }
            }
            if (timeRemaining == 4) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "The winner of this best of 5 game of Quickfire will win MBC!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
                }
            }
            if (timeRemaining == 2) {
                
                Bukkit.broadcastMessage(ChatColor.BOLD + "All songs from the finale contestants:\n");
                ArrayList<UUID> songsDisplayed = new ArrayList<>();
                for (int i = 0; i < secondPlaceWalkoutOrder.size(); i++) {
                    Participant p = secondPlaceWalkoutOrder.get(i);
                    UUID playerUUID = p.getPlayer().getUniqueId();
                    if (!songsDisplayed.contains(playerUUID)) {
                        String songName = playerWalkoutSongNames.get(playerUUID);
                        Bukkit.broadcastMessage(p.getFormattedName() + ": " + ChatColor.BOLD + songName);
                        songsDisplayed.add(playerUUID);
                    }
                }
                Bukkit.broadcastMessage("\n");
                for (int i = 0; i < firstPlaceWalkoutOrder.size(); i++) {
                    Participant p = firstPlaceWalkoutOrder.get(i);
                    UUID playerUUID = p.getPlayer().getUniqueId();
                    if (!songsDisplayed.contains(playerUUID)) {
                        String songName = playerWalkoutSongNames.get(playerUUID);
                        Bukkit.broadcastMessage(p.getFormattedName() + ": " + ChatColor.BOLD + songName);
                        songsDisplayed.add(playerUUID);
                    }
                }
                

            }
        } else if (getState().equals(GameState.STARTING)) {
            if (score[0] == 0 && score[1] == 0) {
                mapCreator(map.mapName, map.creatorName);
            }
            
            if (timeRemaining == 0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, "sfx.started_ring", SoundCategory.BLOCKS, 1, 1);
                }
                Barriers(false);
                setGameState(GameState.ACTIVE);
                timeRemaining = 3600;
            } else {
                if (timeRemaining == 16) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
                    }
                }
                startingCountdown("sfx.starting_beep");
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            createLineAll(20, ChatColor.RED.toString() + ChatColor.BOLD + "Time: " + ChatColor.RESET + getFormattedTime(timeElapsed));
            if (timeElapsed == timeUntilGlowing) {
                for (Participant p : firstPlace.teamPlayers) {
                    if (!p.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
                        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 2, false, false));
                }
                for (Participant p : secondPlace.teamPlayers) {
                    if (!p.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
                        p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 2, false, false));
                }
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + ChatColor.BOLD + " All players are now glowing!");
            }
            else if (timeElapsed%217 == 201) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
                }
            }
            timeElapsed++;
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                resetMap();
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 10;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 0) {
                returnToLobby();
            }
        }
    }

    public void playerIntroOrder() {
        
        List<Participant> firstPlaceParticipants = firstPlace.getPlayers();
        Set<Participant> set1 = new HashSet<>(firstPlaceParticipants);
        firstPlaceParticipants.clear();
        firstPlaceParticipants.addAll(set1);

        List<Participant> secondPlaceParticipants = secondPlace.getPlayers();
        Set<Participant> set2 = new HashSet<>(secondPlaceParticipants);
        secondPlaceParticipants.clear();
        secondPlaceParticipants.addAll(set2);


        for (int i = 0; i < firstPlaceParticipants.size(); i++) {
            firstPlaceWalkoutOrder.add(firstPlaceParticipants.get(i));
        }
        for (int i = 0; i < secondPlaceParticipants.size(); i++) {
            secondPlaceWalkoutOrder.add(secondPlaceParticipants.get(i));
        }

        Collections.shuffle(firstPlaceWalkoutOrder);
        Collections.shuffle(secondPlaceWalkoutOrder);
    }

    public void startRound() {
        Barriers(true);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            resetLine(p, 21);
            p.getInventory().clear();
            p.getPlayer().setInvulnerable(false);
            if (p.getTeam().equals(firstPlace)) {
                p.getPlayer().setMaxHealth(8);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getInventory().addItem(CROSSBOW);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
                p.getInventory().setBoots(firstPlace.getColoredLeatherArmor(BOOTS));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(TEAM_ONE_SPAWN);
                p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
                p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            } else if (p.getTeam().equals(secondPlace)) {
                p.getPlayer().setMaxHealth(8);
                p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
                p.getInventory().addItem(CROSSBOW);
                p.getInventory().addItem(new ItemStack(Material.ARROW,64));
                p.getInventory().setBoots(secondPlace.getColoredLeatherArmor(BOOTS));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().teleport(TEAM_TWO_SPAWN);
                p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
                p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            } else {
                p.getPlayer().setGameMode(GameMode.SPECTATOR);
                p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            }
        }
    }

    private void endRound(MBCTeam winner) {
        for (Arrow a : world.getEntitiesByClass(Arrow.class)) {
            a.remove();
        }

        timeElapsed = 0;
        Bukkit.broadcastMessage("\n"+winner.teamNameFormat() + " have won the round!\n");
        logger.log(winner.getTeamName() + " have won the round!\n");
        createScoreboard();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.setInvulnerable(true);
            p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
        }
        playersAlive[0] = firstPlace.teamPlayers.size();
        playersAlive[1] = secondPlace.teamPlayers.size();
        createLineAll(21, ChatColor.RED.toString() + ChatColor.BOLD + "Next Round:");
        roundNum++;
        if (disconnect) {
            Bukkit.broadcastMessage("Event Paused!");
            setGameState(GameState.PAUSED);
        } else {
            setGameState(GameState.END_ROUND);
            setTimer(6);
        }
    }

    @EventHandler
    public void onArrowShoot(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();

        if (shooter instanceof Player) {
            Player player = (Player) shooter;

            ItemStack arrow = new ItemStack(Material.ARROW);
            player.getInventory().addItem(arrow);
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        event.getArrow().remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void arrowHit(ProjectileHitEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        if (!(e.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;

        if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
            Participant shot = Participant.getParticipant((Player) e.getHitEntity());
            Participant damager = Participant.getParticipant((Player) arrow.getShooter());

            if (damager.getTeam().equals(shot.getTeam())) return;

            QuickfirePlayer damagerPlayer = getQuickfirePlayer(damager.getPlayer());
            QuickfirePlayer shotPlayer = getQuickfirePlayer(shot.getPlayer());

            damager.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(shot.getFormattedName() + " - " + ChatColor.RED + ((int)(shot.getPlayer().getHealth()-2))/2+ " ♥"));
            damager.getPlayer().playSound(damager.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            damagerPlayer.incrementDamageDealt();
            shotPlayer.incrementDamageTaken();
            if (shot.getPlayer().getHealth() <= 2) {
                shotPlayer.incrementDeaths();
                damagerPlayer.incrementKills();
                Death(shot, damager);
                arrow.remove();
            } else {
                shot.getPlayer().damage(2);
                shot.getPlayer().setVelocity(new Vector(arrow.getVelocity().getX()*0.15, 0.3, arrow.getVelocity().getZ()*0.15));
                arrow.remove();
                logger.log(shotPlayer.getPlayer().getName() + " was shot by " + damager.getPlayerName());
            }
        }
    }

    private void Death(Participant victim, Participant killer) {
        MBC.spawnFirework(victim);
        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        killer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(victim.getFormattedName() + " - " + ChatColor.RED + "0 ♥"));
        Bukkit.broadcastMessage(victim.getFormattedName() + " was shot by " + killer.getFormattedName());
        logger.log(victim.getPlayerName() + " was shot and killed by " + killer.getPlayerName());
        victim.getPlayer().removePotionEffect(PotionEffectType.GLOWING);

        if (victim.getTeam().equals(firstPlace)) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
                // TODO, possibly: set spectator to another player on their team
            } else {
                score[1]++;
                if (score[1] == 3) endGame(secondPlace);
                else endRound(secondPlace);
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
                // TODO, possibly: set spectator to another player on their team
            } else {
                score[0]++;
                if (score[0] == 3) endGame(firstPlace);
                else endRound(firstPlace);
            }
        }
    }

    private void endGame(MBCTeam t) {
        Bukkit.broadcastMessage("\n" + t.teamNameFormat() + " win MBC!\n");
        logger.log(t.getTeamName() + " win MBC!\n");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.GLOWING);
            createScoreboard();
            p.sendTitle(t.teamNameFormat() + " win MBC!", " ", 0, 100, 20);
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.playSound(p, "sfx.winners_jingle", SoundCategory.BLOCKS, 1, 1);
            p.setInvulnerable(true);
        }

        for (Participant p : t.teamPlayers) {
            p.winner = true;
        }
        logger.logStats();

        setGameState(GameState.END_GAME);
        createLineAll(21, ChatColor.RED.toString()+ChatColor.BOLD+"Back to lobby:");
        setTimer(13);
    }

    private void returnToLobby() {
        HandlerList.unregisterAll(this);
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.board.getTeam(firstPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            p.board.getTeam(secondPlace.fullName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            p.getPlayer().setMaxHealth(20);
            p.getPlayer().setHealth(20);
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

        MBC.getInstance().lobby.end();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (p == null) return;
        if (!(p.getTeam().equals(firstPlace) && p.getTeam().equals(secondPlace))) return;

        disconnect = true;
        Bukkit.broadcastMessage(p.getFormattedName() + " disconnected!");
        if (p.getTeam().equals(firstPlace)) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
            } else {
                score[1]++;
                if (score[1] == 3) endGame(secondPlace);
                else endRound(secondPlace);
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
            } else {
                score[0]++;
                if (score[0] == 3) endGame(firstPlace);
                else endRound(firstPlace);
            }
        }
    }

    public void resetMap() {
        Barriers(true);
        for (Arrow a : world.getEntitiesByClass(Arrow.class)) {
            a.remove();
        }
    }

    @Override
    public void Unpause() {
        disconnect = false;
        setGameState(GameState.END_ROUND);
        setTimer(6);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (getState().equals(GameState.TUTORIAL) && e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
            e.setCancelled(true);
            return;
        }

        if (e.getPlayer().getLocation().distanceSquared(SPAWN) > MAX_DIST_FROM_CENTER) {
            e.getPlayer().teleport(SPAWN);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(e.getClickedBlock().getType() == Material.SPRUCE_TRAPDOOR || 
                e.getClickedBlock().getType() == Material.DARK_OAK_TRAPDOOR ||
                e.getClickedBlock().getType() == Material.OAK_TRAPDOOR) e.setCancelled(true);
        }
    }

    public void Barriers(boolean b) {
        map.resetBarriers(b);
    }

    private void changeColor() {
        map.changeColor(firstPlace, secondPlace);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }


    public QuickfirePlayer getQuickfirePlayer(Player p) {
        return quickfirePlayers.get(p.getUniqueId());
    }
    
   /**
     * Ensures boots are not taken off.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
    }

    //*
    //@EventHandler
    //public void sculkActivate(BlockReceiveGameEvent e) {
        //if(e.getEvent().equals(GameEvent.SCULK_SENSOR_TENDRILS_CLICKING) && e.getEntity() instanceof Player && getState().equals(GameState.ACTIVE)) {
            //Player p = (Player) e.getEntity();
            //if (getQuickfirePlayer(p) == null) return;
            //if (p.getPlayer().hasPotionEffect(PotionEffectType.GLOWING)) return;
            //p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 2, false, false));
            //p.sendMessage(ChatColor.RED + "You activated a sculk sensor and are now glowing - be careful!");
        //}
    //}
}
