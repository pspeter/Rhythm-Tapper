package sma.rhythmtapper;

import sma.rhythmtapper.framework.Impl.RTGame;
import sma.rhythmtapper.framework.Screen;
import sma.rhythmtapper.game.LoadingScreen;

public class GameActivity extends RTGame {
    @Override
    public Screen getInitScreen() {
        return new LoadingScreen(this);
    }
}
