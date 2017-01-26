package sma.rhythmtapper.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Vibrator;
import android.util.Log;

import sma.rhythmtapper.MainActivity;
import sma.rhythmtapper.framework.FileIO;
import sma.rhythmtapper.framework.Game;
import sma.rhythmtapper.framework.Graphics;
import sma.rhythmtapper.framework.Input;
import sma.rhythmtapper.framework.Music;
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.framework.Input.TouchEvent;
import sma.rhythmtapper.game.models.Ball;
import sma.rhythmtapper.models.Difficulty;

public class GameScreen extends Screen {
    private static final String TAG = "GameScreenTag";
    enum GameState {
        Ready, Running, Paused, GameOver
    }

    // game and device
    private int _gameHeight;
    private int _gameWidth;
    private Random _rand;
    private Difficulty _difficulty;
    private int _lifes;
    private Vibrator _vibrator;

    // score
    private int _score;
    private int _multiplier;
    private int _streak;

    // tickers
    private int _tick;
    private int _doubleMultiplierTicker;
    private int _explosionTicker;
    private float _currentTime;

    // balls
    private List<Ball> _ballsLeft;
    private List<Ball> _ballsMiddle;
    private List<Ball> _ballsRight;

    // lane miss indicators
    private int _laneHitAlphaLeft;
    private int _laneHitAlphaMiddle;
    private int _laneHitAlphaRight;

    // difficulty params
    private float _spawnInterval;
    private int _ballSpeed;
    private final double _spawnChance_normal = 0.26; // TODO dynamic
    private final double _spawnChance_oneup = _spawnChance_normal + 0.003;
    private final double _spawnChance_multiplier = _spawnChance_oneup + 0.001;
    private final double _spawnChance_speeder = _spawnChance_multiplier + 0.003;
    private final double _spawnChance_bomb = _spawnChance_speeder + 0.0005;
    private final double _spawnChance_skull = _spawnChance_bomb + 0.014;

    // audio
    private Music _currentTrack;

    // ui
    private Paint _paintText;

