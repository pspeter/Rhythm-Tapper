package sma.rhythmtapper.game;

import sma.rhythmtapper.framework.Game;
import sma.rhythmtapper.framework.Graphics;
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.models.Difficulty;

/**
 * Created by Peter on 23.01.2017.
 */

public class LoadingScreen extends Screen {
    private Difficulty _diff;
    public LoadingScreen(Game game, Difficulty difficulty) {
        super(game);
        this._diff = difficulty;
    }


    @Override
    public void update(float deltaTime) {
        Graphics g = game.getGraphics();
        Assets.background = g.newImage("background_placeholder.jpg", Graphics.ImageFormat.RGB565);
        Assets.ballNormal = g.newImage("ball_placeholder.png", Graphics.ImageFormat.RGB565);
        Assets.ballMultiplier = g.newImage("ball_multiplier.png", Graphics.ImageFormat.RGB565);
        Assets.ballOneUp = g.newImage("ball_oneup.png", Graphics.ImageFormat.RGB565);
        Assets.sirens = g.newImage("sirens.png", Graphics.ImageFormat.RGB565);
        Assets.click = game.getAudio().createSound("guiclick.ogg");
        game.setScreen(new GameScreen(game, _diff));
    }
    @Override
    public void paint(float deltaTime) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void backButton() {

    }
}
