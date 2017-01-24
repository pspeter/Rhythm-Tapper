package sma.rhythmtapper;

import android.os.Bundle;
import android.util.Log;

import sma.rhythmtapper.framework.Impl.RTGame;
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.game.LoadingScreen;
import sma.rhythmtapper.models.Difficulty;

public class GameActivity extends RTGame {
    private Difficulty _diff;


    @Override
    public Screen getInitScreen() {
        // get passed difficulty object
        _diff = (Difficulty)this.getIntent().getSerializableExtra("difficulty");
        Log.d("tescht", String.valueOf(_diff.getBallSpeed()));
        return new LoadingScreen(this, _diff);
    }
}
