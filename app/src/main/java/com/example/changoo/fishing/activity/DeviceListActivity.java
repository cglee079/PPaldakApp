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

package com.example.changoo.fishing.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.changoo.fishing.R;
import com.example.changoo.fishing.bluetooth.BleManager;

import java.util.ArrayList;
import java.util.Set;


/**
 * It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */

public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    
    // Constants
	public static final long SCAN_PERIOD = 8*1000;	// Stops scanning after a pre-defined scan period.

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private ActivityHandler mActivityHandler;
    private BluetoothAdapter mBtAdapter;
    private BleManager mBleManager;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private ArrayList<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>(); 

    // UI stuff
    private Button mScanBtn = null;
    private TextView mStateTv=null;
    private ProgressBar mStateProgressBar=null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // 취소 할 경우 캔슬
        setResult(Activity.RESULT_CANCELED);
        mActivityHandler = new ActivityHandler();

        // state 텍스트뷰 가져옴
        mStateTv = (TextView)findViewById(R.id.tv_device_state);

        //  Scan 버튼 설정
        mScanBtn = (Button) findViewById(R.id.btn_scan);
        mScanBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mNewDevicesArrayAdapter.clear();
                doDiscovery();
                mScanBtn.setText(R.string.btn_scaning);
                mScanBtn.setEnabled(false);
                mStateProgressBar.setVisibility(View.VISIBLE);

            }
        });

        mStateProgressBar = (ProgressBar)findViewById(R.id.pb_state);

        // 등록된, 연결가능 어댑터 생성
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_item_device);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_item_device);

        // 등록된 디바이스 ListView
        ListView pairedListView = (ListView) findViewById(R.id.lv_paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // 연결 가능 디바이스 ListView
        ListView newDevicesListView = (ListView) findViewById(R.id.lv_new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 블루투스 어댑터 가져옴
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 블루투스 매니저 가져옴
        mBleManager = BleManager.getInstance(getApplicationContext(), null);
        mBleManager.setScanCallback(mLeScanCallback);

        // 현재 페어링된 디바이스 가져옴
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 페어링된 디바이스 추가
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else { // 페어리된 디바이스가  없는 경우
            String noDevices = "페어링 된 장치가 없습니다.";
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
    }

    /**
     * 디바이스 스캔
     * */
    private void doDiscovery() {
        if(D) Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        mStateTv.setText(R.string.tv_bluetooth_state_searching);

        // Turn on sub-title for new devices
        findViewById(R.id.tv_new_devices).setVisibility(View.VISIBLE);

        // Empty cache
        mDevices.clear();
        
        // 스캔중일때 다시 누를 경우 중지
        if (mBleManager.getState() == BleManager.STATE_SCANNING) {
        	mBleManager.scanLeDevice(false);
        }

        // 스캔 요청
        mBleManager.scanLeDevice(true);
        
		// 정해진 시간이 넘어갈시 스캔 중지
		mActivityHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					stopDiscovery();
				}
			}, SCAN_PERIOD);
    }
    
    /**
     * Stop device discover
     */
    private void stopDiscovery() {
    	// Indicate scanning in the title
    	setProgressBarIndeterminateVisibility(false);
        mStateTv.setText(R.string.tv_bluetooth_state_using);
    	// Show scan button
    	mScanBtn.setEnabled(true);
        mScanBtn.setText(R.string.btn_scan);
    	mBleManager.scanLeDevice(false);
        mStateProgressBar.setVisibility(View.INVISIBLE);

    }
    
    /**
     * Check if it's already cached
     */
    private boolean checkDuplicated(BluetoothDevice device) {
    	for(BluetoothDevice dvc : mDevices) {
    		if(device.getAddress().equalsIgnoreCase(dvc.getAddress())) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            if(info != null && info.length() > 16) {
                String address = info.substring(info.length() - 17);
                Log.d(TAG, "User selected device : " + address);

                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };


    /**
     * BLE scan callback
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = 
    		new BluetoothAdapter.LeScanCallback() {
    	@Override
    	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
            	@Override
            	public void run() {
            		if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            			if(!checkDuplicated(device)) {
                			mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                			mNewDevicesArrayAdapter.notifyDataSetChanged();
            				mDevices.add(device);
            			}
            		}
            	}
            });
    	}
    };
    
	public class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {}
			super.handleMessage(msg);
		}
	}

}
