package sma.rhythmtapper.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import sma.rhythmtapper.framework.Game;
import sma.rhythmtapper.framework.Graphics;
import sma.rhythmtapper.framework.Image;
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.framework.Input.TouchEvent;

public class GameScreen extends Screen {
    private static final String TAG = "GameScreenTag";
    enum GameState {
        Ready, Running, Paused, GameOver
    }
    private int _gameHeight;
    private int _gameWidth;
    private int _score;
    private int _multiplier;
    private int _streak;
    private List<Point> _points;
    private Random _rand;
    private int _tick;

    GameState state = GameState.Ready;

    // Variable Setup
    // You would create game objects here.

    private Paint paint;

    public GameScreen(Game game) {
        super(game);

        // Initialize game objects
        _gameHeight = game.getGraphics().getHeight();
        _gameWidth = game.getGraphics().getWidth();
        _multiplier = 1;
        _score = 0;
        _streak = 0;
        _points = new ArrayList<>();
        _rand = new Random(42);
        _tick = 0;

        // Defining a paint object
        paint = new Paint();
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

        // We have four separate update methods in this example.
        // Depending on the state of the game, we call different update methods.
        // Refer to Unit 3's code. We did a similar thing without separating the
        // update methods.

        if (state == GameState.Ready)
            updateReady(touchEvents);
        if (state == GameState.Running)
            updateRunning(touchEvents, deltaTime);
        if (state == GameState.Paused)
            updatePaused(touchEvents);
        if (state == GameState.GameOver)
            updateGameOver(touchEvents);
    }

    private void updateReady(List<TouchEvent> touchEvents) {

        // This example starts with a "Ready" screen.
        // When the user touches the screen, the game begins.
        // state now becomes GameState.Running.
        // Now the updateRunning() method will be called!

        if (touchEvents.size() > 0)
            state = GameState.Running;
    }

    private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {

        //This is identical to the update() method from our Unit 2/3 game.
        spawnPoints();
        movePoints();


        // 1. All touch input is handled here:
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);

            if (event.type == TouchEvent.TOUCH_DOWN) {
                if (event.x < _gameWidth / 3) {
                    hitLane(0, _gameWidth / 3);
                }
                else if (event.x < _gameWidth / 3 * 2) {
                    hitLane(_gameWidth / 3, _gameWidth / 3 * 2);
                }
                else {
                    hitLane(_gameWidth / 3 * 2, _gameWidth);
                }
            }
        }

        Iterator<Point> iter = _points.iterator();
        while (iter.hasNext()) {
            Point p = iter.next();
            if (p.y > 1880) {
                iter.remove();
                Log.d(TAG, "point missed");
                // TODO Success
            }
        }

        // 2. Check miscellaneous events like death:

        //if (livesLeft == 0) {
        //    state = GameState.GameOver;
        //}


        // 3. Call individual update() methods here.
        // This is where all the game updates happen.
        // For example, robot.update();

        _tick = (_tick + 1) % 30;
    }

    private void hitLane(int from, int to) {
        boolean hasHit = false;
        Iterator<Point> iter = _points.iterator();
        while (iter.hasNext()) {
            Point p = iter.next();
            if (p.x > from && p.x < to) {
                if (p.y > 1750) {
                    iter.remove();
                    hasHit = true;
                    Log.d(TAG, "point hit");
                    // TODO failure
                }
            }
        }
        if (!hasHit) {
            Log.d(TAG, "no point pressed");
            // TODO failure
        }
    }

    private void movePoints() {
        for(Point p: _points) {
            p.y += 10;
        }
    }

    private void spawnPoints() {
        if (_tick == 0) {
            if (_rand.nextFloat() < 0.4) {
                _points.add(new Point(_gameWidth / 3 / 2, 50));
            }
            if (_rand.nextFloat() < 0.4) {
                _points.add(new Point(_gameWidth / 2, 50));
            }
            if (_rand.nextFloat() < 0.4) {
                _points.add(new Point(_gameWidth - _gameWidth / 3 / 2, 50));
            }
        }
    }

    private void updatePaused(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                resume();
            }
        }
    }

    private void updateGameOver(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (event.x > 300 && event.x < 980 && event.y > 100
                        && event.y < 500) {
                    nullify();
                    // game.setScreen(new MainMenuScreen(game)); TODO mainmenu, highscore update
                    return;
                }
            }
        }

    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();

        // First draw the game elements.

        // Example:
        g.drawImage(Assets.background, 0, 0);
        // g.drawImage(Assets.character, characterX, characterY);

        for (Point p: _points) {
            g.drawImage(Assets.ball, p.x - 90, p.y - 90);
        }

        // Secondly, draw the UI above the game elements.
        if (state == GameState.Ready)
            drawReadyUI();
        if (state == GameState.Running)
            drawRunningUI();
        if (state == GameState.Paused)
            drawPausedUI();
        if (state == GameState.GameOver)
            drawGameOverUI();

    }

    private void nullify() {

        // Set all variables to null. You will be recreating them in the
        // constructor.
        paint = null;

        // Call garbage collector to clean up memory.
        System.gc();
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        g.drawARGB(155, 0, 0, 0);
        g.drawString(Integer.toString(_score), 640, 300, paint);

    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();

    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        // Darken the entire screen so you can display the Paused screen.
        g.drawARGB(155, 0, 0, 0);

    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, 1281, 801, Color.BLACK);
        g.drawString("GAME OVER.", 640, 300, paint);

    }

    @Override
    public void pause() {
        if (state == GameState.Running)
            state = GameState.Paused;

    }

    @Override
    public void resume() {
        if (state == GameState.Paused)
            state = GameState.Running;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void backButton() {
        pause();
    }
}
