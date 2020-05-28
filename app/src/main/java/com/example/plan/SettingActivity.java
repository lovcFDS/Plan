package com.example.plan;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    private TextView tvLearn, tvReset;
    private SeekBar sbLearn, sbReset;

    private Map<String, Object> map = new HashMap<String, Object>();
    private int learnMinutes, resetMinutes;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        tvLearn = (TextView)findViewById(R.id.setting_tv_learn);
        tvReset = (TextView)findViewById(R.id.setting_tv_reset);
        sbLearn = (SeekBar)findViewById(R.id.setting_seekbar_learn);
        sbReset = (SeekBar)findViewById(R.id.setting_seekbar_reset);


        sbLearn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                learnMinutes = 25 + seekBar.getProgress();
                tvLearn.setText("" + learnMinutes);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbReset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                resetMinutes = 5 +seekBar.getProgress();
                tvReset.setText("" + resetMinutes);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private Map<String, Object> extracted(ObjectInputStream ois_finish) throws IOException, ClassNotFoundException {
        return (Map<String, Object>)ois_finish.readObject();
    }

    @Override
    public void onResume() {
        //读取用户信息，
        super.onResume();

        try {
            FileInputStream f_in_finish = openFileInput(PlanTimeFragment.filename);
            ObjectInputStream ois_finish = new ObjectInputStream(f_in_finish);
            map = extracted(ois_finish);
            f_in_finish.close();
            //设置定时器定时时间
            if ((int) map.get(PlanTimeFragment.KEY_LEARN) >= 25)
                learnMinutes = (int) map.get(PlanTimeFragment.KEY_LEARN);
            if ((int) map.get(PlanTimeFragment.KEY_RESET) >= 5)
                resetMinutes = (int) map.get(PlanTimeFragment.KEY_RESET);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvLearn.setText("" + learnMinutes);
        tvReset.setText("" + resetMinutes);

        sbReset.setProgress(resetMinutes - 5);
        sbLearn.setProgress(learnMinutes - 25);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        map.put(PlanTimeFragment.KEY_RESET, resetMinutes);
        map.put(PlanTimeFragment.KEY_LEARN, learnMinutes);
        try {
            FileOutputStream f_out_todo = openFileOutput(PlanTimeFragment.filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos_todo = new ObjectOutputStream(f_out_todo);
            oos_todo.writeObject(map);
            f_out_todo.flush();
            f_out_todo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }
}
