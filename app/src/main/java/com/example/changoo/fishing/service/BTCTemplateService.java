/*
 * Copyright (C) 2014 Bluetooth Connection Template
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.changoo.fishing.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.changoo.fishing.bluetooth.BleManager;
import com.example.changoo.fishing.bluetooth.ConnectionInfo;

import com.example.changoo.fishing.util.AppSettings;
import com.example.changoo.fishing.util.Constants;


public class BTCTemplateService extends Service {
    private static final String TAG = "LLService";

    // Context, System
    private Context mContext = null;
    private static Handler mActivityHandler = null;
    private ServiceHandler mServiceHandler = new ServiceHandler();
    private final IBinder mBinder = new ServiceBinder();

    // Bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;        // local Bluetooth adapter managed by Android Framework
    private BleManager mBleManager = null;
    private boolean mIsBleSupported = true; // 블루투스를 지원하는가
    private ConnectionInfo mConnectionInfo = null;        // Remembers connection info when BT connection is made


    /*****************************************************
     * Overrided methods
     ******************************************************/
    @Override
    public void onCreate() { //서비스가 최초로 시작됬을때
        Log.d(TAG, "# Service - onCreate() starts here");

        mContext = getApplicationContext();
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "# Service - onStartCommand() starts here");

        // If service returns START_STICKY, android restarts service automatically after forced close.
        // At this time, onStartCommand() method in service must handle null intent.
        return Service.START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // This prevents reload after configuration changes
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "# Service - onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "# Service - onUnbind()");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "# Service - onDestroy()");
        finalizeService();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "# Service - onLowMemory()");
        // onDestroy is not always called when applications are finished by Android system.
        finalizeService();
    }


    /*****************************************************
     * Private methods
     ******************************************************/
    private void initialize() {
        Log.d(TAG, "# Service : initialize --- used by onCreate()");

        AppSettings.initializeAppSettings(mContext);
        startServiceMonitoring();

        //기기가 블루투스를 지원하는지 확인
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            mIsBleSupported = false;
        }

        // Make instances
        mConnectionInfo = ConnectionInfo.getInstance(mContext);

        // Get local Bluetooth adapter
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            // BT is not on, need to turn on manually.
            // Activity will do this.
        } else {
            if (mBleManager == null && mIsBleSupported) {
                setupBLE();
            }
        }
    }

    /*****************************************************
     * Public methods
     ******************************************************/
    //서비스 종료
    public void finalizeService() {
        Log.d(TAG, "# Service : finalize ---used by onDestory()");

        // Stop the bluetooth session
        mBluetoothAdapter = null;
        if (mBleManager != null) {
            mBleManager.finalize();
        }
        mBleManager = null;
    }

    /**
     * Setting up bluetooth connection
     *
     * @param h
     */
    public void setupService(Handler h) {
        mActivityHandler = h;

        // BleManager(싱글톤)를 셋업, 실질적으로 BleManager 최초 초기화 (getInstance 처리)
        if (mBleManager == null)
            setupBLE();


        // TODO: If ConnectionInfo holds previous connection info,
        // try to connect using it.
        if (mConnectionInfo.getDeviceAddress() != null && mConnectionInfo.getDeviceName() != null) {
            //connectDevice(mConnectionInfo.getDeviceAddress());
        } else {
            if (mBleManager.getState() == BleManager.STATE_NONE) {
                // Do nothing
            }
        }
    }

    /**
     * Setup and initialize BLE manager
     */
    public void setupBLE() {
        Log.d(TAG, "Service - setupBLE()");

        // Initialize the BluetoothManager to perform bluetooth le scanning
        if (mBleManager == null)
            mBleManager = BleManager.getInstance(mContext, mServiceHandler);
    }

    /**
     * 블루투스 사용가능한가 확인
     */
    public boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "# Service - cannot find bluetooth adapter. Restart app.");
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Get scan mode
     */
    public int getBluetoothScanMode() {
        int scanMode = -1;
        if (mBluetoothAdapter != null)
            scanMode = mBluetoothAdapter.getScanMode();

        return scanMode;
    }


    /**
     * Connect to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public void connectDevice(String address) {
        if (address != null && mBleManager != null) {
            Log.d(TAG, "Connect Device");

            if (mBleManager.connectGatt(mContext, true, address)) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mConnectionInfo.setDeviceAddress(address);
                mConnectionInfo.setDeviceName(device.getName());
            }
        }
    }

    /**
     * Connect to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public void connectDevice(BluetoothDevice device) {
        if (device != null && mBleManager != null) {
            //mBleManager.disconnect();

            if (mBleManager.connectGatt(mContext, true, device)) {
                mConnectionInfo.setDeviceAddress(device.getAddress());
                mConnectionInfo.setDeviceName(device.getName());
            }
        }
    }

    /**
     * Get connected device name
     */
    public String getDeviceName() {
        return mConnectionInfo.getDeviceName();
    }


    /**
     * Start service monitoring. Service monitoring prevents
     * unintended close of service.
     */
    public void startServiceMonitoring() {
        if (AppSettings.getBgService()) {
            ServiceMonitoring.startMonitoring(mContext);
        } else {
            ServiceMonitoring.stopMonitoring(mContext);
        }
    }


    /*****************************************************
     * Handler, Listener, Timer, Sub classes
     ******************************************************/
    public class ServiceBinder extends Binder {
        public BTCTemplateService getService() {
            return BTCTemplateService.this;
        }
    }

    /**
     * Receives messages from bluetooth manager
     */
    class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                // Bluetooth state changed
                case BleManager.MESSAGE_STATE_CHANGE:
                    // Bluetooth state Changed
                    Log.d(TAG, "Service - MESSAGE FROM BleManager : " + msg.arg1);

                    //핸들로 MAIN ACTIVITY 로 전송
                    switch (msg.arg1) {
                        case BleManager.STATE_NONE: // 0
                            mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_INITIALIZED).sendToTarget();
                            break;

                        case BleManager.STATE_IDLE: // 1
                            mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_INITIALIZED).sendToTarget();
                            break;

                        //블루투스 연결됨
                        case BleManager.STATE_CONNECTED: // 16
                            mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CONNECTED).sendToTarget();
                            break;

                        // 블르투스 연결 시도중 실패
                        case BleManager.STATE_CONNECT_FAIL: //17
                            mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_CONNECT_FAIL).sendToTarget();
                            break;

                        case BleManager.STATE_DISCONNECT: //18
                            mActivityHandler.obtainMessage(Constants.MESSAGE_BT_STATE_DISCONNECT).sendToTarget();
                            break;

                        // 스캔 완료
                        case BleManager.STATE_SCAN_FINISHED: //11
                            mActivityHandler.obtainMessage(Constants.MESSAGE_BT_SCAN_FINISHED).sendToTarget();
                            break;

                    }
                    break;


                // 메세지를 받았을 경우
                case BleManager.MESSAGE_READ:
                    Log.d(TAG, "Service - MESSAGE_READ: ");

                    String strMsg = (String) msg.obj;

                    // send bytes in the buffer to activity
                    if (strMsg != null && strMsg.length() > 0) {
                        mActivityHandler.obtainMessage(Constants.MESSAGE_READ_CHAT_DATA, strMsg)
                                .sendToTarget();
                    }
                    break;

            }    // End of switch(msg.what)

            super.handleMessage(msg);
        }
    }    // End of class MainHandler


}
