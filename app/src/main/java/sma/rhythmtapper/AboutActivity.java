package sma.rhythmtapper;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

import sma.rhythmtapper.helpers.BallDataAdapter;
import sma.rhythmtapper.models.BallData;

public class AboutActivity extends Activity {

    private ListView _listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        this._listView = (ListView)this.findViewById(R.id.about_list);
        BallDataAdapter data = new BallDataAdapter(this);

        data.add(new BallData(R.string.about_ball_normal_title,
                R.string.about_ball_normal_content,
                R.drawable.ball_normal));
        data.add(new BallData(R.string.about_ball_oneup_title,
                R.string.about_ball_oneup_content,
                R.drawable.ball_oneup));
        data.add(new BallData(R.string.about_ball_speeder_title,
                R.string.about_ball_speeder_content,
                R.drawable.ball_speeder));
        data.add(new BallData(R.string.about_ball_bomb_title,
                R.string.about_ball_bomb_content,
                R.drawable.ball_bomb));
        data.add(new BallData(R.string.about_ball_multiplier_title,
                R.string.about_ball_multiplier_content,
                R.drawable.ball_multiplier));
        data.add(new BallData(R.string.about_ball_skull_title,
                R.string.about_ball_skull_content,
                R.drawable.ball_skull));

        this._listView.setAdapter(data);

    }
}
