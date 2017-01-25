package sma.rhythmtapper.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import sma.rhythmtapper.MainActivity;
import sma.rhythmtapper.framework.FileIO;
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
    private Difficulty _difficulty;
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
    private final double _spawnChance_speeder = _spawnChance_multiplier + 0.02;

    // ui
    private Paint _paintText;
    // constants
    // hitbox is the y-range within a ball can be hit by a press in its lane
    private static final int HITBOX_TOP = 1620;
    private static final int HITBOX_BOTTOM = 1900;
    // if no ball is in the hitbox when pressed, remove the lowest ball in the
    // miss zone right above the hitbox (it still counts as a miss)
    private static final int MISS_ZONE_HEIGHT = 150;
    private static final int MISS_FLASH_INITIAL_ALPHA = 240;
    private static final int DOUBLE_MULTIPLIER_TIME = 600;

    private GameState state = GameState.Ready;

    GameScreen(Game game, Difficulty difficulty) {
        super(game);

        _difficulty = difficulty;
        // init difficulty parameters
        _ballSpeed = _difficulty.getBallSpeed();
        _spawnInterval = _difficulty.getSpawnInterval();

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
                        if (!hitLane(_ballsLeft)) {
                            // if no ball was hit
                            _laneHitAlphaLeft = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else if (event.x < _gameWidth / 3 * 2) {
                        if (!hitLane(_ballsMiddle))
                        {
                            _laneHitAlphaMiddle = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else {
                        if (!hitLane(_ballsRight)) {
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
            Log.d("seas", "test game over");
            // update highscore
            FileIO fileIO = game.getFileIO();
            SharedPreferences prefs = fileIO.getSharedPref();
            int oldScore;

            switch(_difficulty.getMode()) {
                case Difficulty.EASY_TAG:
                    oldScore = prefs.getInt(Difficulty.EASY_TAG,0);
                    break;
                case Difficulty.MED_TAG:
                    oldScore = prefs.getInt(Difficulty.MED_TAG,0);
                    break;
                case Difficulty.HARD_TAG:
                    oldScore = prefs.getInt(Difficulty.HARD_TAG,0);
                    break;
                default:
                    oldScore = 0;
                    break;
            }

            if(_score > oldScore) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(_difficulty.getMode(), _score);
                editor.commit();
            }
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
        if (_tick % _spawnInterval == 0) {
            spawnBalls();
        }

        // decrease miss flash intensities
        _laneHitAlphaLeft -= Math.min(_laneHitAlphaLeft, 10);
        _laneHitAlphaMiddle -= Math.min(_laneHitAlphaMiddle, 10);
        _laneHitAlphaRight -= Math.min(_laneHitAlphaRight, 10);

        // decrease doubleMultiplierTicker
        _doubleMultiplierTicker -= Math.min(1, _doubleMultiplierTicker);

        // update spawntime ticker
        _tick = (_tick + 1) % 100000;
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

    private boolean hitLane(List<Ball> balls) {
        Iterator<Ball> iter = balls.iterator();
        Ball lowestBall = null;
        while (iter.hasNext()) {
            Ball b = iter.next();
            if (lowestBall == null || b.y > lowestBall.y) {
                lowestBall = b;
                Log.d(TAG, "point hit");
                onHit(b);
            }
        }

        if (lowestBall != null && lowestBall.y > HITBOX_TOP) {
            balls.remove(lowestBall);
            return true;
        } else {
            if (lowestBall != null && lowestBall.y > HITBOX_TOP - MISS_ZONE_HEIGHT) {
                balls.remove(lowestBall);
            }
            Log.d(TAG, "point missed");
            onMiss();
            return false;
        }
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
            balls.add(0, new Ball(ballX, ballY, Ball.BallType.Normal));
        } else if (randFloat < _spawnChance_oneup) {
            balls.add(0, new Ball(ballX, ballY, Ball.BallType.OneUp));
        } else if (randFloat < _spawnChance_multiplier) {
            balls.add(0, new Ball(ballX, ballY, Ball.BallType.Multiplier));
        } else if (randFloat < _spawnChance_speeder) {
            balls.add(0, new Ball(ballX, ballY, Ball.BallType.Speeder));
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
                    //nullify();
                    //this.backButton();
                    game.goToActivity(MainActivity.class);
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
            case Speeder:
                g.drawImage(Assets.ballSpeeder, b.x - 90, b.y - 90);
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
