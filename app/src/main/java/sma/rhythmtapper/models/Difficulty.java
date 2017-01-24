package sma.rhythmtapper.models;

import java.io.Serializable;

/**
 * Created by andil on 24.01.2017.
 */

public class Difficulty implements Serializable{
    private int _spawnInterval;
    private int _ballSpeed;

    public Difficulty(int spawnInterval, int ballSpeed) {
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
}
