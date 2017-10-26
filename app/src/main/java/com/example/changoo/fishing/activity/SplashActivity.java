package com.example.changoo.fishing.activity;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;

import com.example.changoo.fishing.R;


public class SplashActivity extends AppCompatActivity {

    //로딩 이미지 애니매이션
    ImageView imageView;
    private AnimationDrawable aniFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //액티비티에서 타이틀바 없애기
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        imageView = (ImageView) findViewById(R.id.animation_splash);

        //xml과 연결
        imageView.setBackgroundResource(R.drawable.splash_list);
        aniFrame = (AnimationDrawable) imageView.getBackground();
        aniFrame.start();//애니매이션 시작

        initialize();
    }

    private void initialize() {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                aniFrame.stop();//애니매이션 종료
                finish();    // 액티비티 종료
                overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
                //페이드 인 페이드 아웃 효과 res/anim/fadein, fadeout xml을 만들어 줘야 합니다.
                //다른 전환효과도 응용가능
                //overridePendingTransition(R.anim.fadein, R.anim.fadein);

            }
        };

        handler.sendEmptyMessageDelayed(0, 3000);    // ms, 3초후 종료시킴

    }
}
