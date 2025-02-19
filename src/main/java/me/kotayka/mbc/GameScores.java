package me.kotayka.mbc;

import java.util.Map;

public class GameScores {
    public String[] gameList = new String[6];

    public Map<Participant, Integer> gameOneScores;
    public Map<Participant, Integer> gameTwoScores;
    public Map<Participant, Integer> gameThreeScores;
    public Map<Participant, Integer> gameFourScores;
    public Map<Participant, Integer> gameFiveScores;
    public Map<Participant, Integer> gameSixScores;

    public GameScores() {

    }

    public void inputGame(String gameName, Map<Participant, Integer> scoreMap) {
        int gameNum = 0;
        while (gameList[gameNum] != null) {
            gameNum++;
            if (gameNum == gameList.length) return;
        }
        gameList[gameNum] = gameName;

        switch (gameNum) {
            case 0:
                gameOneScores = scoreMap;
                break;
            case 1:
                gameTwoScores = scoreMap;
                break;
            case 2:
                gameThreeScores = scoreMap;
                break;
            case 3:
                gameFourScores = scoreMap;
                break;
            case 4:
                gameFiveScores = scoreMap;
                break;
            case 5:
                gameSixScores = scoreMap;
                break;
        }
    }

    public Map<Participant, Integer> getRecentMap() {
        int gameNum = 0;
        while (gameList[gameNum] != null) {
            gameNum++;
            if (gameNum == gameList.length) return gameSixScores;
        }

        switch (gameNum) {
            case 0:
                return null;
            case 1:
                return gameOneScores;
            case 2:
                return gameTwoScores;
            case 3:
                return gameThreeScores;
            case 4:
                return gameFourScores;
            case 5:
                return gameFiveScores;
            default:
                return null;
        }
    }

    public String getRecentString() {
        int gameNum = 0;
        while (gameList[gameNum] != null) {
            gameNum++;
            if (gameNum == gameList.length) return gameList[5];
        }
        if (gameNum == 0) return null;
        return gameList[gameNum-1];
    }
}
