package sma.rhythmtapper.game.models;

/**
 * Created by Peter on 24.01.2017.
 */

public class Ball {
    public enum BallType {
        Normal, OneUp, Multiplier, Speeder, Skull
    }

    public int x;
    public int y;
    public BallType type;
    private double speedMultiplier;

    public Ball(int x, int y, BallType type){
        this.x = x;
        this.y = y;
        this.type = type;
        this.speedMultiplier = type == BallType.Speeder ? 1.4 : 1;
    }

    public void update(int speed) {
        this.y += speed * speedMultiplier;
    }
}
