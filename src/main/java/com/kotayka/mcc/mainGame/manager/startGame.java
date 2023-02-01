package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class startGame {
    public Map<String, Boolean> teamReadyMap = new HashMap<>();
    public int numOfteams = MCC.NUM_TEAMS-1;
    public int numOfTeamsReady = 0;
}
