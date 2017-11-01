package com.example.changoo.fishing.data;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Handler;

import com.example.changoo.fishing.model.Fish;
import com.example.changoo.fishing.model.User;
import com.example.changoo.fishing.util.Formatter;
import com.example.changoo.fishing.util.GPSPermission;
import com.example.changoo.fishing.util.Time;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class DataManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "DATAMANAGER";

    /**
     * 잡힘 - 물고기 미끼를 뭄 (Bited)
     * 낚음 - 물고기를 건짐 (Cathced)
     * 거치 - 낚시대를 거치하였는지 (Fixed)
     * 단위 - kg
     * 오차범위 - 0.2
     */

    public static final int DO_FIXED 	= 0;
    public static final int DO_NOFIXED 	= 1;
    public static final int DO_BITED 	= 2;
    public static final int DO_RUNAWAY 	= 6;
    public static final int DO_MISSING 	= 3;
    public static final int DO_CATCHED 	= 4;
    public static final int DO_FIGHTED 	= 5;

    public static final int STATE_WAIT 		= 1000;
    public static final int STATE_BITED 	= 1001; // 잡혔는지 or 잡히지 않았는지
    public static final int STATE_RUNAWAY 	= 1002; // 잡혔는지 or 잡히지 않았는지
    public static final int STATE_FIGTHED 	= 1003; //
    public static final int STATE_CATCHED 	= 1004; //
    public static final int STATE_FIXED 	= 1005;

    ///************************ GPS ********************///

    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    GoogleApiClient mGoogleApiClient;

    /////////////////////////////////////////////////////////

    private Fish mFish = null;

    private int state = STATE_WAIT;

    private int fixedTime = 0;
    private int catchedTime = 0;
    private int runAwayTime = 0;
    private long timing = 0;
    
    private final double errorRange = 0.05; // 오차 범위
    private final double bitedPower = 0.2; // 기준점 포인트, catchPoint 만큼 차이 날 경우 물고기가 입질은 뭄
    private final double fightPower =0.6; // 기준점 포인트, catchPoint 만큼 차이 날 경우 물고기가 입질은 뭄
    private final int fixAngle = 1; // 기준점 포인트, catchPoint 만큼 차이 날 경우 물고기가 입질은 뭄
   
    private Data before_data = null; // 직전 데이터 (kg)
    private Data data = null; //데이터 (kg)
   
    private ArrayList<Data> mDatas; // 잡힌 물고기 힘의 집합 //catching 후 부터 입력됨


    private FragmentActivity mContext = null;
    private Handler mHandler = null;

    public DataManager(FragmentActivity mContext, Handler mHandler) {
        this.mContext = mContext;
        this.mHandler = mHandler;

        mDatas = new ArrayList<>();

    }

    public void updateData(Data data) {
        Log.d(TAG, "Update Data : " + data);
        before_data = this.data;
        this.data = data;


        //잡히지 않은 경우
        if (state == STATE_WAIT) {
            checkFixed();
        }

        if (state == STATE_FIXED) {
            checkNoFixed();
            checkBited();
        }

        //잡힌 경우
        if (state == STATE_BITED) {
            checkRunAway();
            checkfight();
        }

        if(state == STATE_RUNAWAY){
            checkFixed();
        }

        if (state == STATE_FIGTHED) {
            mDatas.add(data);
            checkCathced(); // 물고기를 낙은 경우
        }



    }

    public double getMaxPower() {
        if (mDatas.size() > 0) {
            double max = -1;
            for (int i = 0; i < mDatas.size(); i++) {
                if (mDatas.get(i).getPower() > max)
                    max = mDatas.get(i).getPower();
            }
            return max;
        } else
            return 0;
    }

    public double getAvgPower() {
        if (mDatas.size() > 0) {
            double sum = 0;
            for (int i = 0; i < mDatas.size(); i++)
                sum += mDatas.get(i).getPower();
            return Formatter.setFormat(sum / mDatas.size());
        } else
            return 0;
    }


    /**
     * Private Method
     */

    private void checkfight() {
        if (data.getPower() - before_data.getPower() >= fightPower) {
            state = STATE_FIGTHED;
            mHandler.obtainMessage(DO_FIGHTED).sendToTarget();
            timing = Time.capture();
        }
    }

    private void checkFixed() {
        if (data != null && before_data != null) {
            if (before_data.getAngle() < data.getAngle() + fixAngle && before_data.getAngle() > data.getAngle() - fixAngle){
                fixedTime++;
            } else{
                fixedTime = 0;
            }

            if (fixedTime > 10) {//0
                fixedTime = 0;
                state = STATE_FIXED;
                mHandler.obtainMessage(DO_FIXED).sendToTarget();
            }
        }
    }

    private void checkNoFixed() {
        if (before_data.getAngle() < data.getAngle() + fixAngle && before_data.getAngle() > data.getAngle() - fixAngle){
        } else {
            state = STATE_WAIT;
            mHandler.obtainMessage(DO_NOFIXED).sendToTarget();
        }
    }


    //잡힌 것이 물고기인가? (지속적으로 힘이 같다 - 수초)
    private void checkThing() {
    }

    //물고기를 놓친 경우  (힘이 0에 가깝다)
    private void checkMissing() {
        //0의 오차 범위안에 들어온다면
        if (data.getPower() > 0 - errorRange && data.getPower() < 0 + errorRange) {
            mHandler.obtainMessage(DO_MISSING).sendToTarget();
            clearData();
        }
    }

    private void checkCathced() {
        int last = mDatas.size() - 1;
        int before_last = last - 1;
        if (mDatas.size() > 1) {

            if (mDatas.get(last).getPower() - mDatas.get(before_last).getPower() < 0.1) {
                catchedTime++;
                if (catchedTime > 10) {
                    state = STATE_CATCHED;
                    catched();
                }
            } else {
                catchedTime = 0;
            }
        }
    }


    private void checkBited() { //잡혔는지 확인
        if(state == STATE_FIXED) {
            double gapPower = 0;
            if (data != null && before_data != null) {
                gapPower = data.getPower() - before_data.getPower();
            }
            if (gapPower > bitedPower) { //catchPoint 이상 차이날 경우 (물고기가 잡힌 경우)
                Log.i(TAG, "Bited by Something!!");
                state = STATE_BITED;

                //사용자에게 알림
                long bitedTime = Time.capture();
                mHandler.obtainMessage(DO_BITED, bitedTime).sendToTarget();
            }
        }
    }

    private void checkRunAway(){
//        double gapPower = 9999;
//        if (data != null && before_data != null) {
//            gapPower = data.getPower() - before_data.getPower();
//        }
//        if (gapPower < 0.2) {
//            runAwayTime++;
//            if(runAwayTime >15){
//                state=STATE_RUNAWAY;
//                mHandler.obtainMessage(DO_RUNAWAY).sendToTarget();
//            }
//        }
//        else
//            runAwayTime =0;
    }

    private void catched() {
        //update data stop.
        state = STATE_WAIT;

        //get Time
        long time = Time.capture();
        timing -= time;
        timing *= -1;

        //new Fish
        mFish = new Fish();

        //Fish_id = UserID + Time;
        String fish_id = User.getInstance().getId() + "_" + Formatter.toDateTime(time);

        mFish.setId(fish_id);
        mFish.setUser_id(User.getInstance().getId());

        mFish.setAvgFower(this.getAvgPower());
        mFish.setMaxFower(this.getMaxPower());
        mFish.setDate(Formatter.toDate(time));
        mFish.setTime(Formatter.toTime(time));
        mFish.setTimeing(Formatter.toTimeDouble(timing));

        /***********GPS SET **********/
        GPSPermission.checkGpsService(mContext);
        buildGoogleApiClinet();
        mGoogleApiClient.connect();
        /*****************************/

        mHandler.obtainMessage(DO_CATCHED, mFish).sendToTarget();
        clearData();
    }

    private void clearData() {
        before_data = null;
        data = null;
        mDatas.clear();
        
        catchedTime = 0;
        fixedTime 	= 0;
        runAwayTime = 0;
    }

    /**
     * GPS Overide
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Double[] GPS = getCurrentLocation();
        if (GPS != null) {
            mFish.setGPS_lat(GPS[0]);
            mFish.setGPS_lot(GPS[1]);
        } else {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    /****************************
     * GPS
     ***************************************/
    //get Current Location
    private Double[] getCurrentLocation() {
        Double[] GPS;

        try {
            if (GPSPermission.checkPermissions(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                GPSPermission.setmLocationPermissionGranted(true);
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                GPSPermission.requestExternalPermissions(mContext);
            }

            if (mCurrentLocation != null) {
                GPS = new Double[2];
                GPS[0] = mCurrentLocation.getLatitude();
                GPS[1] = mCurrentLocation.getLongitude();
                return GPS;
            } else {
                return null;
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    //CREATE Google Api Client
    protected synchronized void buildGoogleApiClinet() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .enableAutoManage(mContext, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //ms단위
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


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
//            Context baseContext;set
//            Toast.makeText(baseContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
//
//            e.printStackTrace();
        }
        return nowAddress;
    }

}
