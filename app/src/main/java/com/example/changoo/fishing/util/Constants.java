package com.example.changoo.fishing.util;

public class Constants {


	// Service handler message key
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_NAME = "device_name";
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS = "device_address";
	public static final String SERVICE_HANDLER_MSG_KEY_TOAST = "toast";
    
    // Preference
	public static final String PREFERENCE_NAME = "btchatPref";
	public static final String PREFERENCE_KEY_BG_SERVICE = "BackgroundService";
	public static final String PREFERENCE_CONN_INFO_ADDRESS = "device_address";
	public static final String PREFERENCE_CONN_INFO_NAME = "device_name";
	
    // Message types sent from Service to Activity
    public static final int MESSAGE_CMD_ERROR_NOT_CONNECTED = -50;

    public static final int MESSAGE_BT_NEW_DEVICE = 112;

    public static final int MESSAGE_BT_SCAN_STARTED = 9;
    public static final int MESSAGE_BT_SCANING = 10;
    public static final int MESSAGE_BT_SCAN_FINISHED = 11;
    
    public static final int MESSAGE_BT_STATE_INITIALIZED = 1;
    public static final int MESSAGE_BT_STATE_LISTENING = 2;

    public static final int MESSAGE_BT_STATE_CONNECTING = 13;
    public static final int MESSAGE_BT_STATE_CONNECTED = 16;
    public static final int MESSAGE_BT_STATE_CONNECT_FAIL =17;
    public static final int MESSAGE_BT_STATE_DISCONNECT =18;

    public static final int MESSAGE_BT_STATE_ERROR = 10;

    public static final int MESSAGE_READ_CHAT_DATA = 201;

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	
}
