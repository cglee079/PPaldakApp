package com.example.changoo.fishing.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.httpConnect.Protocol;
import com.example.changoo.fishing.model.User;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText mIdEt = null;
    private EditText mPwEt = null;
    private Button mLoginBtn = null;
    private Button mJoinBtn = null;

    private String saved_id = null;
    private String saved_password = null;
    private String id = null;
    private String password = null;
    private String name = null;
    private String gender = null;


    /**
     * Overide Method
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // 스플래시 화면 띄우기
        startActivity(new Intent(this, SplashActivity.class));

        super.onCreate(savedInstanceState);

        //메인 액티비티에서 타이틀바 없애기
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);


        mIdEt 		= (EditText) findViewById(R.id.et_id);
        mPwEt 		= (EditText) findViewById(R.id.et_pw);
        mLoginBtn 	= (Button) findViewById(R.id.btn_login);
        mJoinBtn 	= (Button) findViewById(R.id.btn_join);

        mLoginBtn.setOnClickListener(new BtnListener());
        mJoinBtn.setOnClickListener(new BtnListener());


        //User 정보
        getUserPreferences();

        if (saved_id.equals("")) { //User 정보 없을 시 첫번째 로그인
            Log.i(TAG, "First Login");
        } else { //User 정보 존재
            Log.i(TAG, "NOT First Login");
            Log.i(TAG, "saved_id : " + saved_id);
            Log.i(TAG, "saved_pw : " + saved_password);

            id = saved_id;
            password = saved_password;

            HttpLoginAsyncTask httpLoginAsyncTask = new HttpLoginAsyncTask();
            httpLoginAsyncTask.execute();
        }

    }

    // User 정보 불러오기
    private void getUserPreferences() {
        SharedPreferences pref = getSharedPreferences("mPref", MODE_PRIVATE);

        saved_id = pref.getString("before_id", "");
        saved_password = pref.getString("before_password", "");

        Log.i(TAG, "SAVED DATA LOADING = id " + saved_id + "pw " + saved_password);
    }

    // User 정보 저장하기
    private void saveUserPreferences(String id, String password) {
        SharedPreferences pref = getSharedPreferences("mPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("before_id", id);
        editor.putString("before_password", password);
        editor.commit();
    }


    /**
     * Listener
     */

    class BtnListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_login) {

                id = mIdEt.getText().toString();
                password = mPwEt.getText().toString();

                HttpLoginAsyncTask httpLoginAsyncTask = new HttpLoginAsyncTask();
                httpLoginAsyncTask.execute();

            } else if (v.getId() == R.id.btn_join) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        }
    }

    class HttpLoginAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog mProgressDialog;
        private String line = null;
        private String page = null;
        private String reqeustURL = null;
        private String parameter = null;

        /**
         * MainThread
         */
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(LoginActivity.this, "", "로딩중...", true, false);
        }

        /**
         * Sub Thread
         */
        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            try {

                //serverURL 가져옴
                reqeustURL = HttpManager.getLoginURL();

                //Parameter
                parameter += "&id=" + id;
                parameter += "&password=" + password;
                parameter += "&name=" + name;
                parameter += "&gender=" + gender;

                //최종 접속 URL
                Log.d(TAG, "URL " + reqeustURL);

                //String->URL
                URL url = new URL(reqeustURL);

                //Http Connect
                HttpURLConnection connect = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.
                Log.d(TAG, "Connect  " + connect);

                connect.setConnectTimeout(3000);
`
                connect.setRequestMethod("POST"); // get방식 통신
                connect.setDoOutput(true); // 쓰기모드 지정
                connect.setDoInput(true); // 읽기모드 지정
                connect.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                connect.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                OutputStream outputStream = connect.getOutputStream();
                outputStream.write(parameter.getBytes());
                outputStream.flush();
                outputStream.close();

                InputStream is = connect.getInputStream(); //input스트림 개방

                StringBuilder mBuilder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader mReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅

                //요청이 정상적으로 전달되엇으면 HTTP_OK(200) 리턴
                //URL이 발견되지 않으면 HTTP_NOT_FOUND(404) 리턴
                //인증에 실패하면 HTTP_UNAUTHORIZED(401) 리턴
                int responseCode = connect.getResponseCode();
                Log.d(TAG, "HTTP_STATE :: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // 버퍼의 웹문서 소스를 줄 단위로 읽어 저장
                    while ((line = mReader.readLine()) != null) {
                        mBuilder.append(line + "\n");
                    }
                }

                mReader.close();
                page = mBuilder.toString();
                Log.d(TAG, "HTTP_OK page: " + page);

                JSONObject message = new JSONObject(page);
                result = message.getString(Protocol.CHECKING_USER);
                if (result.equals(Protocol.USER_OK)) {
                    User user = User.getInstance();
                    Gson gson = new Gson();
                    Log.i(TAG, message.getJSONObject("user").toString());
                    User userFromServer = gson.fromJson(message.getJSONObject("user").toString(), User.class);

                    user.setId(userFromServer.getId());
                    user.setPassword(userFromServer.getPassword());
                    user.setBirth(userFromServer.getBirth());
                    user.setGender(userFromServer.getGender());
                    user.setName(userFromServer.getName());
                    user.setPhoneNumber(userFromServer.getPhoneNumber());
                    user.setImageFile(userFromServer.getImageFile());

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR URL" + e.toString());
            } catch ( SocketTimeoutException se){
                Log.e(TAG, "Error Connect TimeOut " + se.toString());
                return null;
            } catch (IOException io) {
                io.printStackTrace();
                Log.e(TAG, "Error I/O " + io.toString());
            } catch (JSONException je) {
                je.printStackTrace();
                Log.e(TAG, "Error parsing data " + je.toString());
            }

            return result;
        }


        /***
         * MainThread
         */

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mProgressDialog.cancel();

            if (result == null) {
                Log.d(TAG, "CONNET FAILED");
                Toast.makeText(LoginActivity.this, R.string.toast_login_failed_no_response, Toast.LENGTH_SHORT).show();
            } else if (result.equals(Protocol.USER_NOK)) {
                Log.d(TAG, "LOGIN FAILED");
                Toast.makeText(LoginActivity.this, R.string.toast_login_failed_user_nok, Toast.LENGTH_SHORT).show();
            } else if (result.equals(Protocol.USER_OK)) {
                Log.d(TAG, "LOGIN SUCCESS");
                Toast.makeText(LoginActivity.this, R.string.toast_login_success, Toast.LENGTH_SHORT).show();


                // Activity 가 종료되기 전에 저장한다
                // SharedPreferences 에 설정값(특별히 기억해야할 사용자 값)을 저장하기
                saveUserPreferences(mIdEt.getText().toString(), mPwEt.getText().toString());

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                //LoginActivity 종료
                finish();

            }
        }
    }
}


