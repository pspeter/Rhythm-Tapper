package sma.rhythmtapper.framework;

import android.os.Vibrator;

/**
 * Created by Peter on 23.01.2017.
 */

public interface Game {
    public Audio getAudio();

    public Input getInput();

    public FileIO getFileIO();

    public Graphics getGraphics();

    public Vibrator getVibrator();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getInitScreen();

    public void goToActivity(Class<?> activity);
}
