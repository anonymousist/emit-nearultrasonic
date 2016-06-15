package com.example.administrator.emit_sound;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {
    private SeekBar frequency_Bar = null;
    private SeekBar time_Bar = null;
    private ProgressBar soundProssbar;
    private TextView freText;
    private TextView timText;
    private TextView timerText;
    private TextView showMessage;
    Button play_button;
    private double frequency;
    private int duration;
    AudioTrack mAudioTrack;
    Date myDate;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private MediaPlayer myAudioPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play_button = (Button) findViewById(R.id.button1);
        Button stop_button = (Button) findViewById(R.id.button2);
        freText = (TextView)findViewById(R.id.freText);
        timText = (TextView)findViewById(R.id.timText);
        timerText = (TextView)findViewById(R.id.timer);
        showMessage = (TextView)findViewById(R.id.textView);
        frequency_Bar = (SeekBar)findViewById(R.id.frequency_bar);
        time_Bar = (SeekBar)findViewById(R.id.time_bar);
        soundProssbar = (ProgressBar)findViewById(R.id.progressBar);
        frequency_Bar.setMax(20);
        time_Bar.setMax(100);
        frequency_Bar.setOnSeekBarChangeListener(this);
        time_Bar.setOnSeekBarChangeListener(this);
        frequency = Double.parseDouble(String.valueOf(frequency_Bar.getProgress()))*1000;
        duration = Integer.parseInt(String.valueOf(time_Bar.getProgress()))*44100;
        timText.setText("duration : " + String.valueOf(time_Bar.getProgress()) + "s");
        freText.setText("frequency :" + String.valueOf(frequency_Bar.getProgress()) + "khz");
        timerText.setText("system time");
        Thread myThread;

        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //howMessage.setText("frequency :" + String.valueOf(frequency / 1000) + "khz  duration :" + String.valueOf(duration / 44100) + "s");
                showMessage.setText("frequency :" + String.valueOf(frequency / 1000) + "khz  duration :" + "manual stop");
                play_button.setEnabled(false);
             Thread  playAudioThread = new Thread(new Runnable() {
                   @Override
                   public void run() {
                       playSound(frequency, duration);
                   }
               });
                playAudioThread.start();
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop_play();
            }
        });

        final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        myDate = new Date(System.currentTimeMillis());
                        String str = formatter.format(myDate);
                        timerText.setText(str); //更新时间
                        break;
                    default:
                        break;

                }
            }
        };

         myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        Message msg = new Message();
                        msg.what = 1;
                        myHandler.sendMessage(msg);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        myThread.start();
    }

    private void playSound(final double frequency, final int duration){
        //AudioTrack defination
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
         mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,44100
                ,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize*2,AudioTrack.MODE_STREAM);
        //buffer defination and audiotrack initialize
        final double[] mSound = new double[1024*512];
        final short[] mBuffer = new short[1024*512];
        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        mAudioTrack.play();
        //gen sin wave and write to buffer
        Thread gen = new Thread(new Runnable() {
            @Override
            public void run() {

                while (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    for (int i = 0; i < mSound.length; i++) {
                        mSound[i] = Math.sin((2.0 * Math.PI * i / (44100 / frequency)));
                        mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
                    }
                    mAudioTrack.write(mBuffer, 0, mSound.length);
                }
            }
        });
        gen.start();
       // mAudioTrack.stop();
       // mAudioTrack.release();
    }

    protected void stop_play(){
        if(mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.flush();
            mAudioTrack.release();
            mAudioTrack = null;
            play_button.setEnabled(true);
        }
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()) {
            case R.id.frequency_bar:{
                // set freText show the frequency value
                freText.setText(" frequency : "+String.valueOf(seekBar.getProgress())+"khz");
                frequency = Double.parseDouble(String.valueOf(frequency_Bar.getProgress()))*1000;
                break;
            }
            case R.id.time_bar: {
                // 设置“与自定义SeekBar对应的TextView”的值
                //timText.setText("duration : "+String.valueOf(seekBar.getProgress())+"seconds");
                timText.setText("time bar~~~~come back soon");
                duration = Integer.parseInt(String.valueOf(time_Bar.getProgress()))*44100;
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
