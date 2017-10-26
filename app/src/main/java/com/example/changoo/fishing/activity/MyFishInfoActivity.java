package com.example.changoo.fishing.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.BitmapManager;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.model.User;
import com.example.changoo.fishing.util.Formatter;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MyFishInfoActivity extends AppCompatActivity {
    private static final String TAG = "MyFishInfoActivity";
    private Fish mFish;
    TextView mNameTv;
    TextView mMaxTv;
    TextView mAvgTv;
    TextView mSpeciesTv;
    TextView mTimeingTv;
    TextView mTimeTv;
    TextView mGPSTv;
    Button mFacebookBtn;
    Button mKakaoBtn;
    Button mCheckBtn;

    ImageView mFishImgv;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_fish_info);

        Intent intent = getIntent();//Fish 객체 저장
        mFish = intent.getParcelableExtra("fish");

        mNameTv = (TextView) findViewById(R.id.tv_my_info_name);
        mSpeciesTv = (TextView) findViewById(R.id.tv_my_info_species);
        mMaxTv = (TextView) findViewById(R.id.tv_my_info_maxpower);
        mAvgTv = (TextView) findViewById(R.id.tv_my_info_avgpower);
        mTimeTv = (TextView) findViewById(R.id.tv_my_info_datetime);
        mTimeingTv = (TextView) findViewById(R.id.tv_my_info_timing);
        mGPSTv = (TextView) findViewById(R.id.tv_my_info_gps);

        mFishImgv = (ImageView) findViewById(R.id.imgv_my_info_fish);
        mFacebookBtn = (Button) findViewById(R.id.btn_facebook_share);
        mFacebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFacebookPhotoShare();
                //or
                //sendFacebookAppLinkShare();
            }
        });

        mKakaoBtn = (Button) findViewById(R.id.btn_kakao);
        mKakaoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KakaoLink kakaoLink;
                try {
                    kakaoLink = KakaoLink.getKakaoLink(MyFishInfoActivity.this);
                    KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

                    String nameStr = "이름 : " + mFish.getName();
                    String speicesStr = "어종 : " + mFish.getSpecies();
                    String maxFowerStr = "최대힘 : " + mFish.getMaxFower() + " F";
                    String avgFowerStr = "평균힘 : " + mFish.getAvgFower() + " F";
                    String GPSStr = "지역 : " + getAddress(MyFishInfoActivity.this, mFish.getGPS_lat(), mFish.getGPS_lot());

                    String text = nameStr;
                    text += "\n";
                    text += speicesStr;
                    text += "\n";
                    text += maxFowerStr;
                    text += "\n";
                    text += avgFowerStr;
                    text += "\n";
                    text += GPSStr;

                    if (mFish.getImageFile().equals("null")) {
                        kakaoTalkLinkMessageBuilder
                                .addText(text)
                                .addAppButton("팔딱팔딱!")
                                .build();
                    } else {
                        String imageUrl = HttpManager.getFishImageURL() + mFish.getImageFile();
                        int imageWidth = 100;
                        int imageHeight = 100;
                        kakaoTalkLinkMessageBuilder
                                .addImage(imageUrl, imageWidth, imageHeight)
                                .addText(text)
                                .addAppButton("팔딱팔딱!")
                                .build();
                    }

                    kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, MyFishInfoActivity.this);
                } catch (KakaoParameterException e) {
                    Log.e("ddd", e.getMessage());
                }


            }
        });

        mCheckBtn = (Button) findViewById(R.id.btn_my_fish_info_check);
        mCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_my_fish_info_check: //체크 버튼(확인 버튼) 눌렀을 때
                        finish();
                }
            }
        });

        if (mFish != null) {
            Log.i(TAG, mFish.toString());
            mNameTv.setText(mFish.getName());
            mSpeciesTv.setText(mFish.getSpecies());
            mMaxTv.setText(Formatter.setFormat(mFish.getMaxFower()) + " F");
            mAvgTv.setText(Formatter.setFormat(mFish.getAvgFower()) + " F");
            mTimeTv.setText(mFish.getDate() + " " + mFish.getTime());
            mTimeingTv.setText(mFish.getTimeing() + " 초");
            mGPSTv.setText(getAddress(this, mFish.getGPS_lat(), mFish.getGPS_lot()));

            if (mFish.getImageFile().equals("null"))
                Picasso.with(this).load(R.drawable.image_default_fish).transform(new CircleTransform()).into(mFishImgv);
            else
                Picasso.with(this).load(HttpManager.getFishImageURL() + mFish.getImageFile()).transform(new CircleTransform()).into(mFishImgv);

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
        }
        return nowAddress;
    }

    private void sendFacebookPhotoShare() { //facebook 으로 사진 업로드 //이미지 뷰에 설정된 사진 읽어서 업로드

        new AsyncTask<Void,Void,Bitmap>(){

            @Override
            protected Bitmap doInBackground(Void... params) {
                return BitmapManager.getBitmapFromURL(HttpManager.getFishImageURL() + mFish.getImageFile());
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                SharePhoto photo = new SharePhoto.Builder().setBitmap(result).build();
                SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
                ShareDialog shareDialog=new ShareDialog(MyFishInfoActivity.this);
                shareDialog.show(content);

            }
        }.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


}
