package sma.rhythmtapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import sma.rhythmtapper.helpers.HighscoreAdapter;
import sma.rhythmtapper.models.Highscore;

public class HighscoreActivity extends Activity {

    public static String PREF_FILE = "HighscorePrefFile";
    private SharedPreferences _prefs;

    /* views */
    //private ListView _highscoreView;
    //private ArrayAdapter<Highscore> _adapter;
    //private ArrayList<Highscore> _arrayOfScores = new ArrayList<Highscore>();

    private TextView _easyTxtView;
    private TextView _medTxtView;
    private TextView _hardTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        //_highscoreView = (ListView)this.findViewById(R.id.highscore_list_view);

        _easyTxtView = (TextView)this.findViewById(R.id.highscore_txt_score_easy);
        _medTxtView = (TextView)this.findViewById(R.id.highscore_txt_score_medium);
        _hardTxtView = (TextView)this.findViewById(R.id.highscore_txt_score_hard);

        // load highscores
        _prefs = getSharedPreferences(PREF_FILE, 0);

        String easyMode = _prefs.getString("easy", "0");
        String mediumMode = _prefs.getString("medium", "0");
        String hardMode = _prefs.getString("hard", "0");

        _easyTxtView.setText(easyMode);
        _medTxtView.setText(mediumMode);
        _hardTxtView.setText(hardMode);


    }
}
