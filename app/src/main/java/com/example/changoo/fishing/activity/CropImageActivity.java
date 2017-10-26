package com.example.changoo.fishing.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.BitmapManager;
import com.example.changoo.fishing.graphic.CropImage;

public class CropImageActivity extends Activity implements View.OnClickListener {

    private CropImage mCropImage;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 화면 계속 켜지게
        setContentView(R.layout.activity_crop_image);
        mCropImage = (CropImage) findViewById(R.id.crop_view);  // 커스텀 이미지뷰
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_rotate).setOnClickListener(this);
    }

    public void onClick(View v) {
        Intent i = getIntent();
        switch(v.getId()) {
            case R.id.btn_ok:
                mCropImage.save();
                setResult(RESULT_OK, i);
                System.gc();
                finish();
                break;
            case R.id.btn_cancel:
                setResult(RESULT_CANCELED, i);
                System.gc();
                finish();
                break;
            case R.id.btn_rotate:
                mCropImage.rotatePicture();
                break;
        }

    }
}



