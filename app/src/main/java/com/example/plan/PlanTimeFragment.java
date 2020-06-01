package com.example.plan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlanTimeFragment extends Fragment {

    public static final String filename = "timeFragmentSetting";
    public static final String KEY_LEARN = "timeLearn";
    public static final String KEY_RESET = "timeReset";

    private Button btnLearn, btnRest, btnFinish;
    private TextView tvTime, tvContext;
    private ProgressBar pbTime;
    private EditText edTodo;
    private ImageButton btnSoundControl;

    private Map<String, Object> map = new HashMap<String, Object>();
    private int learnMinutes = 25, resetMinutes = 5;

    private CountDownTimer timer;
    private MediaPlayer player;

    private boolean isMute = false;

    public PlanTimeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_time, null);

        btnLearn = (Button)view.findViewById(R.id.fragment_time_btn_learn);
        btnFinish = (Button)view.findViewById(R.id.fragment_tine_btn_finish);
        btnRest = (Button)view.findViewById(R.id.fragment_time_btn_rest);
        btnSoundControl = (ImageButton) view.findViewById(R.id.fragment_time_btn_sound_control);
        tvTime = (TextView)view.findViewById(R.id.fragment_time_tv_time);
        tvContext = (TextView)view.findViewById(R.id.fragment_time_tv_content);
        pbTime = (ProgressBar)view.findViewById(R.id.fragment_time_progress);
        edTodo = (EditText)view.findViewById(R.id.fragment_time_ed_todo);

        btnFinish.setOnClickListener(btnClickListener);
        btnRest.setOnClickListener(btnClickListener);
        btnLearn.setOnClickListener(btnClickListener);
        btnSoundControl.setOnClickListener(btnClickListener);

        //设置进度条
        pbTime.setProgress(0);
        //设置learn rest按钮，输入框可见
        btnLearn.setVisibility(View.VISIBLE);
        btnRest.setVisibility(View.VISIBLE);
        edTodo.setVisibility(View.VISIBLE);
        //设置finish按钮，提示Tv 不可见
        btnFinish.setVisibility(View.GONE);
        tvContext.setVisibility(View.GONE);
        btnSoundControl.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //读取用户设置信息，因为需要获得实时的配置，所以放在onResume
        try{
            FileInputStream f_in_finish = getActivity().openFileInput(PlanTimeFragment.filename);
            ObjectInputStream ois_finish = new ObjectInputStream(f_in_finish);
            map = extracted(ois_finish);
            f_in_finish.close();
            //设置定时器定时时间
            if((int)map.get(PlanTimeFragment.KEY_LEARN) >= 25)
                learnMinutes = (int)map.get(KEY_LEARN);
            if((int)map.get(PlanTimeFragment.KEY_RESET) >= 5)
                resetMinutes = (int)map.get(KEY_RESET);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(player != null)
        {
            if(player.isPlaying())
            {
                btnSoundControl.setImageResource(R.drawable.sound);
            }
            else
            {
                btnSoundControl.setImageResource(R.drawable.mute);
            }
        }
        tvTime.setText(""+ learnMinutes +":00");
    }

    private Map<String, Object> extracted(ObjectInputStream ois_finish) throws IOException, ClassNotFoundException {
        return (Map<String, Object>)ois_finish.readObject();
    }

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int resId = v.getId();
            switch (resId)
            {
                case R.id.fragment_time_btn_learn:
                    //设置finish 静音按钮，提示Tv 可见
                    btnFinish.setVisibility(View.VISIBLE);
                    tvContext.setVisibility(View.VISIBLE);
                    btnSoundControl.setVisibility(View.VISIBLE);
                    //设置learn rest按钮，输入框不可见
                    btnLearn.setVisibility(View.GONE);
                    btnRest.setVisibility(View.GONE);
                    edTodo.setVisibility(View.GONE);
                    //读取输入框
                    String inputTodo = edTodo.getText().toString();
                    edTodo.setText("");
                    tvContext.setText("学习："+inputTodo);
                    if(!isMute) {//开启背景音乐
                        startMusic();
                    }
                    //设置时间框的时间，开启定时
                    tvTime.setText(""+ learnMinutes +":00");
                    setTimer(learnMinutes,resId);
                    break;
                case R.id.fragment_time_btn_rest:
                    //设置finish 静音按钮，提示Tv 可见
                    btnFinish.setVisibility(View.VISIBLE);
                    tvContext.setVisibility(View.VISIBLE);
                    btnSoundControl.setVisibility(View.VISIBLE);
                    //设置learn rest按钮，输入框不可见
                    btnLearn.setVisibility(View.GONE);
                    btnRest.setVisibility(View.GONE);
                    edTodo.setVisibility(View.GONE);
                    //读取输入框
                    String inputStr = edTodo.getText().toString();
                    edTodo.setText("");
                    //显示计划
                    tvContext.setText("休息："+inputStr);
                    if(!isMute) {//开启背景音乐
                        startMusic();
                    }
                    //设置时间框的时间，开启定时
                    tvTime.setText(""+ resetMinutes +":00");
                    setTimer(resetMinutes, resId);
                    break;
                case R.id.fragment_tine_btn_finish:
                    todoFinish(resId);
                    break;
                case R.id.fragment_time_btn_sound_control:
                    if(player.isPlaying())
                    {
                        player.pause();
                        isMute = true;
                        btnSoundControl.setImageResource(R.drawable.mute);
                    }
                    else
                    {
                        player.start();
                        isMute = false;
                        btnSoundControl.setImageResource(R.drawable.sound);
                    }
                    break;
            }
        }
    };

    private void todoFinish(int resId)
    {
        //震动一下
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
        long[] patter = {500, 1000};
        vibrator.vibrate(patter, -1);


        if(timer != null)
            timer.cancel();
        if(player != null)
        {
            if(player.isPlaying())
            {
                player.stop();
            }

        }

        if(resId == R.id.fragment_time_btn_learn || resId == R.id.fragment_tine_btn_finish)
            tvTime.setText(""+ learnMinutes +":00");

        //设置进度条
        pbTime.setProgress(0);
        //设置learn rest按钮，输入框可见
        btnLearn.setVisibility(View.VISIBLE);
        btnRest.setVisibility(View.VISIBLE);
        edTodo.setVisibility(View.VISIBLE);
        //设置finish按钮，提示Tv 不可见
        btnFinish.setVisibility(View.GONE);
        tvContext.setVisibility(View.GONE);
        btnSoundControl.setVisibility(View.GONE);
    }

    private void setTimer(final int minutes, final int resId)
    {
        if(minutes > 0)
        {
            final int millisFuture = minutes*60*1000;
            timer =  new CountDownTimer(millisFuture, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    int sec = (int) (millisUntilFinished/1000);
                    int minute = sec/60;
                    tvTime.setText("" + minute + ":" + (sec%60));
                    pbTime.setMax(millisFuture/1000);
                    pbTime.setProgress((int) (millisUntilFinished/1000));
                }

                @Override
                public void onFinish() {
                    todoFinish(resId);
                }
            };
            timer.start();
        }
    }
    private void startMusic()
    {
        player = MediaPlayer.create(getContext(), R.raw.back_music);
        player.setLooping(true);
        player.start();
    }

    @Override
    public void onStop()
    {
        map.put(KEY_RESET, resetMinutes);
        map.put(KEY_LEARN, learnMinutes);
        try {
            FileOutputStream f_out_todo = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
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
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(player != null)
        {
            if(player.isPlaying())
            {
                player.stop();
            }
            player.release();
        }

        if(timer != null)
        {
            timer.cancel();
        }
    }
}
