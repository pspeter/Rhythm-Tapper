package sma.rhythmtapper.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import sma.rhythmtapper.R;
import sma.rhythmtapper.models.BallData;

/**
 * Created by andil on 26.01.2017.
 */

public class BallDataAdapter extends ArrayAdapter<BallData> {

    public BallDataAdapter(Context context) {
        super(context, -1);
    }

    @NonNull
    @Override
    public View getView(int position, View container, ViewGroup parent) {
        if(container == null){
            Context c = getContext();
            LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            container = inflater.inflate(R.layout.list_ball, null);
        }
        final BallData entry = getItem(position);

        TextView v = null;
        v = (TextView)container.findViewById(R.id.list_ball_title);
        v.setText(entry.getTitleId());
        v = (TextView)container.findViewById(R.id.list_ball_desc);
        v.setText(entry.getDescId());
        ImageView img = (ImageView)container.findViewById(R.id.image);
        img.setImageResource(entry.getImgId());

        return container;
    }
}
