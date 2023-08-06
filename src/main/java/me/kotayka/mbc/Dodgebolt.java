package me.kotayka.mbc;

public class Dodgebolt extends Minigame {
    private final MBCTeam firstPlace;
    private final MBCTeam secondPlace;

    public Dodgebolt() {
        super("Dodgebolt");

        firstPlace = MBC.getInstance().red;
        secondPlace = MBC.getInstance().yellow;
    }

    public Dodgebolt(MBCTeam firstPlace, MBCTeam secondPlace) {
        super("Dodgebolt");
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
    }

    @Override
    public void start() {
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);

    }

    @Override
    public void loadPlayers() {

    }

    @Override
    public void events() {

    }

    @Override
    public void createScoreboard(Participant p) {

    }
}
