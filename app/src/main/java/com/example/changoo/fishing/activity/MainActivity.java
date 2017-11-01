package com.example.changoo.fishing.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.changoo.fishing.DB.CatchedFishDBHelper;
import com.example.changoo.fishing.data.Data;
import com.example.changoo.fishing.fragment.FirstFragment;

import com.example.changoo.fishing.fragment.SecondFragment;
import com.example.changoo.fishing.fragment.ThirdFragment;
import com.example.changoo.fishing.fragment.ForthFragment;
import com.example.changoo.fishing.graphic.BitmapManager;
import com.example.changoo.fishing.graphic.CatchedFishFAB;

import com.example.changoo.fishing.httpConnect.HttpManager;
import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.model.User;
import com.example.changoo.fishing.service.BTCTemplateService;
import com.example.changoo.fishing.util.AppSettings;
import com.example.changoo.fishing.util.Constants;

import com.example.changoo.fishing.R;
import com.example.changoo.fishing.util.Formatter;
import com.example.changoo.fishing.util.GPSPermission;
import com.example.changoo.fishing.util.MyFile;
import com.example.changoo.fishing.util.Time;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context mContext = this;
    private CoordinatorLayout mLayout;

    /*Fragment ID */
    public static final int FRAGMENT_POS_FISHING = 0;
    public static final int FRAGMENT_POS_LIST = 1;
    public static final int FRAGMENT_POS_RANK = 2;
    public static final int FRAGMENT_POS_INFO = 3;
    public static final int FRAGMENT_COUNT = 4;

    /*Result Activity*/
    public static final int FISH_CATCH = 1001;
    public static final int USER_EDIT = 1002;

    /*Top Title*/
    private TextView mTitleTv;
    private ImageView mBluetoothImgv;

    /*BlueTooth*/
    private String mConnectedDevice = null;
    private BluetoothHandler mBlueToothHandler;
    private BTCTemplateService mService;

    /*Progress Dialog*/
    private ProgressDialog mProgressDialog;


    /*For Fish Send to Server */
    private Fish mFish = null;

    /*For Catched Fish*/
    CatchedFishDBHelper mCatchedFishDBHelper = null;
    static final String mFILENAME = "catchedfishs.db";

    private RelativeLayout mCatchedFishLayout = null;
    private TextView mCountCatchedFishInLayoutTv = null;
    private TextView mCountCatchedFishTv;
    private FloatingActionMenu mCatchedFishsFAM;
    private ArrayList<Fish> mCatchedFishs = null;
    private HashMap<String, CatchedFishFAB> mCatchedFishsFABMap = null;
    private DataLogicHandler mDataLogicHandler = null;

    View.OnLongClickListener mFABLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final CatchedFishFAB catchedFishFAB = (CatchedFishFAB) v;
            new AlertDialog.Builder(MainActivity.this).setItems(R.array.fab_long_click, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedIndex) {
                            switch (selectedIndex) {
                                case 0:
                                    removeCatchedFish(catchedFishFAB.getmCathcedFish());
                                    break;
                            }
                        }
                    }).show();

            return true;
        }

    };


    /*Tab View */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private FirstFragment mFirst_fragment = null;
    private ForthFragment mForth_fragment = null;
    private int tabIcons[] = {
            R.drawable.tabicon_fishing,
            R.drawable.tabicon_list,
            R.drawable.tabicon_rank,
            R.drawable.tabicon_info,
    };

    /*For BackPress*/
    private BackPressCloseSystem backPressCloseSystem;

    /**
     * Service connection
     */
    private ServiceConnection mServiceConn = new ServiceConnection() {

        //bindService() 호출시 실행
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "Activity - Service connected");

            mService = ((BTCTemplateService.ServiceBinder) binder).getService();

            // Activity couldn't work with mService until connections are made
            // So initialize parameters and settings here. Do not initialize while running onCreate()
            bluetoothInitialize();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    /**
     * Override Method
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "On create");

        mLayout = (CoordinatorLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(mLayout);
        mContext = this;

        if (savedInstanceState != null) {
            mConnectedDevice = savedInstanceState.getString("mConnectedDevice");
            User.setInstance((User) savedInstanceState.getSerializable("user"));
        }

        //CatchedFishs init
        mCatchedFishLayout = (RelativeLayout) findViewById(R.id.layout_catchedfish);
        mCountCatchedFishInLayoutTv = (TextView) findViewById(R.id.tv_layout_count_catchedfish);

        mCountCatchedFishTv = (TextView) findViewById(R.id.tv_count_cathcedfish);
        mCatchedFishsFAM = (FloatingActionMenu) findViewById(R.id.fam_catchedfIsh);
        mCatchedFishsFAM.setClosedOnTouchOutside(true);
        mCatchedFishsFAM.setAnimated(true);
        mCatchedFishsFAM.setIconAnimated(false);
        mCatchedFishsFAM.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    mCatchedFishsFAM.getMenuIconView().setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
                    mCatchedFishLayout.setVisibility(View.VISIBLE);
                } else {
                    mCatchedFishsFAM.getMenuIconView().setImageDrawable(getResources().getDrawable(R.drawable.icon_catchedfish));
                    mCatchedFishLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        mCatchedFishs = new ArrayList<>();
        mCatchedFishsFABMap = new HashMap<>();

        mCatchedFishDBHelper = new CatchedFishDBHelper(this, mFILENAME, 1);
        Iterator<Fish> iter = mCatchedFishDBHelper.getCatchedFishsFromDB().iterator();
        while (iter.hasNext()) {
            Fish catchedFish = iter.next();
            addCatchedFish(catchedFish);
        }

        mCountCatchedFishTv.setText(mCatchedFishs.size() + "");
        mCountCatchedFishInLayoutTv.setText(mCatchedFishs.size() + " 마리");

        //Data Logic Setting
        mDataLogicHandler = new DataLogicHandler();

        //Bluetooth Setting
        mBlueToothHandler = new BluetoothHandler();
        AppSettings.initializeAppSettings(mContext);

        //BackPress setting
        backPressCloseSystem = new BackPressCloseSystem(this);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Tab Setting
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(FRAGMENT_COUNT); //생성 유지되는 프래그먼트 갯수
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case FRAGMENT_POS_FISHING:
                        mTitleTv.setText(R.string.fragment_first_title);
                        break;
                    case FRAGMENT_POS_LIST:
                        mTitleTv.setText(R.string.fragment_second_title);
                        break;
                    case FRAGMENT_POS_RANK:
                        mTitleTv.setText(R.string.fragment_third_title);
                        break;
                    case FRAGMENT_POS_INFO:
                        mTitleTv.setText(R.string.fragment_forth_title);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        }); // 프래그먼트 바뀔때 이벤트 리스너

        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mBluetoothImgv = (ImageView) findViewById(R.id.imgv_bluetooth);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(FRAGMENT_POS_FISHING).setIcon(tabIcons[FRAGMENT_POS_FISHING]);
        tabLayout.getTabAt(FRAGMENT_POS_LIST).setIcon(tabIcons[FRAGMENT_POS_LIST]);
        tabLayout.getTabAt(FRAGMENT_POS_RANK).setIcon(tabIcons[FRAGMENT_POS_RANK]);
        tabLayout.getTabAt(FRAGMENT_POS_INFO).setIcon(tabIcons[FRAGMENT_POS_INFO]);
        //서비스 스타트 // BleManager 초기화됨
        doStartService();

        Button mNewFileBtn = (Button) findViewById(R.id.btn_newfile);
        mNewFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFile.createFile(MainActivity.this);
            }
        });
        Button mReadBtn = (Button) findViewById(R.id.btn_fileread);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ReadActivity.class));
            }
        });

        GPSPermission.checkGpsService(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE:
                Log.d(TAG, "onActivityResult " + resultCode + " FROM " + "DeviceList Activity");
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Attempt to connect to the device

                    if (address != null && mService != null)
                        mService.connectDevice(address);
                }

                break;

            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a BT session
                    mService.setupBLE();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.e(TAG, "BT is not enabled");
                    Toast.makeText(this, "블루투스가 꺼져 있습니다. 폰의 Settings 에서 블루투스를 활성화 하세요", Toast.LENGTH_SHORT).show();
                }
                break;

            case FISH_CATCH:
                if (resultCode == Activity.RESULT_OK) {
                    mFish = data.getParcelableExtra("fish");
                    removeCatchedFish(mFish);
                    this.sendFishToServer();
                }
                break;

            case USER_EDIT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show();
                    //프래그먼트 갱신
                    if (mForth_fragment != null)
                        mForth_fragment.reload();
                }
                break;

        }    // End of switch(requestCode)
    }


    //뒤로가기
    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "on SaveInstanceState");
        outState.putString("mConnectedDevice", mConnectedDevice);
        outState.putSerializable("user", User.getInstance());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "ON Stop");
        savedCatchedFishToDB();
    }

    /*************************************
     * ********* Private Method ***********
     *************************************/

    private void doStartService() {
        Log.d(TAG, "# Activity - doStartService()");
        startService(new Intent(this, BTCTemplateService.class));
        getApplicationContext().bindService(new Intent(this, BTCTemplateService.class), mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void bluetoothInitialize() {
        Log.d(TAG, "# Activity - initialize()");

        // 블루투스를 지원 가능한지 확인.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "블루투스를 지원하지 않는 기기 입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //서비스를 셋팅함
        mService.setupService(mBlueToothHandler);

        // 블루투스 활성화를 확인하고, 활성화 되어있지 않으면 활성화 요청
        if (!mService.isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    private void sendFishToServer() {
        new HttpSaveFishAsyncTask().execute();
    }

    private void sendFishImageToServer() {
        Bitmap mFishPicture = BitmapFactory.decodeFile(BitmapManager.getImagePath());
        if (mFishPicture != null) {
            Log.d(TAG, "SAVE FISH IMAGE " + mFishPicture.getWidth() + "*" + mFishPicture.getHeight());
            HttpSendImgAsyncTask httpSendImgAsyncTask = new HttpSendImgAsyncTask();
            httpSendImgAsyncTask.execute();
        }
    }

    private void addCatchedFish(Fish catchedFish) {
        mCatchedFishs.add(catchedFish);
        CatchedFishFAB catchedFishFAB = new CatchedFishFAB(MainActivity.this, catchedFish);
        catchedFishFAB.setOnLongClickListener(mFABLongClickListener);
        mCatchedFishsFABMap.put(catchedFish.getId(), catchedFishFAB);
        mCatchedFishsFAM.addMenuButton(catchedFishFAB);
        mCountCatchedFishTv.setText(mCatchedFishs.size() + "");
        mCountCatchedFishInLayoutTv.setText(mCatchedFishs.size() + " 마리");
    }

    private void removeCatchedFish(Fish fish) {
        Fish catchedFish = null;
        for (int i = 0; i < mCatchedFishs.size(); i++) {
            if (mCatchedFishs.get(i).getId().equals(fish.getId())) {
                catchedFish = mCatchedFishs.get(i);
            }
        }

        if (catchedFish != null) {
            mCatchedFishs.remove(catchedFish);
            mCatchedFishsFAM.removeMenuButton(mCatchedFishsFABMap.get(catchedFish.getId()));
        }

        mCountCatchedFishTv.setText(mCatchedFishs.size() + "");
        mCountCatchedFishInLayoutTv.setText(mCatchedFishs.size() + " 마리");
    }


    /******************************************************************************************************************************************/

    /******************************************
     * public method
     *********************************************************************/
    public void savedCatchedFishToDB() {
        mCatchedFishDBHelper.saveCatchedFishsToDB(mCatchedFishs);
    }

    /******************************************************************************************************************************************/


    public class BackPressCloseSystem {
        private long backKeyPressedTime = 0;
        private Toast toast;

        private MainActivity activity;

        public BackPressCloseSystem(MainActivity activity) {
            this.activity = activity;
        }

        public void onBackPressed() {

            if (isAfter2Seconds()) {
                backKeyPressedTime = System.currentTimeMillis();
                // 현재시간을 다시 초기화

                toast = Toast.makeText(activity,
                        "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.",
                        Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            if (isBefore2Seconds()) {
                programShutdown();
                toast.cancel();
            }
        }

        private Boolean isAfter2Seconds() {
            return System.currentTimeMillis() > backKeyPressedTime + 2000;
            // 2초 지났을 경우
        }

        private Boolean isBefore2Seconds() {
            return System.currentTimeMillis() <= backKeyPressedTime + 2000;
            // 2초가 지나지 않았을 경우
        }

        private void programShutdown() {
            activity.savedCatchedFishToDB();

            activity.moveTaskToBack(true);
            activity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());

            System.exit(0);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFirst_fragment = new FirstFragment();
                    //set Handler
                    mFirst_fragment.setmDataLogicHandler(mDataLogicHandler);
                    //FirtFragment에 현재 연결된 디바이스 넘김 // null = 연결 안되있음
                    Bundle oneBundle = new Bundle(1); // 1 <- 전달된 데이터 개수
                    oneBundle.putString(FirstFragment.CONNECTED_DEVICE, mConnectedDevice);
                    mFirst_fragment.setArguments(oneBundle);

                    return mFirst_fragment;
                case 1:
                    return new SecondFragment();
                case 2:
                    return new ThirdFragment();
                case 3:
                    mForth_fragment = new ForthFragment();
                    return mForth_fragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // FRAGMENT_COUNT개의 페이지로 구성됨
            return FRAGMENT_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    public class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Receives BT state messages from service  " + msg.what);
            String deviceName = null;
            switch (msg.what) {
                // Receives BT state messages from service
                // and updates BT state UI

                //초기값
                case Constants.MESSAGE_BT_STATE_INITIALIZED:
                    break;

                //스캔 완료
                case Constants.MESSAGE_BT_SCAN_FINISHED:
                    break;

                case Constants.MESSAGE_BT_STATE_DISCONNECT:
                    Picasso.with(MainActivity.this).load(R.drawable.icon_bluetooth_unconnected).into(mBluetoothImgv);
                    Toast.makeText(mContext, "'" + mConnectedDevice + "'" + " 연결이 끊어졌습니다.", Toast.LENGTH_LONG).show();

                    //연결된 장치 NULL
                    mConnectedDevice = null;

                    //fragment 갱신
                    mFirst_fragment.setmConnectedDevice(mConnectedDevice);
                    mFirst_fragment.reload();

                    break;

                // 디바이스 연결 실패
                case Constants.MESSAGE_BT_STATE_CONNECT_FAIL:

                    //선택한 디바이스 이름을 가져옴
                    deviceName = mService.getDeviceName();

                    Picasso.with(MainActivity.this).load(R.drawable.icon_bluetooth_unconnected).into(mBluetoothImgv);

                    if (deviceName != null) {
                        if (deviceName.equals(mConnectedDevice)){
                            Toast.makeText(mContext, "'" + deviceName + "'" + " 연결 되어있습니다.", Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(mContext, "'" + deviceName + "'" + " 연결 실패하였습니다.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(mContext, "'" + "-----" + "'" + " 연결 실패하였습니다.", Toast.LENGTH_LONG).show();
                    }
                    break;

                //연결 성공
                case Constants.MESSAGE_BT_STATE_CONNECTED:
                    if (mService != null) {

                        //선택한 디바이스 이름을 가져옴
                        deviceName = mService.getDeviceName();

                        //연결에 성공했으므로, 연결된 디바이스를 선택한 디바이스로 설정
                        mConnectedDevice = deviceName;

                        //fragment 갱신
                        mFirst_fragment.setmConnectedDevice(mConnectedDevice);
                        mFirst_fragment.reload();

                        //블루투스 아이콘 변경
                        Picasso.with(MainActivity.this).load(R.drawable.icon_bluetooth_connected).into(mBluetoothImgv);

                        if (deviceName != null) {
                            Toast.makeText(mContext, "'" + deviceName + "'" + " 연결되었습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mContext, "'" + "-----" + "'" + " 연결되었습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;

                //블루투스로 부터 데이터를 받아옴
                case Constants.MESSAGE_READ_CHAT_DATA:
                    if (msg.obj != null) {
                        String str = (String) msg.obj;
                        String[] splited = str.split("/");

                        String angleStr = splited[0];
                        Double angle = Double.parseDouble(angleStr);
                        angle = Formatter.setFormat(angle);

                        String powerStr = splited[1];
                        Double power = Double.parseDouble(powerStr);
                        power = Formatter.setFormat(power);

                        ///////////////////////////////////////////////////////////////////////
                        try {
                            if (MyFile.getMyFile() == null){
                                MyFile.createFile(MainActivity.this);
                            } else if (MyFile.getMyFile() != null) {
                                BufferedWriter file = new BufferedWriter(new FileWriter(MyFile.getMyFile(), true));
                                String str2 = Time.getDate() + "  " + Time.getTime() + "        " + angleStr + "     " + powerStr;
                                file.write(str2);
                                file.newLine();
                                file.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ///////////////////////////////////////////////////////////////////////
                        if (mFirst_fragment != null){
                            mFirst_fragment.updateData(new Data(angle, power));
                        }
                    }
                    break;

                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }    // End of class ActivityHandler

    public class DataLogicHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FirstFragment.DO_BITED: //잡음
                    long bitedTime = (long) msg.obj;
                    Intent intent = new Intent(MainActivity.this, BitedActivity.class);
                    intent.putExtra("bitedTime", bitedTime);
                    startActivity(intent);
                    break;
                    
                case FirstFragment.DO_MISSING: //놓침
                    break;

                case FirstFragment.DO_CATHCED: // 낚음
                    Fish catchedFish = (Fish) msg.obj;
                    addCatchedFish(catchedFish);
                    mCatchedFishsFAM.toggle(true);
                    break;
            }
        }
    }

    class HttpSendImgAsyncTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "HTTP_SEND_IMAGE_TASK";

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
            mProgressDialog = ProgressDialog.show(MainActivity.this, "", "로딩중...", true, false);
        }

        @Override
        protected String doInBackground(Void... params) {
            //serverURL 가져옴
            reqeustURL = HttpManager.getSaveFishImageURL();

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

                String id = mFish.getId();
                String filename = mFish.getImageFile();

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
                int bytesAvailable = fileInputStream.available();
                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

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
        }
    }

    class HttpSaveFishAsyncTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "HttpSaveFishAsyncTask";

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
            mProgressDialog = ProgressDialog.show(MainActivity.this, "", "로딩중...", true, false);
        }

        /**
         * Sub Thread
         */
        @Override
        protected String doInBackground(Void... params) {
            Log.d(this.TAG, mFish.toHttpParameter());
            String result = null;
            try {
                //serverURL 가져옴
                reqeustURL = HttpManager.getSaveFishURL();

                //Parameter
                parameter = mFish.toHttpParameter();

                //최종 접속 URL
                Log.d(TAG, "URL " + reqeustURL);

                //String->URL
                URL url = new URL(reqeustURL);

                //Http Connect
                HttpURLConnection connect = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성.
                Log.d(TAG, "Connect  " + connect);

                connect.setConnectTimeout(3000);

                connect.setRequestMethod("POST"); // post 통신
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
            return result;
        }


        /***
         * MainThread
         */

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.cancel();
            if (mFish.getImageFile() != null)
                sendFishImageToServer();
        }
    }


}

