package com.alberto.networkmap;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;

/**
 * This class acts as a Service which is listening for Intents with the data of
 * a transition defined by a start location and a end location. The class
 * processes this data to obtain a user path tree which is properly stored in
 * the network map
 * 
 * @author Alberto García
 * 
 * @use The intent received by this class should contain an extra bundle with
 *      the next fields {@code IntentExtra
 *      ["CURRENT_LOCATION"->parcelabe(UTMLocation), "PREVIOUS_LOCATION"->parcelabe(UTMLocation)]}
 */
public class NetworkMapPathAlignment extends IntentService {

	private final String TAG = "NetworkMapPathAlignment";
	private NetworkMapDataSource DSNetworkMap = null;

	public NetworkMapPathAlignment() {
		super("IntentServiceNetworkMap");
		DSNetworkMap = new NetworkMapDataSource(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Process data to be storage in our Network Map
		Bundle extra = intent.getExtras();
		UTMLocation currentLocation = extra.getParcelable("CURRENT_LOCATION");
		UTMLocation previousLocation = extra.getParcelable("PREVIOUS_LOCATION");
		ProcessData(previousLocation, currentLocation);
	}

	private void ProcessData(UTMLocation previousLocation,
	        UTMLocation currentLocation) {

		int totalTimes = 0;
		int pathTimes = 0;
		Log.w(TAG, "START LOCATION:" + previousLocation.toString()
		        + " END LOCATION:" + currentLocation.toString());

		try {
			if (DSNetworkMap != null) {
				// Open connection
				DSNetworkMap.open();
				if (DSNetworkMap.existsPath(previousLocation, currentLocation)) {
					// If already exists a row for the transition, then update
					// it
					DSNetworkMap.updateTimesPath(previousLocation,
					        currentLocation);
				} else {
					// Otherwise insert a new row with a transition probability
					// of zero
					float probability = 0;
					DSNetworkMap.insertPath(previousLocation, currentLocation,
					        probability);
				}
				// Upon insert or update we need to update the probability of
				// every transition with the same start location
				// Retrieve every row with start location equal to the given
				// start location
				Cursor cur = DSNetworkMap.selectAllPath(previousLocation);
				if (cur.moveToFirst()) {
					// Calculate the total number of transitions which start in
					// the given location
					while (!cur.isAfterLast()) {
						totalTimes = totalTimes + cur.getInt(9);
						cur.moveToNext();
					}
					// Update probability of each transition in the map
					cur.moveToFirst();
					while (!cur.isAfterLast()) {
						pathTimes = cur.getInt(9);
						// The new probability is calculated as the number of
						// times
						// that the transition under evaluation has been
						// detected divide
						// by the total number of transitions
						float prob = (float) pathTimes / (float) totalTimes;
						// Update the probability of the transition under
						// evaluation
						DSNetworkMap.updateProbabilityPath(previousLocation,
						        currentLocation, prob);
						cur.moveToNext();
					}
				}
				// Close the connection
				if (DSNetworkMap != null) {
					DSNetworkMap.close();
					DSNetworkMap = null;
				}
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
			if (DSNetworkMap != null) {
				DSNetworkMap.close();
				DSNetworkMap = null;
			}
			Log.e(TAG,
			        "Error it could not perfom the action against the database");
		}

	}

	@Override
	public void onDestroy() {

		// Close the connection if it still open
		if (DSNetworkMap != null) {
			DSNetworkMap.close();
			DSNetworkMap = null;
		}
		super.onDestroy();
	}
}
