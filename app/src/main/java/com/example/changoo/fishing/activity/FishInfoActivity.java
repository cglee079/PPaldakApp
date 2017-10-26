package com.example.changoo.fishing.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.util.Formatter;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FishInfoActivity extends AppCompatActivity {
    private static final String TAG = "MyFishInfoActivity";
    private Fish mFish;
    TextView mUserIdTv;
    TextView mNameTv;
    TextView mMaxTv;
    TextView mAvgTv;
    TextView mSpeciesTv;
    TextView mTimeingTv;
    TextView mTimeTv;
    TextView mGPSTv;
    Button mCheckBtn;
    ImageView mFishImgv;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fish_info);

        Intent intent = getIntent();//Fish 객체 저장
        mFish = intent.getParcelableExtra("fish");

        mUserIdTv = (TextView) findViewById(R.id.tv_info_user_id);
        mNameTv = (TextView) findViewById(R.id.tv_info_name);
        mSpeciesTv = (TextView) findViewById(R.id.tv_info_species);
        mMaxTv = (TextView) findViewById(R.id.tv_info_maxpower);
        mAvgTv = (TextView) findViewById(R.id.tv_info_avgpower);
        mTimeTv = (TextView) findViewById(R.id.tv_info_datetime);
        mTimeingTv=(TextView)findViewById(R.id.tv_info_timing);
        mGPSTv = (TextView) findViewById(R.id.tv_info_gps);

        mFishImgv = (ImageView) findViewById(R.id.imgv_info_fish);

        if (mFish != null) {
            mUserIdTv.setText(mFish.getUser_id());
            mNameTv.setText(mFish.getName());
            mSpeciesTv.setText(mFish.getSpecies());
            mMaxTv.setText(Formatter.setFormat(mFish.getMaxFower()) + " F");
            mAvgTv.setText(Formatter.setFormat(mFish.getAvgFower()) + " F");
            mTimeTv.setText(mFish.getDate() + " " + mFish.getTime());
            mTimeingTv.setText(mFish.getTimeing() +" 초");
            mGPSTv.setText(getAddress(this, mFish.getGPS_lat(), mFish.getGPS_lot()));

            if (mFish.getImageFile().equals("null"))
                Picasso.with(this).load(R.drawable.image_default_fish).transform(new CircleTransform()).into(mFishImgv);
            else
                Picasso.with(this).load(HttpManager.getFishImageURL() + mFish.getImageFile()).transform(new CircleTransform()).into(mFishImgv);

            mCheckBtn  = (Button) findViewById(R.id.btn_fish_info_check);
            mCheckBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()){
                        case R.id.btn_fish_info_check : //체크 버튼(확인 버튼) 눌렀을 때
                            finish();
                    }
                }
            });
        }
    }//onCreate End

    private String getAddress(Context mContext, double lat, double lng) {
        String nowAddress = "현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            return null;
//            Context baseContext;
//            Toast.makeText(baseContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
//
//            e.printStackTrace();
        }
        return nowAddress;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
