package com.example.changoo.fishing.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.changoo.fishing.R;
import com.example.changoo.fishing.util.Formatter;

import java.io.IOException;

public class BitedActivity extends AppCompatActivity {
    private ImageView mBitedImgv;
    private TextView mTimeTv;
    private Button mOkBtn;
    private boolean isVibrate = true;
    //Ringtone audio=null;
    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bited);

        mBitedImgv 	= (ImageView) findViewById(R.id.imgv_bited);
        mTimeTv 	= (TextView) findViewById(R.id.tv_bited_time);

        Glide.with(this).load(R.drawable.icon_bited).asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mBitedImgv);

        long bitedTime;
        Intent intent = getIntent();
        bitedTime = intent.getLongExtra("bitedTime", 0);
        mTimeTv.setText(Formatter.toTime(bitedTime));

        //음악 재생
        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.catch_fish_sounds);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        new Thread() {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            @Override
            public void run() {
                while (isVibrate) {
                    vibrator.vibrate(1000);
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }.start();

        mOkBtn = (Button)findViewById(R.id.btn_ok);
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        isVibrate = false;
        mediaPlayer.stop();
    }
}
