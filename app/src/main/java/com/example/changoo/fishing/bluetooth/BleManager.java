package com.example.changoo.fishing.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;


public class BleManager {

    // Debugging
    private static final String TAG = "BleManager";

    // Constants that indicate the current connection state

    public static final int STATE_ERROR = -1;
    public static final int STATE_NONE = 0;        // Initialized
    public static final int STATE_IDLE = 1;        // 연결되있지 않음

    public static final int STATE_SCANNING = 10;    // 스캔 중
    public static final int STATE_SCAN_FINISHED = 11;    // 스캔 완료

    public static final int STATE_CONNECTING = 13;    // 연결 중
    public static final int STATE_CONNECTED = 16;    // 연결 성공
    public static final int STATE_CONNECT_FAIL = 17; // 연결 실패
    public static final int STATE_DISCONNECT = 18; // 연결 실패

    // Message types sent from the BluetoothManager to Handler
    public static final int MESSAGE_STATE_CHANGE = 1; //블루투스 상태변화
    public static final int MESSAGE_READ = 2; // 메세지 읽음

    public static final long SCAN_PERIOD = 5 * 1000;    // Stops scanning after a pre-defined scan period.

    // System, Management
    private static Context mContext = null;
    private static BleManager mBleManager = null;        // Singleton pattern
    private final Handler mHandler;


    // Bluetooth
    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mDefaultDevice = null;

    private BluetoothGatt mBluetoothGatt = null;

    private ArrayList<BluetoothGattService> mGattServices
            = new ArrayList<BluetoothGattService>();
    private BluetoothGattService mDefaultService = null;
    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics
            = new ArrayList<BluetoothGattCharacteristic>();
    private ArrayList<BluetoothGattCharacteristic> mWritableCharacteristics
            = new ArrayList<BluetoothGattCharacteristic>();
    private BluetoothGattCharacteristic mDefaultChar = null;


    // Parameters
    private int mState = -1;


    /**
     * Constructor. Prepares a new Bluetooth session.
     *
     * @param context The UI Activity Context
     * @param handler A Listener to receive messages back to the UI Activity
     */
    private BleManager(Context context, Handler h) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = h;
        mContext = context;
        if (mContext == null){ return; }
    }

    public synchronized static BleManager getInstance(Context c, Handler h) {
        if (mBleManager == null){
            mBleManager = new BleManager(c, h);
        }

        return mBleManager;
    }

    public synchronized void finalize() {
        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mState = STATE_IDLE;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            disconnect();
        }

        mDefaultDevice = null;
        mBluetoothGatt = null;
        mDefaultService = null;
        mGattServices.clear();
        mGattCharacteristics.clear();
        mWritableCharacteristics.clear();

        if (mContext == null) {
            return;
        }

        // Don't forget this!!
        // Unregister broadcast listeners