    // constants
    // initial y coordinate of spawned balls
    private static final int BALL_INITIAL_Y = 0;
    // hitbox is the y-range within a ball can be hit by a press in its lane
    private static final int HITBOX_CENTER = 1760;
    private static final int HITBOX_HEIGHT = 280;
    // if no ball is in the hitbox when pressed, remove the lowest ball in the
    // miss zone right above the hitbox (it still counts as a miss)
    private static final int MISS_ZONE_HEIGHT = 150;
    private static final int MISS_FLASH_INITIAL_ALPHA = 240;
    private static final int DOUBLE_MULTIPLIER_TIME = 600;
    // explosion
    private static final int EXPLOSION_TOP = 600;
    private static final int EXPLOSION_TIME = 150;

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
        _vibrator = game.getVibrator();
        _multiplier = 1;
        _doubleMultiplierTicker = 0;
        _score = 0;
        _streak = 0;
        _ballsLeft = new ArrayList<>();
        _ballsMiddle = new ArrayList<>();
        _ballsRight = new ArrayList<>();
        _rand = new Random();
        _tick = 0;
        _currentTime = 0f;
        _explosionTicker = 0;
        _lifes = 10;
        _laneHitAlphaLeft = 0;
        _laneHitAlphaMiddle = 0;
        _laneHitAlphaRight = 0;
        _currentTrack = Assets.musicTrack;

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
        if (touchEvents.size() > 0) {
            state = GameState.Running; // TODO triggers pause on every game start
            touchEvents.clear();
            _currentTrack.setLooping(true);
            _currentTrack.setVolume(0.3f);
            _currentTrack.play();
        }
    }

    private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
        // 1. All touch input is handled here:
        handleTouchEvents(touchEvents);

        // 2. Check miscellaneous events like death:
        checkDeath();

        // 3. Individual update() methods.
        updateVariables(deltaTime);
    }

    private void explosion(List<Ball> balls) {
        Iterator<Ball> iter = balls.iterator();
        while (iter.hasNext()) {
            Ball b = iter.next();
            if (b.y > EXPLOSION_TOP) {
                iter.remove();
                _score += 10 * _multiplier
                        * (_doubleMultiplierTicker > 0 ? 2 : 1);
            }
        }
    }

    private void checkDeath() {
        if (_lifes <= 0) {
            state = GameState.GameOver;
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
                editor.apply();
            }
        }
    }

    private void handleTouchEvents(List<TouchEvent> touchEvents) {
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
    }

    // update all the games variables each tick
    private void updateVariables(float deltatime) {
        // update timer
        _currentTime += deltatime;

        // update ball position
        for (Ball b: _ballsLeft) {
            b.update((int) (_ballSpeed * deltatime));
        }

        for (Ball b: _ballsMiddle) {
            b.update((int) (_ballSpeed * deltatime));
        }

        for (Ball b: _ballsRight) {
            b.update((int) (_ballSpeed * deltatime));
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
        if (_currentTime % _spawnInterval <= deltatime) {
            Log.d("spawntimer", "" + _currentTime + " " + _spawnInterval + " " + deltatime);
            spawnBalls();
        }

        // decrease miss flash intensities
        _laneHitAlphaLeft -= Math.min(_laneHitAlphaLeft, 10);
        _laneHitAlphaMiddle -= Math.min(_laneHitAlphaMiddle, 10);
        _laneHitAlphaRight -= Math.min(_laneHitAlphaRight, 10);

        // atom explosion ticker
        if (_explosionTicker > 0) {
            explosion(_ballsLeft);
            explosion(_ballsMiddle);
            explosion(_ballsRight);
        }

        // update tickers
        _doubleMultiplierTicker -= Math.min(1, _doubleMultiplierTicker);
        _explosionTicker -= Math.min(1, _explosionTicker);
        _tick = (_tick + 1) % 100000;
    }

    // remove the balls from an iterator that have fallen through the hitbox
    private boolean removeMissed(Iterator<Ball> iterator) {
        while (iterator.hasNext()) {
            Ball b = iterator.next();
            if (b.y > HITBOX_CENTER + HITBOX_HEIGHT / 2) {
                iterator.remove();
                Log.d(TAG, "fail press");
                onMiss(b);

                return b.type != Ball.BallType.Skull;
            }
        }
        return false;
    }

    // handles a TouchEvent on a certain lane
    private boolean hitLane(List<Ball> balls) {
        Iterator<Ball> iter = balls.iterator();
        Ball lowestBall = null;
        while (iter.hasNext()) {
            Ball b = iter.next();
            if (lowestBall == null || b.y > lowestBall.y) {
                lowestBall = b;
            }
        }

        if (lowestBall != null && lowestBall.y > HITBOX_CENTER - HITBOX_HEIGHT / 2) {
            balls.remove(lowestBall);
            onHit(lowestBall);
            return lowestBall.type != Ball.BallType.Skull;
        } else {
            if (lowestBall != null && lowestBall.y > HITBOX_CENTER - HITBOX_HEIGHT / 2 - MISS_ZONE_HEIGHT) {
                balls.remove(lowestBall);
            }
            onMiss(null);

            return false;
        }
    }

    // triggers when a lane gets tapped that has currently no ball in its hitbox
    private void onMiss(Ball b) {
        if(b != null && b.type == Ball.BallType.Skull) {
            return;
        }
        _vibrator.vibrate(100);
        _streak = 0;
        _score -= Math.min(_score, 50);
        _multiplier = 1;
        --_lifes;
        updateMultipliers();
    }

    // triggers when a lane gets tapped that currently has a ball in its hitbox
    private void onHit(Ball b) {
        _streak++;
        switch(b.type) {
            case OneUp: {
                ++_lifes;
            } break;
            case Multiplier: {
                _doubleMultiplierTicker = DOUBLE_MULTIPLIER_TIME;
            } break;
            case Bomb: {
                _explosionTicker = EXPLOSION_TIME;
                Assets.soundExplosion.play(1);
            } break;
            case Skull: {
                onMiss(null); // hitting a skull counts as a miss
                Assets.soundCreepyLaugh.play(1);
                return;
            }
        }

        updateMultipliers();
        _score += 10 * _multiplier
                * (_doubleMultiplierTicker > 0 ? 2 : 1);
    }

    // triggers after a touch event was handled by hitLane()
    private void updateMultipliers() {
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
        else {
            _multiplier = 1;
        }
    }

    private void spawnBalls() {
        float randFloat = _rand.nextFloat();
        final int ballY = BALL_INITIAL_Y;
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
        } else if (randFloat < _spawnChance_bomb) {
            balls.add(0, new Ball(ballX, ballY, Ball.BallType.Bomb));
        } else if (randFloat < _spawnChance_skull) {
            balls.add(0, new Ball(ballX, ballY, Ball.BallType.Skull));
        }
    }

    private void updatePaused(List<TouchEvent> touchEvents) {
        if (_currentTrack.isPlaying()) {
            _currentTrack.pause();
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_DOWN) {
                resume();
            }
        }
    }

    private void updateGameOver(List<TouchEvent> touchEvents) {
        if (!_currentTrack.isStopped()) {
            _currentTrack.stop();
        }

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

        for (Ball b: _ballsLeft) {
            paintBall(g, b);
        }

        for (Ball b: _ballsMiddle) {
            paintBall(g, b);
        }

        for (Ball b: _ballsRight) {
            paintBall(g, b);
        }


        if (_explosionTicker > 0) {
            if (_rand.nextDouble() > 0.05) {
                g.drawImage(Assets.explosion, 0, 680);
            } else {
                g.drawImage(Assets.explosionBright, 0, 680);
            }
            g.drawARGB((int)((double)_explosionTicker/EXPLOSION_TIME * 255), 255, 255, 255);
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

    private void paintBall(Graphics g, Ball b) {
        switch(b.type) {
            case Normal:
                g.drawImage(Assets.ballNormal, b.x - 90, b.y - 90);
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
            case Bomb:
                g.drawImage(Assets.ballBomb,  b.x - 90, b.y - 90);
                break;
            case Skull:
                g.drawImage(Assets.ballSkull, b.x - 90, b.y - 90);
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
        if (state == GameState.Running) {
            state = GameState.Paused;
            if(_currentTrack.isPlaying()) {
                _currentTrack.stop();
            }
        }

    }

    @Override
    public void resume() {
        if (state == GameState.Paused) {
            state = GameState.Running;
            _currentTrack.play();
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void backButton() {
        pause();
    }
}
