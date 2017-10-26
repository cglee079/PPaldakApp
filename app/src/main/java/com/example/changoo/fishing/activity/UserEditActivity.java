package com.example.changoo.fishing.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.graphic.CircleTransform;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.model.User;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class UserEditActivity extends AppCompatActivity {
    public static final String TAG = "USEREDITACTIVITY";

    private DatePickerDialog dialog;
    private Button mCancelBtn;
    private Button mCheckBtn;
    private ImageView mUserImgv = null;
    private EditText mNameEt = null;
    private EditText mBirthEt = null;
    private EditText mPasswordEt = null;
    private EditText mPhoneNumberEt = null;
    private EditText mIdEt = null;
    private Button mBirthBtn = null;
    private Calendar calendar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        mIdEt = (EditText) findViewById(R.id.et_user_id);
        mNameEt = (EditText) findViewById(R.id.et_user_name);
        mPasswordEt = (EditText) findViewById(R.id.et_user_password);
        mBirthEt = (EditText) findViewById(R.id.et_user_birth);
        mPhoneNumberEt = (EditText) findViewById(R.id.et_user_tel);
        mCancelBtn = (Button) findViewById(R.id.btn_profile_edit_cancel);
        mCheckBtn = (Button) findViewById(R.id.btn_profile_edit_ok);
        mBirthBtn = (Button) findViewById(R.id.btn_et_birth);
        mUserImgv = (ImageView) findViewById(R.id.imgv_user_edit_image);

        mCancelBtn.setOnClickListener(mBtnListener);
        mCheckBtn.setOnClickListener(mBtnListener);
        mBirthBtn.setOnClickListener(mBtnListener);

        User user = User.getInstance();
        mNameEt.setText(user.getName());
        mBirthEt.setText(user.getBirth());
        mPhoneNumberEt.setText(user.getPhoneNumber());
        mIdEt.setText(user.getId());
        mPasswordEt.setText(user.getPassword());
        if (user.getImageFile() == null)
            Picasso.with(this).load(R.drawable.image_default_user).transform(new CircleTransform()).into(mUserImgv);
        else
            Picasso.with(this).load(HttpManager.getUserImageURL() + user.getImageFile()).transform(new CircleTransform()).into(mUserImgv);


        //날짜 입력 캘린더 생성
        calendar = Calendar.getInstance();
        if (user.getBirth() != null) {
            String year = user.getBirth().substring(0, 4);
            calendar.set(Integer.parseInt(year), 0, 1);
        }
        dialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    Button.OnClickListener mBtnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_profile_edit_cancel:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
                case R.id.btn_profile_edit_ok:
                    setResult(RESULT_OK);
                    new HttpUserAsyncTask().execute();
                    break;
                case R.id.btn_et_birth:
                    dialog.show();
                    break;
            }
        }
    };

    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        int year;
        int month;
        int day;

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            this.year = year;
            month = monthOfYear + 1;
            day = dayOfMonth;
            mBirthEt.setText(this.year + "년" + month + "월" + day + "일");
        }
    };

    class HttpUserAsyncTask extends AsyncTask<Void, Void, Void> {
        private String line = null;
        private String page = null;
        private String reqeustURL = null;
        String name = null;
        String password = null;
        String phoneNumber = null;
        String id = null;
        String birth = null;

        /**
         * MainThread
         */
        @Override
        protected void onPreExecute() {
            name = mNameEt.getText().toString();
            password = mPasswordEt.getText().toString();
            id = mIdEt.getText().toString();
            birth = mBirthEt.getText().toString();
            phoneNumber = mPhoneNumberEt.getText().toString();
        }

        /**
         * Sub Thread
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {

                User mUser = User.getInstance();
                mUser.setName(name);
                mUser.setPassword(password);
                mUser.setId(id);
                mUser.setBirth(birth);
                mUser.setPhoneNumber(phoneNumber);

                User.setInstance(mUser);

                //URL 가져옴
                reqeustURL = HttpManager.getSaveUser();

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

                InputStream is = connect.getInputStream(); //input스트림 개방

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
                }
                mReader.close();
                page = mBuilder.toString();
                Log.d(TAG, "HTTP_OK page: " + page);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch (ConnectException ce) {
                ce.printStackTrace();
                Log.e(TAG, "Error Connected " + ce.toString());
            } catch (IOException io) {
                io.printStackTrace();
                Log.e(TAG, "Error I/O " + io.toString());
            } finally {
                return null;
            }
        }

        /**
         * MainThread
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}
