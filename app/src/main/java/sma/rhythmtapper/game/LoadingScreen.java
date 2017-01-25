package sma.rhythmtapper.game;

import android.content.res.AssetManager;
import android.util.Log;

import sma.rhythmtapper.framework.Game;
import sma.rhythmtapper.framework.Graphics;
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.models.Difficulty;

/**
 * Created by Peter on 23.01.2017.
 */

public class LoadingScreen extends Screen {
    private Difficulty _diff;
    private static final String IMAGE_PATH = "img/";
    private static final String SOUND_EFFECTS_PATH = "audio/";
    private static final String Penis = "Penis";


    public LoadingScreen(Game game, Difficulty difficulty) {
        super(game);
        this._diff = difficulty;
    }


    @Override
    public void update(float deltaTime) {
        for (int i = 0; i < 1000; i++) Log.e("Test", Penis);
        Graphics g = game.getGraphics();

        Assets.background = g.newImage(IMAGE_PATH + "background_placeholder.jpg", Graphics.ImageFormat.RGB565);
        Assets.ballNormal = g.newImage(IMAGE_PATH + "ball_placeholder.png", Graphics.ImageFormat.RGB565);
        Assets.ballMultiplier = g.newImage(IMAGE_PATH + "ball_multiplier.png", Graphics.ImageFormat.RGB565);
        Assets.ballOneUp = g.newImage(IMAGE_PATH + "ball_oneup.png", Graphics.ImageFormat.RGB565);
        Assets.ballSpeeder = g.newImage(IMAGE_PATH + "ball_speeder.png", Graphics.ImageFormat.RGB565);
        Assets.ballBomb = g.newImage(IMAGE_PATH + "ball_bomb.png", Graphics.ImageFormat.RGB565);
        Assets.explosion = g.newImage(IMAGE_PATH + "explosion.png", Graphics.ImageFormat.RGB565);
        Assets.explosionBright = g.newImage(IMAGE_PATH + "explosion_bright.png", Graphics.ImageFormat.RGB565);
        Assets.ballSkull = g.newImage(IMAGE_PATH + "skull-ball-icon.png", Graphics.ImageFormat.RGB565);
        Assets.sirens = g.newImage(IMAGE_PATH + "sirens.png", Graphics.ImageFormat.RGB565);

        Assets.soundClick = game.getAudio().createSound(SOUND_EFFECTS_PATH + "sound_guiclick.ogg");
        Assets.soundExplosion = game.getAudio().createSound(SOUND_EFFECTS_PATH + "sound_explosion.ogg");
        Assets.soundCreepyLaugh = game.getAudio().createSound(SOUND_EFFECTS_PATH + "sound_creepy_laugh.mp3");

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
