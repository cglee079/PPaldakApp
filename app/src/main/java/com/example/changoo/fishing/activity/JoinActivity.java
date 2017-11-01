package com.example.changoo.fishing.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.BitmapManager;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.httpConnect.Protocol;
import com.example.changoo.fishing.model.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class JoinActivity extends AppCompatActivity {
    private static final String TAG = "JoinActivity";

    /********************
     * For Camera
     ******************/
    private static final int PICK_FROM_GALLERY = 0;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private Uri mImageCaptureUri = null;
    private Bitmap mFishPicture = null;
    private boolean isPickPicture = false;


    /********************************************************/

    private User mUser = new User();


    private ImageView mUserImgv = null;
    private TextView mMessageTx = null;
    private EditText mIdEt = null;
    private EditText mPwEt = null;
    private EditText mNameEt = null;
    private EditText mBirthEt = null;
    private EditText mPhoneNumberEt = null;
    private EditText mAuthoriztionEt = null;
    private Button mJoinBtn = null;
    private Button mCancelBtn = null;
    private Button mBirthBtn = null;
    private Button mAuthoriztionBtn = null;
    private Button mCheckMessageBtn = null;
    private RadioButton mMaleRadioBtn = null;
    private RadioButton mFemaleRadioBtn = null;

    int mAuthorizationNumber;

    DatePickerDialog dialog; //날짜 입력

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mUserImgv 		= (ImageView) findViewById(R.id.imgv_user_img);
        mIdEt 			= (EditText) findViewById(R.id.et_join_id);
        mPwEt 			= (EditText) findViewById(R.id.et_join_pw);
        mNameEt 		= (EditText) findViewById(R.id.et_join_name);
        mBirthEt 		= (EditText) findViewById(R.id.et_join_birth);
        mPhoneNumberEt 	= (EditText) findViewById(R.id.et_join_phone);
        mAuthoriztionEt = (EditText) findViewById(R.id.et_join_authorization_number);
        mMessageTx 		= (TextView) findViewById(R.id.tv_join_message);

        mJoinBtn 		= (Button) findViewById(R.id.btn_join_join);
        mCancelBtn 		= (Button) findViewById(R.id.btn_join_cancel);
        mBirthBtn 		= (Button) findViewById(R.id.btn_join_birth);
        mAuthoriztionBtn = (Button) findViewById(R.id.btn_join_authorization);
        mCheckMessageBtn = (Button) findViewById(R.id.btn_join_check_message);
        mMaleRadioBtn 	= (RadioButton) findViewById(R.id.rbtn_join_male);
        mFemaleRadioBtn = (RadioButton) findViewById(R.id.rbtn_join_female);

        Picasso.with(this).load(R.drawable.icon_camera_white).transform(new CircleTransform()).into(mUserImgv);
        mUserImgv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSelectPicture();
            }
        });
        
        mJoinBtn.setOnClickListener(new BtnListener());
        mCancelBtn.setOnClickListener(new BtnListener());
        mBirthBtn.setOnClickListener(new BtnListener());
        mAuthoriztionBtn.setOnClickListener(new BtnListener());
        mCheckMessageBtn.setOnClickListener(new BtnListener());

        mBirthEt.setFocusable(false);
        mBirthEt.setClickable(false);

        //날짜 입력 캘린더 생성
        Calendar calendar = Calendar.getInstance();
        calendar.set(1992,8,1);
        dialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }


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
                mUserImgv.setImageBitmap(mFishPicture);
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

    class BtnListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_join_join) { //가입 버튼 클릭
                setDataToServer();
                Log.d(TAG, "JoinActivity end");
            } else if (v.getId() == R.id.btn_join_cancel) { //취소 버튼 클릭
                Log.d(TAG, "JoinActivity end");
                finish();
            } else if (v.getId() == R.id.btn_join_birth) { // 날짜 입력 버튼 클릭
                dialog.show();
            } else if (v.getId() == R.id.btn_join_check_message) { // 휴대폰 번호 인증 입력 버튼 클릭
                requestMessagePermission();
            } else if (v.getId() == R.id.btn_join_authorization) { // 휴대폰 번호 인증 입력 버튼 클릭
                if (mAuthoriztionEt.getText().toString().equals(Integer.toString(mAuthorizationNumber))) {
                    mMessageTx.setText("휴대폰 번호가 인증 되었습니다.");  //인증 성공
                } else {
                    mMessageTx.setText("휴대폰 번호 인증을 실패했습니다."); //인증 실패
                    Log.i(TAG, "authorization failed" + mAuthoriztionEt.getText().toString() + "::" + Integer.toString(mAuthorizationNumber));
                }
            }
        }
    }

    //날짜 입력 캘린더 리스너
    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        int year;
        int month;
        int day;

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            this.year 	= year;
            this.month 	= monthOfYear + 1;
            this.day 	= dayOfMonth;
            mBirthEt.setText(this.year + "년" + month + "월" + day + "일");
        }
    };


    /**
     * 문자 권한 런타임시 허락받기
     * API 23부터 Mainifest에 미리 등록할 수 없음
     */
    public void requestMessagePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) { //해당 퍼미션이 없을 경우
            Log.i(TAG, "permission no existed");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 255); //request cod 255번으로 권한을 요청하는 다이어로그 생성
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "permission already greanted");

            //문자 발송
            SmsManager smsManager = SmsManager.getDefault();
            mAuthorizationNumber = setAuthorizationNumber();
            smsManager.sendTextMessage(mPhoneNumberEt.getText().toString(), null, Integer.toString(mAuthorizationNumber), null, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 255: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    //문자 발송
                    SmsManager smsManager = SmsManager.getDefault();
                    int number = setAuthorizationNumber();
                    smsManager.sendTextMessage(mPhoneNumberEt.getText().toString(), null, Integer.toString(number), null, null);

                    Log.i(TAG, "permission was granted");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(getApplication(), "회원가입을 진행할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "JoinActivity end");
                    finish();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //휴대폰 인증번호 생성 메소드
    public int setAuthorizationNumber() {
        Random random = new Random();
        int number = random.nextInt(100000) + 10000;//5자리의 난수 발생
        return number;
    }

    /*****************************private method************************************************/
    /**
     * 서버 통신
     */
    private void setDataToServer() {
        //데이터 수신
        new JoinActivity.HttpFishsAsyncTask().execute();
    }

    private void sendFishImageToServer() {
        Bitmap mFishPicture = BitmapFactory.decodeFile(BitmapManager.getImagePath());
        if (mFishPicture != null) {
            Log.d(TAG, "SAVE FISH IMAGE " + mFishPicture.getWidth() + "*" + mFishPicture.getHeight());
            JoinActivity.HttpSendImgAsyncTask httpSendImgAsyncTask = new JoinActivity.HttpSendImgAsyncTask();
            httpSendImgAsyncTask.execute();
        }
    }

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

    class HttpFishsAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog mProgressDialog;
        private String line = null;
        private String page = null;
        private String reqeustURL = null;


        /**
         * MainThread
         */
        @Override
        protected void onPreExecute() {

            mProgressDialog = ProgressDialog.show(JoinActivity.this, "", "로딩중...", true, false);

            String id = mIdEt.getText().toString();
            String password = mPwEt.getText().toString();
            String name = mNameEt.getText().toString();
            String gender = null;
            String birth = mBirthEt.getText().toString();
            String phoneNumber = mPhoneNumberEt.getText().toString();
            String imgFile=null;

            if (mMaleRadioBtn.isChecked()) {
                gender = mMaleRadioBtn.getText().toString();
            } else if (mFemaleRadioBtn.isChecked()) {
                gender = mFemaleRadioBtn.getText().toString();
            }
            
            //한글 인코딩
            try {
                if(gender != null) { gender = URLEncoder.encode(gender, "UTF-8"); }
                if(name != null) { name = URLEncoder.encode(name, "UTF-8"); }
                if(birth != null) { birth = URLEncoder.encode(birth, "UTF-8"); }
            } catch (UnsupportedEncodingException e) {
            }

            mUser.setId(id);
            mUser.setPassword(password);
            mUser.setName(name);
            mUser.setGender(gender);
            mUser.setBirth(birth);
            mUser.setPhoneNumber(phoneNumber);

            if (isPickPicture == true) {
                imgFile = mUser.getId() + ".jpg";
            }
            mUser.setImageFile(imgFile);
        }

        /**
         * Sub Thread
         */
        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            try {

                //URL 가져옴
                reqeustURL = HttpManager.getJoinURL();

                //Parameter
                reqeustURL += mUser.toHttpParameter();

                //최종 접속 URL
                Log.d(TAG, "URL " + reqeustURL);

                //String->URL
                URL url = new URL(reqeustURL);

                //Http Connect
                HttpURLConnection connect = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.

                Log.d(TAG, "Connect  " + connect);
                connect.setRequestMethod("GET"); // get방식 통신
                connect.setDoOutput(true); // 쓰기모드 지정
                connect.setDoInput(true); // 읽기모드 지정
                connect.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                connect.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                InputStream is;

                int status = connect.getResponseCode();

                if (status != HttpURLConnection.HTTP_OK) {
                    is = connect.getErrorStream();
                    Log.d(TAG, "staus  " + status);
                } else {
                    is = connect.getInputStream();
                    Log.d(TAG, "staus  " + status);
                }
                connect.getInputStream(); //input스트림 개방

                StringBuilder mBuilder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader mReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅

                //요청이 정상적으로 전달되엇으면 HTTP_OK(200) 리턴
                //URL이 발견되지 않으면 HTTP_NOT_FOUND(404) 리턴
                //인증에 실패하면 HTTP_UNAUTHORIZED(401) 리턴
                int responseCode = connect.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "HTTP_OK :: " + responseCode);

                    // 버퍼의 웹문서 소스를 줄 단위로 읽어 저장
                    while ((line = mReader.readLine()) != null) {
                        mBuilder.append(line + "\n");
                    }

                    mReader.close();
                    page = mBuilder.toString();

                    Log.d(TAG, "HTTP_OK page: " + page);

                    JSONObject message = new JSONObject(page);
                    result = message.getString(Protocol.CHECKING_USER);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch (IOException io) {
                io.printStackTrace();
                Log.e(TAG, "Error I/O " + io.toString());
            } catch (JSONException je) {
                je.printStackTrace();
                Log.e(TAG, "Error parsing data " + je.toString());
            }

            return result;
        }

        /**
         * MainThread
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.cancel();
            if (result.equals(Protocol.JOIN_NOK) || result == null) {
                Log.d(TAG, "JOIN FAILED");
                Toast.makeText(JoinActivity.this, R.string.toast_join_failed, Toast.LENGTH_SHORT).show();
                finish();
            } else if (result.equals(Protocol.JOIN_OK)) {
                Log.d(TAG, "JOIN SUCCESS");
                Toast.makeText(JoinActivity.this, R.string.toast_join_success, Toast.LENGTH_SHORT).show();
                if (mUser.getImageFile() != null){
                    sendFishImageToServer();
                }
            }
        }

    }

    class HttpSendImgAsyncTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "HTTP_SEND_IMAGE_TASK";

        private ProgressDialog mProgressDialog;
        private String reqeustURL = null;

        /**
         * MainThread
         */
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(JoinActivity.this, "", "로딩중...", true, false);
        }

        @Override
        protected String doInBackground(Void... params) {
            //serverURL 가져옴
            reqeustURL = HttpManager.getSaveUserImageURL();

            //최종 접속 URL
            Log.d(TAG, "URL " + reqeustURL);

            // request 준비
            try {
                URL url = new URL(reqeustURL);
                String boundary = "SpecificString";
                HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                connect.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connect.setRequestMethod("POST"); // get방식 통신
                connect.setDoOutput(true); // 쓰기모드 지정
                connect.setDoInput(true); // 읽기모드 지정
                DataOutputStream wr = new DataOutputStream(connect.getOutputStream());

                String id = mUser.getId();
                String filename = mUser.getImageFile();

                wr.writeBytes("\r\n--" + boundary + "\r\n");

                //ID PARAMETER 전송
                wr.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n" + id);
                wr.writeBytes("\r\n--" + boundary + "\r\n");

                //FILE NAME PARAMETER 전송
                wr.writeBytes("Content-Disposition: form-data; name=\"filename\"\r\n\r\n" + filename);
                wr.writeBytes("\r\n--" + boundary + "\r\n");

                //IMAGE 전송
                wr.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
                wr.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                
                FileInputStream fileInputStream = new FileInputStream(BitmapManager.getImagePath());
                int bytesAvailable 	= fileInputStream.available();
                int maxBufferSize 	= 1024;
                int bufferSize		= Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer 		= new byte[bufferSize];

                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    // Upload file part(s)
                    DataOutputStream dataWrite = new DataOutputStream(connect.getOutputStream());
                    dataWrite.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }


                wr.writeBytes("\r\n--" + boundary + "--\r\n");
                wr.flush();

                Log.d(TAG, "############## SEND IMAGE COMPLETED ##################");

                BufferedReader rd = null;

                rd = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = rd.readLine()) != null) {
                    Log.i(TAG, line);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch (SocketTimeoutException se) {
                Log.e(TAG, "Error Connect TimeOut " + se.toString());
                return null;
            } catch (IOException io) {
                io.printStackTrace();
                Log.e(TAG, "Error I/O " + io.toString());
            }

            return null;
        }


        /**
         * Sub Thread
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.cancel();
            finish();
        }
    }
}
