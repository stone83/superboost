package com.ccmt.library.util;

import java.util.Locale;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class NetUtil {

	public static final int TYPE_NO_NET = 0;
	public static final int TYPE_WIFI = 1;
	public static final int TYPE_CMWAP = 2;
	public static final int TYPE_CMNET = 3;
	private static Uri PREFERRED_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");
	public static String proxyIp;
	public static int proxyPort;

	/**
	 * 检查网络,如果是移动网络会设置wap代理ip和端口.
	 * 
	 * @return
	 */
	public static boolean checkNet(Context context) {
		// 检查是否存在可以利用的网络
		// WIFI、手机接入点（APN）
		boolean wifiConnected = isWIFIConnected(context);
		boolean mobileConnected = isMobileConnected(context);
		// 不可以——提示工作
		if (!wifiConnected && !mobileConnected) {
			return false;
		}
		// 可以
		// 明确到底是哪个渠道可以使用
		if (mobileConnected) {
			// 如果当前的是wap方式通信：代理信息——没有固定
			// 信息变动
			// IP是10.0.0.172 端口是80 ip：010.000.000.172 80
			// 读取：数据库
			readAPN(context);
		}
		return true;
	}

	/**
	 * 读取apn信息针对于Wap方式
	 * 
	 * @param context
	 */
	private static void readAPN(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		// 获取当前处于活动状态的APN的信息
		try {
			Cursor query = contentResolver.query(PREFERRED_APN_URI, null, null,
					null, null);
			if (proxyIp == null || proxyPort == 0) {
				if (query != null && query.moveToNext()) {
					proxyIp = query.getString(query.getColumnIndex("proxy"));
					proxyPort = query.getInt(query.getColumnIndex("port"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断wifi是否可以连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWIFIConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.isConnected()
				&& activeNetInfo.getState() == NetworkInfo.State.CONNECTED
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * 判断手机接入点是否可以连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.isConnected()
				&& activeNetInfo.getState() == NetworkInfo.State.CONNECTED
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

	/**
	 * 网络是否可用
	 * 
	 * @param activity
	 * @return
	 */
	// public static boolean isNetworkAvailable(Context context) {
	// ConnectivityManager connectivity = (ConnectivityManager) context
	// .getSystemService(Context.CONNECTIVITY_SERVICE);
	// if (connectivity == null) {
	// return false;
	// }
	// NetworkInfo[] info = connectivity.getAllNetworkInfo();
	// if (info != null) {
	// for (int i = 0; i < info.length; i++) {
	// if (info[i].isConnected()) {
	// if (info[i].getState() == NetworkInfo.State.CONNECTED) {
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }

	/**
	 * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
	 * 
	 * @param context
	 * @return true表示开启
	 */
	public static boolean isGpsEnabled(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
		boolean gps = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
		boolean network = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			return true;
		}
		return false;
	}

	/**
	 * 强制帮用户打开GPS
	 * 
	 * @param context
	 */
	public static void openGPS(Context context) {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}

	/**
	 * wifi是否打开
	 */
	// public static boolean isWifiEnabled(Context context) {
	// ConnectivityManager mgrConn = (ConnectivityManager) context
	// .getSystemService(Context.CONNECTIVITY_SERVICE);
	// TelephonyManager mgrTel = (TelephonyManager) context
	// .getSystemService(Context.TELEPHONY_SERVICE);
	// return ((mgrConn.getActiveNetworkInfo() != null && mgrConn
	// .getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) ||
	// mgrTel
	// .getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
	// }

	/**
	 * 判断当前网络是否是wifi网络
	 * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //判断3G网
	 * 
	 * @param context
	 * @return boolean
	 */
	// public static boolean isWifi(Context context) {
	// ConnectivityManager connectivityManager = (ConnectivityManager) context
	// .getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	// if (activeNetInfo != null && activeNetInfo.isConnected()
	// && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
	// return true;
	// }
	// return false;
	// }

	/**
	 * 判断当前网络是否是3G网络
	 * 
	 * @param context
	 * @return boolean
	 */
	// public static boolean is3G(Context context) {
	// ConnectivityManager connectivityManager = (ConnectivityManager) context
	// .getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	// if (activeNetInfo != null && activeNetInfo.isConnected()
	// && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
	// return true;
	// }
	// return false;
	// }

	/**
	 * 更加严谨的写法：
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetwork(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * 获取当前的网络状态 ,但是不设置wap代理ip和端口,0代表没有网络,1代表WIFI网络,2代表wap网络,3代表net网络.
	 * 
	 * @param context
	 * @return
	 */
	public static int getAPNType(Context context) {
		int result = 0;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()
				&& networkInfo.getState() == NetworkInfo.State.CONNECTED) {
			int nType = networkInfo.getType();
			if (nType == ConnectivityManager.TYPE_MOBILE) {
				// LogUtils.i("networkInfo.getExtraInfo() is "
				// + networkInfo.getExtraInfo());
				if (networkInfo.getExtraInfo().toLowerCase(Locale.getDefault())
						.equals("cmnet")) {
					result = TYPE_CMNET;
				} else {
					result = TYPE_CMWAP;
				}
			} else if (nType == ConnectivityManager.TYPE_WIFI) {
				// LogUtils.i("wifi");
				result = TYPE_WIFI;
			}
		}
		return result;
	}

}