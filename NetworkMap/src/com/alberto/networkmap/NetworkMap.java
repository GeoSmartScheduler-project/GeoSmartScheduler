package com.alberto.networkmap;

import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Messenger;

/**
 * 
 * This class defines the interface to access the data in the Network Map. If an
 * application want to access the map, it should create an instance of this
 * class and use the defined methods to obtain the information.
 * 
 * @Use First create and instance of the class using the constructor. Then
 *      access to one of the two methods {@code locationBW()} or
 *      {@code pathBW()}
 * 
 * @author Alberto García
 * 
 */
public class NetworkMap {

	/* Attributes */
	// Debug attributes
	static final String TAG = "ServiceNetworkMap";
	private Context mContext;
	// Location attributes
	private LocationManager locationManager = null;
	private String mProvider = null;
	private UTMLocation locationOfMeasurement;

	// Messages
	public static final int MSG_LOCATION_BW = 0;
	public static final int MSG_PATH_BW = 1;
	// Target we publish for clients to send messages to IncomingHandler.
	protected Messenger mMessenger = null;

	public NetworkMap(Context context) {

		mContext = context;
		// Get and instance of the system´s Location Manager
		locationManager = (LocationManager) mContext
		        .getSystemService(Context.LOCATION_SERVICE);
		// If we use the MockLocation provider to test , retrieve location from
		// it

		if (locationManager.isProviderEnabled("MY_GPS_PROVIDER")) {
			mProvider = "MY_GPS_PROVIDER";
			// Otherwise use on of the built-in location provider
			// (NETWORK_PROVIDER or GPS_PROVIDER)
		} else if (locationManager
		        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			mProvider = LocationManager.NETWORK_PROVIDER;
		} else if (locationManager
		        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			mProvider = LocationManager.GPS_PROVIDER;
		} else {
			// If none of the above location provider is enabled then find a
			// provider by criteria
			Criteria criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			mProvider = locationManager.getBestProvider(criteria, true);
		}

	}

	/**
	 * Request the network performance data stored in the network map for the
	 * current location
	 * 
	 * @return A object Bundle with the bandwidth and the UTM location in case
	 *         of success
	 *         {@code Bundle["bandwidth"->float;"location"->UTMLocation]}
	 *         otherwise {@code null}
	 */
	public Bundle locationBW() {

		float bandwidth = 0;
		Bundle data = null;
		// Obtain current location
		Location x = locationManager.getLastKnownLocation(mProvider);
		if (x != null) {
			// Create new UTMLocation with the current location
			locationOfMeasurement = new UTMLocation(x,
			        Config.getNetworkMapAccuracy());//UTMLocation.ACCURACY_100M
			// Get the network performance for the current location
			bandwidth = lotactionData(locationOfMeasurement);
			// Create the bundle for the response
			if (bandwidth > -1) {
				data = new Bundle();
				data.putFloat("bandwidth", bandwidth);
				data.putParcelable("location", locationOfMeasurement);
			}

		}
		return data;
	}

	/**
	 * Request the network performance data stored in the network map for the
	 * current location and the next location in the path
	 * 
	 * @return A object Bundle with the network performance of the current
	 *         location and next location in case of success
	 *         {@code Bundle["START_BW"->float;"END_BW"->float;]} otherwise
	 *         {@code null}
	 */
	public Bundle pathBW() {

		Bundle data = null;
		// Obtain current location
		Location x = locationManager.getLastKnownLocation(mProvider);
		if (x != null) {
			// Create new UTMLocation with the current location
			locationOfMeasurement = new UTMLocation(x,
			        UTMLocation.ACCURACY_100M);
			// Get the network performance for the current location and the next
			// location
			data = pathData(locationOfMeasurement);

		}
		return data;
	}

	/**
	 * This method retrieves the network performance for the location passed as
	 * an argument and the network performance for the next more likely location
	 * of destination for the user who is moving from the passed location
	 * 
	 * @param oLocation
	 *            The current {@code UTMLocation} for which the network
	 *            performance is retrieved and used as the starting position for
	 *            the path
	 * 
	 * @return A object Bundle with both network performance parameters, one for
	 *         the start location and the other one for the end location
	 *         {@code Bundle["START_BW"->float;"END_BW"->float;]}. In case of
	 *         error {@code null} will be returned.
	 */
	private Bundle pathData(UTMLocation oLocation) {

		int endUTMzone;
		String endUTMband;
		int endUTMeasting;
		int endUTMnorthing;
		@SuppressWarnings("unused")
		float probability = 0;
		// Starting location from the user is currently departing
		UTMLocation startLocation = new UTMLocation(oLocation);
		float startBW = 0;
		float endBW = 0;
		Bundle response = null;

		NetworkMapDataSource DSNetworkMap = new NetworkMapDataSource(mContext);

		DSNetworkMap.open();
		// Find the next more likely location
		Cursor cursor = DSNetworkMap.selectMostLikelyPath(startLocation);

		if (cursor.moveToFirst()) {
			endUTMzone = cursor.getInt(5);
			endUTMband = cursor.getString(6);
			endUTMeasting = cursor.getInt(7);
			endUTMnorthing = cursor.getInt(8);
			probability = cursor.getFloat(10);
			// Get the network performance for the found location
			Cursor cur = DSNetworkMap.selectBWSample(endUTMzone, endUTMband,
			        endUTMeasting, endUTMnorthing);
			endBW = cur.getFloat(0);
			cur.close();
		}

		cursor.close();
		// Get the network performance for the start location
		cursor = DSNetworkMap.selectBWSample(startLocation);
		if (cursor.moveToFirst()) {
			startBW = cursor.getFloat(0);
		}
		cursor.close();
		DSNetworkMap.close();
		// Create the bundle response with both network performance parameters
		response = new Bundle();
		response.putFloat("START_BW", startBW);
		response.putFloat("END_BW", endBW);
		return response;
	}

	/**
	 * This method retrieves the network performance for the location passed as
	 * an argument
	 * 
	 * @param oLocation
	 *            The current {@code UTMLocation} for which the network
	 *            performance is retrieved
	 * 
	 * @return The network performance for the passed location as a
	 *         {@code float}. In case of error {@code -1} will be returned
	 */
	private float lotactionData(UTMLocation oLocation) {
		NetworkMapDataSource DSNetworkMap = new NetworkMapDataSource(mContext);
		// Get the network performance for the passed location
		DSNetworkMap.open();
		Cursor cursor = DSNetworkMap.selectBWSample(oLocation);
		if (cursor.moveToFirst()) {
			// Read row obtained
			float bandwidth = 0;
			bandwidth = cursor.getFloat(0);
			cursor.close();
			DSNetworkMap.close();
			return bandwidth;
		} else {
			// If there is no data return -1
			cursor.close();
			DSNetworkMap.close();
			return -1;
		}

	}

}
