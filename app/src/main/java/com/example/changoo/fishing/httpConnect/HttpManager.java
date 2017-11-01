package com.example.changoo.fishing.httpConnect;

/**
 * Created by changoo on 2017-03-07.
 */

public class HttpManager {
	private static final String serverURL 			= "http://192.168.43.75:8080/ServerFishing";
	private static final String LoginURL 			= serverURL + "/login";
	private static final String JoinURL 			= serverURL + "/join";
	private static final String saveUserImageURL 	= serverURL + "/saveUserImage";
	private static final String saveUser 			= serverURL + "/saveUser";

	private static final String showFishURL 		= serverURL + "/showFishsByID";
	private static final String showFishRankURL 	= serverURL + "/showFishsforRank";
	private static final String saveFishImageURL 	= serverURL + "/saveFishImage";
	private static final String saveFishURL 		= serverURL + "/saveFish";
	private static final String deleteFishURL 		= serverURL + "/deleteFish";

	private static final String fishImageURL		= serverURL + "/resources/fish_img/";
	private static final String userImageURL 		= serverURL + "/resources/user_img/";

	public static String getSaveFishURL() {
		return saveFishURL;
	}

	public static String getSaveFishImageURL() {
		return saveFishImageURL;
	}

	public static String getSaveUserImageURL() {
		return saveUserImageURL;
	}

	public static String getServerURL() {
		return serverURL;
	}

	public static String getShowFishURL() {
		return showFishURL;
	}

	public static String getShowFishRankURL() {
		return showFishRankURL;
	}

	public static String getLoginURL() {
		return LoginURL;
	}

	public static String getFishImageURL() {
		return fishImageURL;
	}

	public static String getUserImageURL() {
		return userImageURL;
	}

	public static String getJoinURL() {
		return JoinURL;
	}

	public static String getDeleteFishURL() {
		return deleteFishURL;
	}

	public static String getSaveUser() {
		return saveUser;
	}
}
