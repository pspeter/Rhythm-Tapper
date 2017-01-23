package sma.rhythmtapper.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sma.rhythmtapper.R;
import sma.rhythmtapper.models.Highscore;

public class HighscoreAdapter extends ArrayAdapter<Highscore> {
    public HighscoreAdapter(Context context, ArrayList<Highscore> scores) {
        super(context, 0, scores);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Highscore score = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_highscore, parent, false);
        }

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.text_date);
        TextView tvHome = (TextView) convertView.findViewById(R.id.text_score);

        // Populate the data into the template view using the data object
        tvName.setText(score.getDate().toString());
        tvHome.setText(score.getScore());
        // Return the completed view to render on screen
        return convertView;
    }
}
