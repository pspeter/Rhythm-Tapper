package sma.rhythmtapper.game.models;

/**
 * Created by Peter on 24.01.2017.
 */

public class Ball {
    public enum BallType {
        Normal, OneUp
    }

    public int x;
    public int y;
    public BallType type;

    public Ball(int x, int y, BallType type){
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
