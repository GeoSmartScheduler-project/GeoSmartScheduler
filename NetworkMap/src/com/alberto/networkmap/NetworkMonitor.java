package com.alberto.networkmap;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.util.Log;

/**
 * This class implements the methods to take data which are used to determine
 * the network performance
 * 
 * @use To obtain a network performance sample is necesario to call the method
 *      {@code StartMonitoring()} just before start a download from the server
 *      to start monitoring the network received bytes. Upon the download ends,
 *      the method {@code StopMonitoring()} to finish the monitoring and pass
 *      the data to the alignment module. If the download is cancelled the
 *      monitoring should be cancelled by calling {@code CancelMonitoring()}.
 * 
 * @author Alberto García
 * 
 */
public class NetworkMonitor {

	/* Attributes */
	// Debug attributes
	private final String TAG = "NetworkMonitor";
	private Context mContext;
	// Monitoring attributes
	private long startBytes;
	private long startTime;
	private long endTime;
	private long endBytes;
	private UTMLocation locationOfMeasurement;
	private UTMLocation previousLocation;

	// Location attributes
	private LocationManager locationManager = null;
	private LocationListener locationListener;

	/**
	 * Constructor for the class
	 * 
	 * @param context
	 *            The application context
	 */
	public NetworkMonitor(Context context) {

		mContext = context;
		// Acquire a reference to the system Location Manager and set location
		// variables and location listener.
		locationManager = (LocationManager) mContext
		        .getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates

		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.

				UTMLocation newUTMLocation = new UTMLocation(location,
				        Config.getNetworkMapAccuracy());
				// When the devices changes its location, we compare the
				// previous
				// UTM location with the new UTM location
				if (!locationOfMeasurement.isEqual(newUTMLocation)) {
					// If we are in a new UTM Location, the devices has
					// performed a transition between UTM Location which have to
					// be stored in the map
					// store previous location
					previousLocation = new UTMLocation(locationOfMeasurement);
					UTMLocation nextLocation = new UTMLocation(newUTMLocation);

					Log.w(TAG,
					        "Previous utm location: "
					                + previousLocation.toString()
					                + "| Next utm location: "
					                + nextLocation.toString());

					// Send message to the service in charge of align the data
					// with the previous location and the current location
					Intent serviceData = new Intent(mContext,
					        NetworkMapPathAlignment.class);
					serviceData.putExtra("CURRENT_LOCATION", nextLocation);
					serviceData.putExtra("PREVIOUS_LOCATION", previousLocation);
					mContext.startService(serviceData);

				}

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String provider, int status,
			        Bundle extras) {
				// TODO Auto-generated method stub

			}

		};

	}

	/**
	 * This method starts the monitoring of the download to get data from the
	 * network performance
	 */
	public void StartMonitoring() {
		Log.d(TAG, "Monitorization has started");
		/* Restart variables */
		startBytes = 0;
		startTime = 0;
		endBytes = 0;
		endTime = 0;
		String mProvider = null;

		// If we use the MockLocation provider to test , retrieve location from
		// this provider
		if (locationManager.isProviderEnabled("MY_GPS_PROVIDER")) {
			mProvider = "MY_GPS_PROVIDER";

		} else if (locationManager
		        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

			mProvider = LocationManager.NETWORK_PROVIDER;
		} else if (locationManager
		        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			mProvider = LocationManager.GPS_PROVIDER;
		} else {
			Criteria criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			mProvider = locationManager.getBestProvider(criteria, true);
		}

		// Register the listener with the Location Manager to receive
		// location updates
		locationManager.requestLocationUpdates(mProvider, (2 * 1000), 500,
		        locationListener);
		// Store current location in UTM coordinates
		Location x = locationManager.getLastKnownLocation(mProvider);
		if (x != null) {
			locationOfMeasurement = new UTMLocation(x,
			        Config.getNetworkMapAccuracy());
		}

		// get start time to measure elapsed time between start and stop
		startTime = System.currentTimeMillis();
		// gets total bytes received before the download of data
		startBytes = TrafficStats.getTotalRxBytes();
	}

	/**
	 * This method stops the running monitoring and sends the data to the
	 * alignment module
	 */
	public void StopMonitoring() {
		// get stop time to measure elapsed time between start and stop
		endTime = System.currentTimeMillis();
		// get total bytes received after the transmission of data
		endBytes = TrafficStats.getTotalRxBytes();
		// Unregister the listener of location updates
		locationManager.removeUpdates(locationListener);

		Log.d(TAG, "Monitorization has stoped");
		// Send message to the service in charge of align the data
		Intent serviceData = new Intent(mContext, NetworkMapDataAlignment.class);
		serviceData.putExtra("START_TIME", startTime);
		serviceData.putExtra("END_TIME", endTime);
		serviceData.putExtra("START_BYTES", startBytes);
		serviceData.putExtra("END_BYTES", endBytes);
		serviceData.putExtra("LOCATION", locationOfMeasurement);
		mContext.startService(serviceData);

	}

	/**
	 * This method cancels a running monitoring and discard the data already
	 * taken
	 */
	public void CancelMonitoring() {
		// Unregister the listener of location updates
		locationManager.removeUpdates(locationListener);
		/* Restart variables */
		startBytes = 0;
		startTime = 0;
		endBytes = 0;
		endTime = 0;
		Log.d(TAG, "Monitorization has been canceled");
	}

}
