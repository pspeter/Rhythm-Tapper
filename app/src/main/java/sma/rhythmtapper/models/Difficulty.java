package sma.rhythmtapper.models;

import java.io.Serializable;

/**
 * Created by andil on 24.01.2017.
 */

public class Difficulty implements Serializable{

    public static final String EASY_TAG = "easy";
    public static final String MED_TAG = "medium";
    public static final String HARD_TAG = "hard";

    private int _spawnInterval;
    private int _ballSpeed;
    private String _mode;

    public Difficulty(String _mode, int spawnInterval, int ballSpeed) {
        this._mode = _mode;
        this._spawnInterval = spawnInterval;
        this._ballSpeed = ballSpeed;
    }

    public int getSpawnInterval() {
        return _spawnInterval;
    }

    public void setSpawnInterval(int _spawnInterval) {
        this._spawnInterval = _spawnInterval;
    }

    public int getBallSpeed() {
        return _ballSpeed;
    }

    public void setBallSpeed(int _ballSpeed) {
        this._ballSpeed = _ballSpeed;
    }

    public String getMode() {
        return _mode;
    }

    public void setMode(String _mode) {
        this._mode = _mode;
    }
}
