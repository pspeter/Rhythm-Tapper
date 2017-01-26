package sma.rhythmtapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import sma.rhythmtapper.models.Difficulty;


public class DifficultySelectionActivity extends Activity implements View.OnClickListener{

    private Button btnEasy;
    private Button btnMid;
    private Button btnHard;

    private final Difficulty _diffEasy =
            new Difficulty(Difficulty.EASY_TAG, "Aquaria_Minibadass_OC_ReMix.mp3", 20, 4);
    private final Difficulty _diffMid =
            new Difficulty(Difficulty.MED_TAG, "super_meat_boy_power_of_the_meat.mp3", 23.4375f, 10);
    private final Difficulty _diffHard =
            new Difficulty(Difficulty.HARD_TAG, "Aquaria_Minibadass_OC_ReMix.mp3", 16.66666f, 15);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_selection);

        this.btnEasy = (Button)this.findViewById(R.id.diff_btn_easy);
        this.btnEasy.setOnClickListener(this);
        this.btnMid = (Button)this.findViewById(R.id.diff_btn_mid);
        this.btnMid.setOnClickListener(this);
        this.btnHard = (Button)this.findViewById(R.id.diff_btn_hard);
        this.btnHard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.diff_btn_easy:
                i = new Intent(this, GameActivity.class);
                i.putExtra("difficulty", this._diffEasy);
                this.startActivity(i);
                break;
            case R.id.diff_btn_mid:
                i = new Intent(this, GameActivity.class);
                i.putExtra("difficulty", this._diffMid);
                this.startActivity(i);
                break;
            case R.id.diff_btn_hard:
                i = new Intent(this, GameActivity.class);
                i.putExtra("difficulty", this._diffHard);
                this.startActivity(i);
                break;
            default:
                Log.e("","unexpected id!");
        }
    }
}
