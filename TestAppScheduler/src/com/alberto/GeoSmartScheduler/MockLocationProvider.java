package com.alberto.GeoSmartScheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * This class implements the methods that mock the location provider and allows
 * us to emulate the movement of the device
 * 
 * @author Alberto García
 * 
 */
public class MockLocationProvider extends Thread implements LocationListener {

	private List<String> data;
	private LocationManager locationManager;
	/**
	 * Name of our location provider
	 */
	private String mocLocationProvider;
	private String TAG = "MockLocationProvider";
	/**
	 * File with the locations to be mocked
	 */
	private String gpsFile = "BusPath1.txt";

	/**
	 * Constructor of the class
	 * 
	 * @param applicationContext
	 *            Application context
	 * @param gps_file
	 *            File with the locations
	 * @throws IOException
	 */
	public MockLocationProvider(Context applicationContext, String gps_file)
	        throws IOException {
		setName("MockLocationProvider");
		data = new ArrayList<String>();
		if (gps_file != null && gps_file != "") {
			gpsFile = gps_file;
			Log.d(TAG, gpsFile);
		}

		mocLocationProvider = CommonUtilities.MY_GPS;
		locationManager = (LocationManager) applicationContext
		        .getSystemService(Context.LOCATION_SERVICE);

		// If the mocking location provider already exists remove it to be added
		// later
		if (locationManager.getProvider(mocLocationProvider) != null) {
			Log.w(TAG, "Removing provider " + mocLocationProvider);
			locationManager.removeTestProvider(mocLocationProvider);
		}

		// Add mocking location provider
		if (locationManager.getProvider(mocLocationProvider) == null) {
			Log.w(TAG, "Adding provider " + mocLocationProvider + " again");
			locationManager.addTestProvider(mocLocationProvider, false, false,
			        false, false, false, false, false,
			        android.location.Criteria.POWER_LOW,
			        android.location.Criteria.ACCURACY_FINE);
			locationManager.setTestProviderEnabled(mocLocationProvider, true);
		}

		if (locationManager.isProviderEnabled(mocLocationProvider)) {
			locationManager.requestLocationUpdates(mocLocationProvider, 0, 0,
			        this);
		}

		// Load locations from gps file
		try {
			InputStream is = applicationContext.getAssets().open(gpsFile);
			BufferedReader reader = new BufferedReader(
			        new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {

				data.add(line);
			}
			// Log.d("gps", data.size() + " lines");

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		// Start thread which read a location every second and passes each
		// location to the used location provider
		String[] locations = new String[data.size()];
		data.toArray(locations);
		for (String str : locations) {

			try {

				Thread.sleep(1000);

			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			// Set one position
			String[] parts = str.split(",");
			Double latitude = Double.valueOf(parts[0]);
			Double longitude = Double.valueOf(parts[1]);
			// We use the
			Location location = new Location(mocLocationProvider);
			location.setLatitude(latitude);
			location.setLongitude(longitude);
			// set the time in the location. If the time on this location
			// matches the time on the one in the previous set call, it will be
			// ignored
			location.setTime(System.currentTimeMillis());
			location.setAccuracy(25);

			// Log.d(TAG, location.toString());

			try {
				locationManager.setTestProviderLocation(mocLocationProvider,
				        location);
				// Log.w(TAG,
				// "LastKnownLocation of "+LocationManager.NETWORK_PROVIDER+" is: "+locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
				// Log.w(TAG,
				// "LastKnownLocation of "+mocLocationProvider+" is: "+locationManager.getLastKnownLocation(mocLocationProvider));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread x = currentThread();
			if (x.isInterrupted()) {
				return;
			}
		}
		return;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

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
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
