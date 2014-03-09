package com.alberto.GeoSmartScheduler;

import java.util.LinkedList;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.alberto.networkmap.NetworkMap;
import com.alberto.networkmap.UTMLocation;

/**
 * This class implements the scheduler that use the network map to improve
 * resource consumption
 * 
 * @author Alberto García
 * 
 */
public class NetworkMapScheduler extends IntentService {

	private static final String TAG = "NaiveScheduler";
	private NetworkMap mNetworkMap;
	private LocationManager locationManager;
	private LinkedList<Bundle> fifo;

	public NetworkMapScheduler() {
		super("NetworkMapScheduler");
	}

	@Override
	public void onCreate() {
		mNetworkMap = new NetworkMap(getApplication());
		locationManager = (LocationManager) this
		        .getSystemService(Context.LOCATION_SERVICE);
		super.onCreate();
		fifo = new LinkedList<Bundle>();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		/*
		 * the scheduler receives intents with the info of the update to be
		 * retrieved and queue each request in the FIFO queue
		 */
		String id_twt = intent.getExtras().getString("id_twt");
		String size = intent.getExtras().getString("size");
		Bundle incoming_update = new Bundle();
		incoming_update.putString("id_twt", id_twt);
		incoming_update.putString("size", size);
		fifo.add(incoming_update);
		/*
		 * Until the queue is empty, the scheduler request the update to the
		 * server attending to the conditions
		 */
		while (!fifo.isEmpty()) {

			// Request network performance at current location
			Bundle data = mNetworkMap.locationBW();
			float bandwidth = -1;
			if (data != null) {
				bandwidth = data.getFloat("bandwidth");
			}
			Log.d(TAG, "The average bandwidth in the location is " + bandwidth);
			// If the network performance is greater or there is no info in the
			// map for the current location, the scheduler issues a request
			if (bandwidth > CommonUtilities.THRESHOLD || bandwidth == -1) {
				Bundle update = fifo.poll();
				requestUpdate(update.getString("id_twt"),
				        update.getString("size"));

			} else {
				// Otherwise the scheduler waits until the devices changes its
				// position and the network performance is reevaluated
				Log.d(TAG,
				        "there is not enough bandwidth to download update from server");
				Location location = null;
				UTMLocation UTMLoc_before = null;
				if (data != null) {
					UTMLoc_before = data.getParcelable("location");
				} else {
					Location x = locationManager
					        .getLastKnownLocation(CommonUtilities.MY_GPS);
					UTMLoc_before = new UTMLocation(x,
					        UTMLocation.ACCURACY_1000M);
				}
				UTMLocation UTMLoc_now = new UTMLocation(UTMLoc_before);
				// The scheduler checks if the current UTM Location is different
				// from the previous UTM location
				// until a UTM location transitions is detected
				while (UTMLoc_now.isEqual(UTMLoc_before)) {
					// get last know location
					if (locationManager
					        .isProviderEnabled(CommonUtilities.MY_GPS)) {
						location = locationManager
						        .getLastKnownLocation(CommonUtilities.MY_GPS);
						UTMLoc_now = new UTMLocation(location,
						        UTMLoc_before.getGranularity());
					} else {
						Log.d(TAG, "Error retriving location from provider "
						        + CommonUtilities.MY_GPS);
						break;
					}
				}
			}
		}
	}

	/**
	 * This method issues a request to the server to retrieve a update
	 * 
	 * @param id_twt
	 *            Tweet ID
	 * @param size
	 *            Tweet size
	 */
	private void requestUpdate(String id_twt, String size) {
		Location location = null;
		String serverUrl = CommonUtilities.SERVER_URL_FILE;
		String latitude = "0";
		String longitude = "0";
		UTMLocation utmLocation = null;
		LocationManager locationManager = (LocationManager) this
		        .getSystemService(Context.LOCATION_SERVICE);

		if (locationManager.isProviderEnabled(CommonUtilities.MY_GPS)) {
			// Wait until get a location
			while (location == null) {
				location = locationManager
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

		// Send GET request to obtain the file
		CommonUtilities.get(getBaseContext(), serverUrl, id_twt);

	}

	@Override
	public void onDestroy() {
		// Log.d(TAG,"NaiveScheduler OnDestroy called");
		super.onDestroy();
	}

}
