package sma.rhythmtapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private ListView _highscoreView;
    private ArrayAdapter<Highscore> _adapter;
    private ArrayList<Highscore> _arrayOfScores = new ArrayList<Highscore>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        _highscoreView = (ListView)this.findViewById(R.id.highscore_list_view);

        // load highscores
        _prefs = getSharedPreferences(PREF_FILE, 0);


        // TEST: add new value every time activity gets started
        CharSequence dateString = DateFormat.format("dd.MM.yyyy, hh:mm", new Date());
        SharedPreferences.Editor edit = _prefs.edit();
        edit.putString(dateString.toString(), "1000");
        edit.commit();




        // iterate through all prefs
        Map<String, ?> keys = _prefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("map values",entry.getKey() + ": " +
                    entry.getValue());
            this._arrayOfScores.add(new Highscore(entry.getKey(), Integer.parseInt(entry.getValue().toString())));
        }
        HighscoreAdapter adapter = new HighscoreAdapter(this, this._arrayOfScores);

        this._highscoreView.setAdapter(adapter);
    }
}
