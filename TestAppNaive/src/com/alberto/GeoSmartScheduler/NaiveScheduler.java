package com.alberto.GeoSmartScheduler;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.alberto.networkmap.UTMLocation;

public class NaiveScheduler extends IntentService {

	public NaiveScheduler() {
		super("NaiveScheduler");
	}

	private static final String TAG = "NaiveScheduler";
	private LocationManager mlocationManager;

	@Override
	public void onCreate() {
		mlocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Location location = null;
		String id_twt = intent.getExtras().getString("id_twt");
		String size = intent.getExtras().getString("size");
		String serverUrl = CommonUtilities.SERVER_URL_FILE;
		String latitude = "0";
		String longitude = "0";
		UTMLocation utmLocation = null;

		if (mlocationManager.isProviderEnabled(CommonUtilities.MY_GPS)) {
			// Wait until get a location
			while (location == null) {
				location = mlocationManager
						.getLastKnownLocation(CommonUtilities.MY_GPS);
			}
			if (location != null) {
				latitude = String.valueOf(location.getLatitude());
				longitude = String.valueOf(location.getLongitude());
				utmLocation = new UTMLocation(location,
						UTMLocation.ACCURACY_1000M);
			}
		}
		long timeInMillis = System.currentTimeMillis();
		/*
		 * Log.i(CommonUtilities.TAG_DOWNLOAD_LOCATION, CommonUtilities
		 * .getDate(timeInMillis, "dd/MM/yyyy HH:mm:ss.SSS") + "|" +
		 * timeInMillis + "|" + id_twt + "|" + latitude + "|" + longitude + "|"
		 * + utmLocation.toString());
		 */

		// Send GET request to obtain the file
		CommonUtilities.get(getBaseContext(), serverUrl, id_twt);
		CommonUtilities
		.appendLog(
				"DOWNLOAD_LOCATION.txt",
				CommonUtilities.getDate(timeInMillis,
						"dd/MM/yyyy HH:mm:ss.SSS")
						+ "|"
						+ timeInMillis
						+ "|"
						+ id_twt
						+ "|"
						+ latitude
						+ "|"
						+ longitude
						+ "|"
						+ utmLocation.toString()
						+ "|" + size);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