//		mContext.unregisterReceiver(mReceiver);
    }



    /*****************************************************
     *	Private methods
     ******************************************************/

    /**
     * This method extracts UUIDs from advertised data
     * Because Android native code has bugs in parsing 128bit UUID
     * use this method instead.
     */

    private void stopScanning() {
        if (mState < STATE_CONNECTING) {
            mState = STATE_IDLE;
            //스캔 완료 메세지를 보냄
            mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_SCAN_FINISHED, 0).sendToTarget();
        }
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    /**
     * Check services and looking for writable characteristics
     */
    private int checkGattServices(List<BluetoothGattService> gattServices) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return -1;
        }

        for (BluetoothGattService gattService : gattServices) {
            // Default service info
            // Remember service
            mGattServices.add(gattService);

            // Extract characteristics
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                // Remember characteristic
                mGattCharacteristics.add(gattCharacteristic);


                boolean isWritable = isWritableCharacteristic(gattCharacteristic);
                if (isWritable) {
                    mWritableCharacteristics.add(gattCharacteristic);
                }

                boolean isReadable = isReadableCharacteristic(gattCharacteristic);
                if (isReadable) {
                    readCharacteristic(gattCharacteristic);
                }

                if (isNotificationCharacteristic(gattCharacteristic)) {
                    setCharacteristicNotification(gattCharacteristic, true);
                    if (isWritable && isReadable) {
                        mDefaultChar = gattCharacteristic;
                    }
                }
            }
        }

        return mWritableCharacteristics.size();
    }

    private boolean isWritableCharacteristic(BluetoothGattCharacteristic chr) {
        if (chr == null) return false;

        final int charaProp = chr.getProperties();
        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isReadableCharacteristic(BluetoothGattCharacteristic chr) {
        if (chr == null){ return false;}

        final int charaProp = chr.getProperties();
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            Log.d("BTC Template", "# Found readable characteristic");
            return true;
        } else {
            Log.d("BTC Template", "# Not readable characteristic");
            return false;
        }
    }

    private boolean isNotificationCharacteristic(BluetoothGattCharacteristic chr) {
        if (chr == null) { return false; }

        final int charaProp = chr.getProperties();
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            Log.d("BTC Template", "# Found notification characteristic");
            return true;
        } else {
            Log.d("BTC Template", "# Not notification characteristic");
            return false;
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d("BTC Template", "# BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d("BTC Template", "# BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }


    /*****************************************************
     * Public methods
     ******************************************************/

    public void setScanCallback(BluetoothAdapter.LeScanCallback cb) {
        mLeScanCallback = cb;
    }

    public int getState() {
        return mState;
    }

    public boolean scanLeDevice(final boolean enable) {
        boolean isScanStarted = false;
        if (enable) {
            if (mState == STATE_SCANNING){
                return false;
            }

            if (mBluetoothAdapter.startLeScan(mLeScanCallback)) {
                mState = STATE_SCANNING;
                mDeviceList.clear();

                // If you want to scan for only specific types of peripherals
                // call below function instead
                //startLeScan(UUID[], BluetoothAdapter.LeScanCallback);

                // 일정 시간후 스캔 종료
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanning();
                    }
                }, SCAN_PERIOD);

                isScanStarted = true;
            }
        } else { // 스캔중일때 다시 누를 경우
            if (mState < STATE_CONNECTING) {
                mState = STATE_IDLE;
                mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_IDLE, 0).sendToTarget();
            }
            stopScanning();
        }

        return isScanStarted;
    }

    /**
     *블루투스 디바이스 연결
     */
    public boolean connectGatt(Context c, boolean bAutoReconnect, BluetoothDevice device) {
        if (c == null || device == null){
            return false;
        }
        
        mGattServices.clear();
        mGattCharacteristics.clear();
        mWritableCharacteristics.clear();

        mBluetoothGatt = device.connectGatt(c, bAutoReconnect, mGattCallback);
        mDefaultDevice = device;

        mState = STATE_CONNECTING;
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTING, 0).sendToTarget();
        return true;
    }


    /**
     *블루투스 디바이스 연결
     */
    public boolean connectGatt(Context c, boolean bAutoReconnect, String address) {
        if (c == null || address == null){
            return false;
        }

        if (mBluetoothGatt != null && mDefaultDevice != null
                && address.equals(mDefaultDevice.getAddress())) {
            if (mBluetoothGatt.connect()) {
                mState = STATE_CONNECTING;
                return true;
            }
        }

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (device == null) {
            Log.d("BTC Template", "# Device not found.  Unable to connect.");
            return false;
        }

        mGattServices.clear();
        mGattCharacteristics.clear();
        mWritableCharacteristics.clear();

        mBluetoothGatt = device.connectGatt(c, bAutoReconnect, mGattCallback);
        mDefaultDevice = device;

        mState = STATE_CONNECTING;
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTING, 0).sendToTarget();
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d("BTC Template", "# BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }


    public void setWritableCharacteristic(BluetoothGattCharacteristic chr) {
        mDefaultChar = chr;
    }

    public ArrayList<BluetoothGattService> getServices() {
        return mGattServices;
    }

    public ArrayList<BluetoothGattCharacteristic> getCharacteristics() {
        return mGattCharacteristics;
    }

    public ArrayList<BluetoothGattCharacteristic> getWritableCharacteristics() {
        return mWritableCharacteristics;
    }


    /*****************************************************
     * Handler, Listener, Timer, Sub classes
     ******************************************************/

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //mConnectedDevice=BluetoothAdapter.getDefaultAdapter().getName();
                mState = STATE_CONNECTED;
                Log.d(TAG, "# Connected to GATT server.");
                mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTED, 0).sendToTarget();
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "# Disconnected from GATT server.");

                //연결 시도 중 연결 실패
                if(mState == STATE_CONNECTING){
                    mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECT_FAIL, 0).sendToTarget();
                } else if(mState == STATE_CONNECTED) { // 연결되있는 상태에서 연결 실패
                    mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_DISCONNECT, 0).sendToTarget();
                    gatt.disconnect();
                }

                mState = STATE_IDLE;

                mBluetoothGatt = null;
                mGattServices.clear();
                mDefaultService = null;
                mGattCharacteristics.clear();
                mWritableCharacteristics.clear();
                mDefaultChar = null;
                mDefaultDevice = null;
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "# New GATT service discovered.");
                checkGattServices(gatt.getServices());
            } else {
                Log.d(TAG, "# onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // We've received data from remote
                Log.d(TAG, "# Read characteristic: " + characteristic.toString());
            }
        }

        /**
         *  데이터를 받았을 경우
          */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                //for(byte byteChar : data)
                //	stringBuilder.append(String.format("%02X ", byteChar));
                stringBuilder.append(data);
                Log.d(TAG, stringBuilder.toString());

                mHandler.obtainMessage(MESSAGE_READ, new String(data)).sendToTarget();
            }

            if (mDefaultChar == null && isWritableCharacteristic(characteristic)) {
                mDefaultChar = characteristic;
            }
        }

        ;
    };


}
