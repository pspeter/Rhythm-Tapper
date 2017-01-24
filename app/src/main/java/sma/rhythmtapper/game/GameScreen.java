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
import sma.rhythmtapper.models.Difficulty;

public class GameScreen extends Screen {
    private static final String TAG = "GameScreenTag";
    enum GameState {
        Ready, Running, Paused, GameOver
    }

    // game params
    private int _gameHeight;
    private int _gameWidth;
    private Random _rand;
    private int _tick;
    // score
    private int _score;
    private int _multiplier;
    private int _streak;
    private int _doubleMultiplierTicker;
    // lifes
    private int _lifes;
    // balls
    private List<Ball> _ballsLeft;
    private List<Ball> _ballsMiddle;
    private List<Ball> _ballsRight;
    // lane miss indicators
    private int _laneHitAlphaLeft;
    private int _laneHitAlphaMiddle;
    private int _laneHitAlphaRight;
    // difficulty params
    private int _spawnInterval;
    private int _ballSpeed;
    private double _globalSpeedMultiplier;
    private final double _spawnChance_normal = 0.17; // TODO dynamic
    private final double _spawnChance_oneup = _spawnChance_normal + 0.001;
    private final double _spawnChance_multiplier = _spawnChance_oneup + 0.004;
    // ui
    private Paint _paintText;
    // const
    private static final int HITBOX_TOP = 1620;
    private static final int HITBOX_BOTTOM = 1900;
    private static final int MISS_FLASH_INITIAL_ALPHA = 240;
    private static final int DOUBLE_MULTIPLIER_TIME = 600;

    private GameState state = GameState.Ready;

    GameScreen(Game game, Difficulty difficulty) {
        super(game);

        // init difficulty parameters
        _ballSpeed = difficulty.getBallSpeed();
        _spawnInterval = difficulty.getSpawnInterval();

        // Initialize game objects
        _gameHeight = game.getGraphics().getHeight();
        _gameWidth = game.getGraphics().getWidth();
        _multiplier = 1;
        _doubleMultiplierTicker = 0;
        _score = 0;
        _streak = 0;
        _globalSpeedMultiplier = 1;
        _ballsLeft = new ArrayList<>();
        _ballsMiddle = new ArrayList<>();
        _ballsRight = new ArrayList<>();
        _rand = new Random();
        _tick = 0;
        _lifes = 10;
        _laneHitAlphaLeft = 0;
        _laneHitAlphaMiddle = 0;
        _laneHitAlphaRight = 0;

        // paint for text
        _paintText = new Paint();
        _paintText.setTextSize(30);
        _paintText.setTextAlign(Paint.Align.CENTER);
        _paintText.setAntiAlias(true);
        _paintText.setColor(Color.WHITE);
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

        if (touchEvents.size() > 0) {
            state = GameState.Running; // TODO triggers pause on every game start
            touchEvents.clear();
        }
    }

    private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {

        // 1. All touch input is handled here:
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);

