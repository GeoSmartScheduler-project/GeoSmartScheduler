package com.alberto.GeoSmartScheduler;

import static com.alberto.GeoSmartScheduler.CommonUtilities.SENDER_ID;
import static com.alberto.GeoSmartScheduler.CommonUtilities.displayMessage;
import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * This class implements the methods used to handle GCM notifications and
 * deliver it to the correct entity
 * 
 * @author Alberto García
 * 
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(SENDER_ID);
	}

	/**
	 * Method called on device registered
	 **/
	@Override
	protected void onRegistered(Context context, String registrationId) {
		// Log.d(TAG, "Device registered: regId = " + registrationId);
		displayMessage(context, "Your device registred with GCM");
		ServerUtilities.register(context, MainActivity.name,
		        MainActivity.email, registrationId);
	}

	/**
	 * Method called on device unregistered
	 * */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		// Log.d(TAG, "Device unregistered");
		displayMessage(context, getString(R.string.gcm_unregistered));
		// ServerUtilities.unregister(context, registrationId);
		GCMRegistrar.setRegisteredOnServer(this, false);
	}

	/**
	 * Method called on Receiving a new message
	 * */
	@Override
	protected void onMessage(Context context, Intent intent) {
		Intent start_intent = new Intent(CommonUtilities.START_TEST);
		String gpsFile = intent.getExtras().getString("gpsFile");
		start_intent.putExtra("gpsFile", gpsFile);
		context.sendBroadcast(start_intent);

		String id_twt = intent.getExtras().getString("message");
		String size = intent.getExtras().getString("size");

		long timeInMillis = System.currentTimeMillis();
		/*
		 * Log.i(CommonUtilities.TAG_NOTIFICATIONS, CommonUtilities
		 * .getDate(timeInMillis, "dd/MM/yyyy HH:mm:ss.SSS") + "|" +
		 * timeInMillis + "|" + id_twt + "|" + size);
		 */
		CommonUtilities.appendLog(
		        "NOTIFICATIONS.txt",
		        CommonUtilities
		                .getDate(timeInMillis, "dd/MM/yyyy HH:mm:ss.SSS")
		                + "|"
		                + timeInMillis + "|" + id_twt + "|" + size);
		Intent schedulerService = new Intent(context, NaiveScheduler.class);
		schedulerService.putExtra("id_twt", id_twt);
		schedulerService.putExtra("size", size);
		startService(schedulerService);
		// Log.d(TAG, "Message passed to Scheduler Service");

	}

	/**
	 * Method called on receiving a deleted message
	 * */
	@Override
	protected void onDeletedMessages(Context context, int total) {
		// Log.d(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		displayMessage(context, message);

	}

	/**
	 * Method called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {
		// Log.d(TAG, "Received error: " + errorId);
		displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	/**
	 * Method called on recoverable Error
	 */
	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		// Log.d(TAG, "Received recoverable error: " + errorId);
		displayMessage(context,
		        getString(R.string.gcm_recoverable_error, errorId));
		return super.onRecoverableError(context, errorId);
	}

}
