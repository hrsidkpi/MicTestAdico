package com.example.mictestadico;


import static android.media.AudioManager.MODE_NORMAL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mRecorder;
    private Handler mHandler = new Handler();

    private AudioRecord recorder;
    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMicrophoneAvailable()) {
                    ((TextView)findViewById(R.id.button)).setText("Mic not available");
                    return;
                }
                ((TextView)findViewById(R.id.button)).setText("Mic available");
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                File file = new File(Environment.getExternalStorageDirectory() + "/Demo");
                if (!file.exists())
                    file.mkdir();

                mRecorder.setOutputFile(getExternalFilesDir(Environment.DIRECTORY_MUSIC)  + "/demoAudio.mp3");

                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    mRecorder = null;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    mRecorder = null;
                }

                Thread thread = new Thread(mPollTask);
                thread.start();
            }
        });



    }


    public static boolean isMicrophoneAvailable() {
        AudioManager audioManager = (AudioManager) MyApp.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getMode() == MODE_NORMAL;
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;
    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

            ((TextView)findViewById(R.id.button)).setText("volume: "+amp);

                }
            });

            if ((amp > 100)) {
                //===========================
                // DO STUFF HERE
                //===========================
                //Log.i("Noise", "==== onCreate ===");
            }
            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, 10);
        }
    };

}