package sma.rhythmtapper.models;

import java.io.Serializable;

import sma.rhythmtapper.framework.Music;

public class Difficulty implements Serializable{

    public static final String EASY_TAG = "easy";
    public static final String MED_TAG = "medium";
    public static final String HARD_TAG = "hard";



    private String _music;
    private float _spawnInterval;
    private int _ballSpeed;
    private String _mode;

    public Difficulty(String _mode, String music, float spawnInterval, int ballSpeed) {
        this._mode = _mode;
        this._music = music;
        this._spawnInterval = spawnInterval;
        this._ballSpeed = ballSpeed;
    }

    public String getMusic() {
        return _music;
    }

    public void setMusic(String _music) {
        this._music = _music;
    }

    public float getSpawnInterval() {
        return _spawnInterval;
    }

    public void setSpawnInterval(float _spawnInterval) {
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
