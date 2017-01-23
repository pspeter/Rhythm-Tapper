package sma.rhythmtapper.game;

import sma.rhythmtapper.framework.Game;
import sma.rhythmtapper.framework.Graphics;
import sma.rhythmtapper.framework.Screen;

/**
 * Created by Peter on 23.01.2017.
 */

public class LoadingScreen extends Screen {
    public LoadingScreen(Game game) {
        super(game);
    }


    @Override
    public void update(float deltaTime) {
        Graphics g = game.getGraphics();
        Assets.background = g.newImage("background_placeholder.jpg", Graphics.ImageFormat.RGB565);
        Assets.ball = g.newImage("ball_placeholder.jpg", Graphics.ImageFormat.RGB565);
        Assets.click = game.getAudio().createSound("guiclick.ogg");
        //game.setScreen();


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
