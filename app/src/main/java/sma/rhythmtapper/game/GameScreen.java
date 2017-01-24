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
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.framework.Input.TouchEvent;
import sma.rhythmtapper.game.models.Ball;

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
    private List<Ball> _ballsLeft;
    private List<Ball> _ballsMiddle;
    private List<Ball> _ballsRight;
    private Random _rand;
    private int _tick;
    private int _lifes;

    private final double _spawnChance_normal = 0.17;
    private final double _spawnChance_oneup = _spawnChance_normal + 0.005;
    private final int _spawnInterval = 10;
    private final int _ballSpeed = 20;

    GameState state = GameState.Ready;

    // Variable Setup
    // You would create game objects here.

    private Paint _paint;

    public GameScreen(Game game) {
        super(game);

        // Initialize game objects
        _gameHeight = game.getGraphics().getHeight();
        _gameWidth = game.getGraphics().getWidth();
        _multiplier = 1;
        _score = 0;
        _streak = 0;
        _ballsLeft = new ArrayList<>();
        _ballsMiddle = new ArrayList<>();
        _ballsRight = new ArrayList<>();
        _rand = new Random(42);
        _tick = 0;
        _lifes = 10;

        // Defining a paint object
        _paint = new Paint();
        _paint.setTextSize(30);
        _paint.setTextAlign(Paint.Align.CENTER);
        _paint.setAntiAlias(true);
        _paint.setColor(Color.WHITE);
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

        // 1. All touch input is handled here:
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);

            if (event.type == TouchEvent.TOUCH_DOWN) {
                if (event.y > 1500) {
                    if (event.x < _gameWidth / 3) {
                        hitLane(_ballsLeft.iterator());
                    }
                    else if (event.x < _gameWidth / 3 * 2) {

                        hitLane(_ballsMiddle.iterator()); // TODO triggers on every game start
                    }
                    else {
                        hitLane(_ballsRight.iterator());
                    }
                }
                else {
                    touchEvents.remove(i);
                    pause();
                }
            }
        }

        removeMissed(_ballsLeft.iterator());
        removeMissed(_ballsMiddle.iterator());
        removeMissed(_ballsRight.iterator());

        // 2. Check miscellaneous events like death:

        if (_lifes == 0) {
            state = GameState.GameOver;
        }

        // 3. Call individual update() methods here.
        // This is where all the game updates happen.
        // For example, robot.update();

        // update ball position
        for (Ball b: _ballsLeft)
            b.update();

        for (Ball b: _ballsMiddle)
            b.update();

        for (Ball b: _ballsRight)
            b.update();

        // spawn new balls
        if (_tick == 0) {
            spawnPoints();
        }

        // update spawntime ticker
        _tick = (_tick + 1) % _spawnInterval;
    }

    private void removeMissed(Iterator<Ball> iterator) {
        while (iterator.hasNext()) {
            Ball b = iterator.next();
            if (b.y > 1880) {
                iterator.remove();
                Log.d(TAG, "fail press");
                onMiss();
            }
        }
    }

    private void hitLane(Iterator<Ball> iter) {
        boolean hasHit = false;
        while (iter.hasNext()) {
            Ball b = iter.next();
            if (b.y > 1650) {
                if (b.type == Ball.BallType.OneUp) {
                    ++_lifes;
                }
                iter.remove();
                hasHit = true;
                Log.d(TAG, "point hit");
                onHit();
                break;
            }
        }
        if (!hasHit) {
            Log.d(TAG, "point missed");
            onMiss();
        }
    }

    private void onMiss() {
        _streak = 0;
        _score -= Math.min(_score, 50);
        _multiplier = 1;
        --_lifes;
    }

    private void onHit() {
        _streak++;
        if (_streak > 80) {
            _multiplier = 10;
        }
        else if (_streak > 40) {
            _multiplier = 5;
        }
        else if (_streak > 30) {
            _multiplier = 4;
        }
        else if (_streak > 20) {
            _multiplier = 3;
        }
        else if (_streak > 10) {
            _multiplier = 2;
        }

        _score += 10 * _multiplier;
    }

    private void spawnPoints() {
        float randFloat = _rand.nextFloat();
        if (randFloat < _spawnChance_normal) {
            _ballsLeft.add(new Ball(_gameWidth / 3 / 2, 50, Ball.BallType.Normal, _ballSpeed));
        } else if (randFloat < _spawnChance_oneup) {
            _ballsLeft.add(new Ball(_gameWidth / 3 / 2, 50, Ball.BallType.OneUp, _ballSpeed));
        }
        randFloat = _rand.nextFloat();
        if (randFloat < _spawnChance_normal) {
            _ballsMiddle.add(new Ball(_gameWidth / 2, 50, Ball.BallType.Normal, _ballSpeed));
        } else if (randFloat < _spawnChance_oneup) {
            _ballsMiddle.add(new Ball(_gameWidth / 3 / 2, 50, Ball.BallType.OneUp, _ballSpeed));
        }
        randFloat = _rand.nextFloat();
        if (randFloat < _spawnChance_normal) {
            _ballsRight.add(new Ball(_gameWidth - _gameWidth / 3 / 2, 50, Ball.BallType.Normal, _ballSpeed));
        } else if (randFloat < _spawnChance_oneup) {
            _ballsRight.add(new Ball(_gameWidth / 3 / 2, 50, Ball.BallType.OneUp, _ballSpeed));
        }
    }

    private void updatePaused(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_DOWN) {
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

        for (Ball b: _ballsLeft) {
            switch(b.type) {
                case Normal:
                    g.drawImage(Assets.ballNormal, b.x - 90, b.y - 90);
                    break;
                case OneUp:
                    g.drawImage(Assets.ballOneUp, b.x - 90, b.y - 90);
                    break;
            }
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
        _paint = null;

        // Call garbage collector to clean up memory.
        System.gc();
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        g.drawARGB(155, 0, 0, 0);
        g.drawString(Integer.toString(_score), 640, 300, _paint);

    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, _gameWidth, 100, Color.BLACK);

        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(_score)
                .append(" Multiplier: ").append(_multiplier).append("x")
                .append(" Lifes remaining: ").append(_lifes);
        g.drawString(sb.toString(), 600, 80, _paint);
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        // Darken the entire screen so you can display the Paused screen.
        g.drawARGB(155, 0, 0, 0);
        
    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, 1281, 801, Color.BLACK);
        g.drawString("GAME OVER.", 640, 300, _paint);
        g.drawString("FINAL SCORE: " + _score, 640, 600, _paint);
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
