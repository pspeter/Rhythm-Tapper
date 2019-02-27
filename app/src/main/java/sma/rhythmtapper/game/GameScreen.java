package sma.rhythmtapper.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.Log;

import sma.rhythmtapper.MainActivity;
import sma.rhythmtapper.framework.FileIO;
import sma.rhythmtapper.framework.Game;
import sma.rhythmtapper.framework.Graphics;
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
    private boolean _isEnding;

    // score
    private int _score;
    private int _multiplier;
    private int _streak;
    private int _combo;

    // tickers
    private int _tick;
    private int _doubleMultiplierTicker;
    private int _explosionTicker;
    private float _currentTime;
    private int _endTicker;

    // balls
    private List<Ball> _balls1;
    private List<Ball> _balls2;
    private List<Ball> _balls3;
    private List<Ball> _balls4;
    private List<Ball> _balls5;

    // lane miss indicators
    private int _laneHitAlpha1;
    private int _laneHitAlpha2;
    private int _laneHitAlpha3;
    private int _laneHitAlpha4;
    private int _laneHitAlpha5;

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
    private Paint _paintScore;
    private Paint _paintGameover;

    // constants
    // how far the screen should scroll after the track ends
    private static final int END_TIME = 1800;
    // initial y coordinate of spawned balls
    private static final int BALL_INITIAL_Y = -50;
    // hitbox is the y-range within a ball can be hit by a press in its lane
    private static  int HITBOX_CENTER = 1760;
    private static  int HITBOX_HEIGHT = 280;
    // if no ball is in the hitbox when pressed, remove the lowest ball in the
    // miss zone right above the hitbox (it still counts as a miss)
    private static  int MISS_ZONE_HEIGHT = 150;
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
        _balls1 = new ArrayList<>();
        _balls2 = new ArrayList<>();
        _balls3 = new ArrayList<>();
        _balls4 = new ArrayList<>();
        _balls5 = new ArrayList<>();

        _rand = new Random();
        _tick = 0;
        _endTicker = END_TIME / _difficulty.getBallSpeed();
        _currentTime = 0f;
        _explosionTicker = 0;
        _lifes = 10;
        _laneHitAlpha1 = 0;
        _laneHitAlpha2 = 0;
        _laneHitAlpha3 = 0;
        _laneHitAlpha4 = 0;
        _laneHitAlpha5 = 0;
        _currentTrack = Assets.musicTrack;
        _isEnding = false;

        // paints for text
        _paintScore = new Paint();
        _paintScore.setTextSize(30);
        _paintScore.setTextAlign(Paint.Align.CENTER);
        _paintScore.setAntiAlias(true);
        _paintScore.setColor(Color.WHITE);


        _paintGameover = new Paint();
        _paintGameover.setTextSize(50);
        _paintGameover.setTextAlign(Paint.Align.CENTER);
        _paintGameover.setAntiAlias(true);
        _paintGameover.setColor(Color.BLACK);

        HITBOX_CENTER= game.getScreenY()-HITBOX_HEIGHT;
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
            state = GameState.Running;
            touchEvents.clear();
            _currentTrack.setLooping(false);
            _currentTrack.setVolume(0.25f);
            _currentTrack.play();
        }
    }

    private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
        // 1. All touch input is handled here:
        handleTouchEvents(touchEvents);

        // 2. Check miscellaneous events like death:
        checkDeath();
        checkEnd();

        // 3. Individual update() methods.
        updateVariables(deltaTime);
    }

    private void checkEnd() {
        if (_currentTrack.isStopped()) {
            _isEnding = true;
        }
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
            endGame();
        }
    }

    private void endGame() {
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

    private void handleTouchEvents(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();

        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);

            if (event.type == TouchEvent.TOUCH_DOWN) {
                if (event.y > game.getScreenY()*0.5f) {
                    // ball hit area
                    if (event.x < _gameWidth / 5) {
                        if (!hitLane(_balls1)) {
                            // if no ball was hit
                            _laneHitAlpha1 = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else if (event.x < _gameWidth / 5 * 2) {
                        if (!hitLane(_balls2)) {
                            // if no ball was hit
                            _laneHitAlpha2 = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else if (event.x < _gameWidth / 5 * 3) {
                        if (!hitLane(_balls3))
                        {
                            _laneHitAlpha3 = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else if (event.x < _gameWidth / 5 * 4) {
                        if (!hitLane(_balls4)) {
                            // if no ball was hit
                            _laneHitAlpha4 = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                    else {
                        if (!hitLane(_balls5)) {
                            _laneHitAlpha5 = MISS_FLASH_INITIAL_ALPHA;
                        }
                    }
                }
                else {
                    // pause area
                    touchEvents.clear();
                    pause();
                    break;
                }
            }
        }
    }

    // update all the games variables each tick
    private void updateVariables(float deltatime) {
        // update timer
        _currentTime += deltatime;

        // update ball position
        for (Ball b: _balls1) {
            b.update((int) (_ballSpeed * deltatime));
        }

        for (Ball b: _balls2) {
            b.update((int) (_ballSpeed * deltatime));
        }

        for (Ball b: _balls3) {
            b.update((int) (_ballSpeed * deltatime));
        }

        for (Ball b: _balls4) {
            b.update((int) (_ballSpeed * deltatime));
        }

        for (Ball b: _balls5) {
            b.update((int) (_ballSpeed * deltatime));
        }

        // remove missed balls
        if (removeMissed(_balls1.iterator())) {
            _laneHitAlpha1 = MISS_FLASH_INITIAL_ALPHA;
        }

        if (removeMissed(_balls2.iterator())) {
            _laneHitAlpha2 = MISS_FLASH_INITIAL_ALPHA;
        }

        if (removeMissed(_balls3.iterator())) {
            _laneHitAlpha3 = MISS_FLASH_INITIAL_ALPHA;
        }

        if (removeMissed(_balls4.iterator())) {
            _laneHitAlpha4 = MISS_FLASH_INITIAL_ALPHA;
        }


        if (removeMissed(_balls5.iterator())) {
            _laneHitAlpha5 = MISS_FLASH_INITIAL_ALPHA;
        }

        // spawn new balls
        if (!_isEnding && _currentTime % _spawnInterval <= deltatime) {
            spawnBalls();
        }

        // decrease miss flash intensities
        _laneHitAlpha1 -= Math.min(_laneHitAlpha1, 10);
        _laneHitAlpha2 -= Math.min(_laneHitAlpha2, 10);
        _laneHitAlpha3 -= Math.min(_laneHitAlpha3, 10);
        _laneHitAlpha4 -= Math.min(_laneHitAlpha4, 10);
        _laneHitAlpha5 -= Math.min(_laneHitAlpha5, 10);

        // atom explosion ticker
        if (_explosionTicker > 0) {
            explosion(_balls1);
            explosion(_balls2);
            explosion(_balls3);
            explosion(_balls4);
            explosion(_balls5);
        }

        // update tickers
        _doubleMultiplierTicker -= Math.min(1, _doubleMultiplierTicker);
        _explosionTicker -= Math.min(1, _explosionTicker);
        _tick = (_tick + 1) % 100000;

        if (_isEnding) {
            _endTicker -= Math.min(1, _endTicker);

            if (_endTicker <= 0) {
                endGame();
            }
        }
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
        _combo=0;
        _score -= Math.min(_score, 50);
        _multiplier = 1;
        //--_lifes;
        updateMultipliers();
    }

    // triggers when a lane gets tapped that currently has a ball in its hitbox
    private void onHit(Ball b) {
        _streak++;
        ++_lifes;
        ++_combo;
        switch(b.type) {
            case OneUp: {
                ++_lifes;
            } break;
            case Multiplier: {
                _doubleMultiplierTicker = DOUBLE_MULTIPLIER_TIME;
            } break;
            case Bomb: {
                _explosionTicker = EXPLOSION_TIME;
                Assets.soundExplosion.play(0.7f);
            } break;
            case Skull: {
                onMiss(null); // hitting a skull counts as a miss
                Assets.soundCreepyLaugh.play(1);
                return;
            }
        }

        updateMultipliers();
        _score += 10 * _multiplier*_combo;
                //* (_doubleMultiplierTicker > 0 ? 2 : 1);
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
        int ballX = _gameWidth / 5 / 2;
        spawnBall(_balls1, randFloat, ballX, ballY);

        randFloat = _rand.nextFloat();
        ballX = _gameWidth / 5 / 2 * 3 ;
        spawnBall(_balls2,randFloat, ballX, ballY);

        randFloat = _rand.nextFloat();
        ballX = _gameWidth / 2;
        spawnBall(_balls3, randFloat, ballX, ballY);

        randFloat = _rand.nextFloat();
        ballX =  _gameWidth - _gameWidth / 5 / 2 * 3;
        spawnBall(_balls4,randFloat, ballX, ballY);

        randFloat = _rand.nextFloat();
        ballX = _gameWidth - _gameWidth / 5 / 2 ;
        spawnBall(_balls5, randFloat, ballX, ballY);

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
                return;
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
                if (event.x > 300 && event.x < 540 && event.y > 845
                        && event.y < 1100) {
                    game.goToActivity(MainActivity.class);
                    return;
                } else if (event.x >= 540 && event.x < 780 && event.y > 845
                        && event.y < 1100) {
                    game.setScreen(new LoadingScreen(game, _difficulty));
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
        g.drawRect(0                 , 0, _gameWidth / 5 + 1, _gameHeight, Color.argb(_laneHitAlpha1, 255, 0, 0));
        g.drawRect(_gameWidth / 5    , 0, _gameWidth / 5 + 1, _gameHeight, Color.argb(_laneHitAlpha2, 255, 0, 0));
        g.drawRect(_gameWidth / 5 * 2, 0, _gameWidth / 5 + 1, _gameHeight, Color.argb(_laneHitAlpha3, 255, 0, 0));
        g.drawRect(_gameWidth / 5 * 3, 0, _gameWidth / 5 + 1, _gameHeight, Color.argb(_laneHitAlpha4, 255, 0, 0));
        g.drawRect(_gameWidth / 5 * 4, 0, _gameWidth / 5 + 1, _gameHeight, Color.argb(_laneHitAlpha5, 255, 0, 0));

        final int dx=_gameWidth / 10;
        for(int i=0;i<5;i++)
        {
            int n=2*i+1;
            g.drawImage(Assets.ballHitpoint, dx*n-90, HITBOX_CENTER);
        }

        for (Ball b: _balls1) {
            paintBall(g, b);
        }

        for (Ball b: _balls2) {
            paintBall(g, b);
        }

        for (Ball b: _balls3) {
            paintBall(g, b);
        }

        for (Ball b: _balls4) {
            paintBall(g, b);
        }

        for (Ball b: _balls5) {
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
        _paintScore = null;

        // Call garbage collector to clean up memory.
        System.gc();
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        g.drawARGB(155, 0, 0, 0);
        g.drawString("Tap to start!", 540, 500, _paintScore);
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
        g.drawString(s, 600, 80, _paintScore);
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        g.drawARGB(155, 0, 0, 0);
        g.drawImage(Assets.pause, 200, 500);
        g.drawString("TAP TO CONTINUE", 540, 845, _paintGameover);
    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        g.drawARGB(205, 0, 0, 0);
        g.drawImage(Assets.gameover, 200, 500);
        g.drawString("FINAL SCORE: " + _score, 540, 845, _paintGameover);
    }

    @Override
    public void pause() {
        if (state == GameState.Running) {
            state = GameState.Paused;
            _currentTrack.pause();
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
        if(_currentTrack.isPlaying()) {
            _currentTrack.stop();
        }
    }

    @Override
    public void backButton() {
        dispose();
    }
}
