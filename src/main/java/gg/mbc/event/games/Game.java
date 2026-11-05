package gg.mbc.event.games;

import gg.mbc.event.scheduling.EventStage;
import gg.mbc.event.scoring.ScoreManager;

public abstract class Game implements EventStage {
    private ScoreManager scoreManager;
}
