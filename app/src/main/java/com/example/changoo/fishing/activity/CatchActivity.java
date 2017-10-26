package com.example.changoo.fishing.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.util.Formatter;
import com.example.changoo.fishing.util.GPSPermission;
import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.BitmapManager;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.util.Time;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CatchActivity extends AppCompatActivity{
    private static final String TAG = "CATCH_ACTIVITY";

    /////////////////////////////////////////////////////////
    ///************************ Camera ********************///
    private static final int PICK_FROM_GALLERY = 0;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private Uri mImageCaptureUri = null;
    private Bitmap mFishPicture = null;
    private boolean isPickPicture = false;

    ///////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////

    private Fish mFish = null;
    private ImageView mFishImgv = null;
    private TextView mMaxPowerTv = null;
    private TextView mAvgPowerTv = null;
    private TextView mDateTimeTv = null;
    private TextView mTimeingTv = null;
    private TextView mGPSTv = null;
    private EditText mNameEt = null;
    private EditText mSpeciesEt = null;

    private Button mSaveBtn = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch);

        Intent intent = getIntent();
        mFish = intent.getParcelableExtra("fish");


        mFishImgv = (ImageView) findViewById(R.id.imgv_catch_fish);
        Picasso.with(this).load(R.drawable.icon_camera).transform(new CircleTransform()).into(mFishImgv);

        mFishImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSelectPicture();
            }
        });


        mMaxPowerTv = (TextView) findViewById(R.id.tv_maxpower);
        mAvgPowerTv = (TextView) findViewById(R.id.tv_avgpower);
        mDateTimeTv = (TextView) findViewById(R.id.tv_datetime);
        mTimeingTv = (TextView) findViewById(R.id.tv_timeing);
        mGPSTv = (TextView) findViewById(R.id.tv_gps);
        mSaveBtn = (Button) findViewById(R.id.btn_save);

        mNameEt = (EditText) findViewById(R.id.et_name);
        mSpeciesEt = (EditText) findViewById(R.id.et_species);


        mMaxPowerTv.setText(Formatter.setFormat(mFish.getMaxFower()) + " F");
        mAvgPowerTv.setText(Formatter.setFormat(mFish.getAvgFower()) + " F");
        mDateTimeTv.setText(mFish.getDate() + " " + mFish.getTime());
        mTimeingTv.setText(mFish.getTimeing()+"");
        mGPSTv.setText(getAddress(this, mFish.getGPS_lat(), mFish.getGPS_lot()));

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String insertName = mNameEt.getText().toString();
                if (insertName.length() != 0)
                    mFish.setName(insertName);

                String insertSpecies = mSpeciesEt.getText().toString();
                if (insertSpecies.length() != 0)
                    mFish.setSpecies(insertSpecies);
                Intent data = new Intent();
                data.putExtra("fish", mFish);
                setResult(RESULT_OK, data);
                finish();
            }
        });


        //시간 설정
        mDateTimeTv.setText(Time.getDate() + "  " + Time.getTime());


    }

    /**
     * Overide Method
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Result RequestCode :" + requestCode);
        if (resultCode != RESULT_OK) { // 에러처리
            if (data != null) {
                Log.e(TAG, "resultCode : " + resultCode + " Error!!! ");
            }
            return;
        }

        switch (requestCode) {
            case CROP_FROM_CAMERA: { // Crop된 후 처리
                mFishPicture = BitmapFactory.decodeFile(BitmapManager.getImagePath());
                mFishPicture = BitmapManager.getCroppedBitmap(mFishPicture);
                mFishImgv.setImageBitmap(mFishPicture);
                mFish.setImageFile(mFish.getId() + ".jpg");
                isPickPicture = true;
                break;
            }

            case PICK_FROM_GALLERY:
            case PICK_FROM_CAMERA: {
                mFishPicture = null;
                if (requestCode == PICK_FROM_GALLERY) {
                    mImageCaptureUri = data.getData(); // 갤러리에서 선택된 사진의 Uri 리턴
                    try {
                        mFishPicture = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri); // Uri로 이미지 가져오기
                        Log.e(TAG, "PICK_FROM_ALBUM : " + mFishPicture.getHeight() * mFishPicture.getWidth()); // 확인코드
                    } catch (Exception e) {
                        Log.e(TAG, "PICK_FROM_ALBUM : " + e.toString());
                    }

                    if (mFishPicture != null)
                        try {
                            FileOutputStream fos = new FileOutputStream(BitmapManager.getImagePath());
                            mFishPicture.compress(Bitmap.CompressFormat.JPEG, 100, fos); // 이미지 저장
                            fos.flush();
                            fos.close();
                        } catch (Exception e) {
                            Log.e(TAG, "" + requestCode + " : " + e.toString());
                        }
                    else {
                        Log.e(TAG, "Bitmap is null"); // 에러처리
                        return;
                    }
                }

                Intent intent = new Intent(this, CropImageActivity.class);
                startActivityForResult(intent, CROP_FROM_CAMERA);
                break;
            }
        }
    }



    /**
     * Private Method
     */

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /****************************
     * CAMERA
     ***************************************/

    private void doSelectPicture() {

        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doTakePhotoAction();
            }
        };
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doTakeAlbumAction();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .show();
    }

    // 카메라 호출
    private void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(BitmapManager.getImagePath());
        mImageCaptureUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(Intent.createChooser(intent, null), PICK_FROM_CAMERA);
    }

    // Gallery 호출
    private void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_GALLERY);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////////////////////////////

}