            if (event.type == TouchEvent.TOUCH_DOWN) {
                if (event.y > 1500) {
                    // ball hit area
                    if (event.x < _gameWidth / 3) {
                        if (!hitLane(_ballsLeft.iterator())) {
                            // if no ball was hit
                            _laneHitAlphaLeft = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else if (event.x < _gameWidth / 3 * 2) {
                        if (!hitLane(_ballsMiddle.iterator()))
                        {
                            _laneHitAlphaMiddle = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else {
                        if (!hitLane(_ballsRight.iterator())) {
                            _laneHitAlphaRight = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                }
                else {
                    // pause area
                    touchEvents.remove(i);
                    pause();
                }
            }
        }

        // 2. Check miscellaneous events like death:

        if (_lifes <= 0) {
            state = GameState.GameOver;
        }

        // 3. Call individual update() methods here.
        // This is where all the game updates happen.
        // For example, robot.update();

        // update ball position
        for (Ball b: _ballsLeft) {
            b.update((int) (_ballSpeed * _globalSpeedMultiplier));
        }

        for (Ball b: _ballsMiddle) {
            b.update((int) (_ballSpeed * _globalSpeedMultiplier));
        }

        for (Ball b: _ballsRight) {
            b.update((int) (_ballSpeed * _globalSpeedMultiplier));
        }

        // remove missed balls
        if (removeMissed(_ballsLeft.iterator())) {
            _laneHitAlphaLeft = MISS_FLASH_INITIAL_ALPHA;
        }

        if (removeMissed(_ballsMiddle.iterator())) {
            _laneHitAlphaMiddle = MISS_FLASH_INITIAL_ALPHA;
        }

        if (removeMissed(_ballsRight.iterator())) {
            _laneHitAlphaRight = MISS_FLASH_INITIAL_ALPHA;
        }

        // spawn new balls
        if (_tick == 0) {
            spawnBalls();
        }

        // decrease miss flash intensities
        _laneHitAlphaLeft -= Math.min(_laneHitAlphaLeft, 10);
        _laneHitAlphaMiddle -= Math.min(_laneHitAlphaMiddle, 10);
        _laneHitAlphaRight -= Math.min(_laneHitAlphaRight, 10);

        // decrease doubleMultiplierTicker
        _doubleMultiplierTicker -= Math.min(1, _doubleMultiplierTicker);

        // update spawntime ticker
        _tick = (_tick + 1) % _spawnInterval;
    }

    private boolean removeMissed(Iterator<Ball> iterator) {
        while (iterator.hasNext()) {
            Ball b = iterator.next();
            if (b.y > HITBOX_BOTTOM) {
                iterator.remove();
                Log.d(TAG, "fail press");
                onMiss();
                return true;
            }
        }
        return false;
    }

    private boolean hitLane(Iterator<Ball> iter) {
        boolean hasHit = false;
        while (iter.hasNext()) {
            Ball b = iter.next();
            if (b.y > HITBOX_TOP) {
                iter.remove();
                hasHit = true;
                Log.d(TAG, "point hit");
                onHit(b);
                break; // only hit & remove the first one
            }
        }
        if (!hasHit) {
            Log.d(TAG, "point missed");
            onMiss();
        }
        return hasHit;
    }

    private void onMiss() {
        _streak = 0;
        _score -= Math.min(_score, 50);
        _multiplier = 1;
        _globalSpeedMultiplier = 1;
        --_lifes;
    }

    private void onHit(Ball b) {
        _streak++;
        if (b.type == Ball.BallType.OneUp) {
            ++_lifes;
        }
        else if (b.type == Ball.BallType.Multiplier) {
            _doubleMultiplierTicker = DOUBLE_MULTIPLIER_TIME;
        }
        updateMultipliers();
        _score += 10 * _multiplier
                * (_doubleMultiplierTicker > 0 ? 2 : 1);
    }

    private void updateMultipliers() {
        if (_streak > 80) {
            _multiplier = 10;
            _globalSpeedMultiplier = 1.64;
        }
        else if (_streak > 40) {
            _multiplier = 5;
            _globalSpeedMultiplier = 1.32;
        }
        else if (_streak > 30) {
            _multiplier = 4;
            _globalSpeedMultiplier = 1.24;
        }
        else if (_streak > 20) {
            _multiplier = 3;
            _globalSpeedMultiplier = 1.16;
        }
        else if (_streak > 10) {
            _multiplier = 2;
            _globalSpeedMultiplier = 1.08;
        }
    }

    private void spawnBalls() {
        float randFloat = _rand.nextFloat();
        final int ballY = 0;
        int ballX = _gameWidth / 3 / 2;
        spawnBall(_ballsLeft, randFloat, ballX, ballY);

        randFloat = _rand.nextFloat();
        ballX = _gameWidth / 2;
        spawnBall(_ballsMiddle, randFloat, ballX, ballY);

        randFloat = _rand.nextFloat();
        ballX = _gameWidth - _gameWidth / 3 / 2;
        spawnBall(_ballsRight, randFloat, ballX, ballY);

    }

    private void spawnBall(List<Ball> balls, float randFloat, int ballX, int ballY) {
        if (randFloat < _spawnChance_normal) {
            balls.add(new Ball(ballX, ballY, Ball.BallType.Normal));
        } else if (randFloat < _spawnChance_oneup) {
            balls.add(new Ball(ballX, ballY, Ball.BallType.OneUp));
        } else if (randFloat < _spawnChance_multiplier) {
            balls.add(new Ball(ballX, ballY, Ball.BallType.Multiplier));
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
        g.drawRect(0                 , 0, _gameWidth / 3 + 1, _gameHeight, Color.argb(_laneHitAlphaLeft, 255, 0, 0));
        g.drawRect(_gameWidth / 3    , 0, _gameWidth / 3 + 1, _gameHeight, Color.argb(_laneHitAlphaMiddle, 255, 0, 0));
        g.drawRect(_gameWidth / 3 * 2, 0, _gameWidth / 3 + 1, _gameHeight, Color.argb(_laneHitAlphaRight, 255, 0, 0));
        // g.drawImage(Assets.character, characterX, characterY);

        for (Ball b: _ballsLeft) {
            paintBall(g, b, deltaTime);
        }

        for (Ball b: _ballsMiddle) {
            paintBall(g, b, deltaTime);
        }

        for (Ball b: _ballsRight) {
            paintBall(g, b, deltaTime);
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

    private void paintBall(Graphics g, Ball b, float deltaTime) {
        switch(b.type) {
            case Normal:
                g.drawImage(Assets.ballNormal, b.x - 90, b.y - 90); // TODO deltatime for framerate independent movement(?)
                break;
            case OneUp:
                g.drawImage(Assets.ballOneUp, b.x - 90, b.y - 90);
                break;
            case Multiplier:
                g.drawImage(Assets.ballMultiplier, b.x - 90, b.y - 90);
                break;
        }
    }

    private void nullify() {

        // Set all variables to null. You will be recreating them in the
        // constructor.
        _paintText = null;

        // Call garbage collector to clean up memory.
        System.gc();
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        g.drawARGB(155, 0, 0, 0);
        g.drawString(Integer.toString(_score), 640, 300, _paintText);

    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();

        if (_doubleMultiplierTicker > 0) {
            g.drawImage(Assets.sirens, 0, 100);
        }

        g.drawRect(0, 0, _gameWidth, 100, Color.BLACK);

        String s = "Score: " + _score +
                "   Multiplier: " + _multiplier * (_doubleMultiplierTicker > 0 ? 2 : 1) + "x" +
                "   Lifes remaining: " + _lifes;
        g.drawString(s, 600, 80, _paintText);
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        // Darken the entire screen so you can display the Paused screen.
        g.drawARGB(155, 0, 0, 0);
    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawRect(0, 0, 1281, 801, Color.BLACK);
        g.drawString("GAME OVER.", 640, 300, _paintText);
        g.drawString("FINAL SCORE: " + _score, 640, 600, _paintText);
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
