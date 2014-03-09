package com.alberto.GeoSmartScheduler;

import static com.alberto.GeoSmartScheduler.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.alberto.GeoSmartScheduler.CommonUtilities.EXTRA_MESSAGE;
import static com.alberto.GeoSmartScheduler.CommonUtilities.SENDER_ID;
import static com.alberto.GeoSmartScheduler.CommonUtilities.START_TEST;

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	// App context
	final Context appContext = this;
	// label to display messages
	TextView lblMessage;
	// Async task
	AsyncTask<Void, Void, Void> mRegisterTask;
	MockLocationProvider mMockGpsProviderTask;
	// Connection detector
	ConnectionDetector cd;

	public static String name;
	public static String email;
	private String regId = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lblMessage = (TextView) findViewById(R.id.lblMessage);
		lblMessage.setMovementMethod(new ScrollingMovementMethod());

		// Set name and email for device
		name = "Alberto-Wildfire";
		email = "alberto.liu.project@gmail.com";

		// Check if Internet present
		cd = new ConnectionDetector(getApplicationContext());
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			CommonUtilities.displayMessage(getApplicationContext(),
					"Please connect to working Internet connection");
			// stop executing code by return
			return;
		}

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);

		// Register Broadcast receivers
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));

		registerReceiver(mHandleGPSstart, new IntentFilter(START_TEST));

		/*
		 * Erase flag is registered on server to force to register again ->
		 * GCMRegistrar.setRegisteredOnServer(this, false); If we receive an
		 * error in the server like Not registered, we should unregister the
		 * device in order to obtain a new id -> GCMRegistrar.unregister(this);
		 */

		// Get GCM registration id
		if (GCMRegistrar.isRegistered(this)) {
			regId = GCMRegistrar.getRegistrationId(this);
		}
		// Log.d(TAG, "Registration ID of device:" + regId);

		// Check if regId already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				Toast.makeText(getApplicationContext(),
						"Already registered with GCM", Toast.LENGTH_LONG)
						.show();
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// Register on our server
						// On server creates a new user
						ServerUtilities.register(context, name, email, regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}

	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message depending upon your app
			 * requirement For now i am just displaying it on the screen
			 * */

			// Showing received message
			lblMessage.append(newMessage + "\n");
			Toast.makeText(getApplicationContext(),
					"New Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	/**
	 * Receiving start test signal
	 */
	private final BroadcastReceiver mHandleGPSstart = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			// Start mock location provider if it is not already started
			if (mMockGpsProviderTask == null || !mMockGpsProviderTask.isAlive()) {
				try {
					mMockGpsProviderTask = new MockLocationProvider(appContext,
							intent.getStringExtra("gpsFile"));
					mMockGpsProviderTask.start();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

		}
	};

	public void onClickStartTest(View v) {

		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file10.txt",
						"file10.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file20.txt",
						"file20.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file40.txt",
						"file40.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file50.txt",
						"file50.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file60.txt",
						"file60.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file64K.txt",
						"file64.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file70.txt",
						"file70.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file80.txt",
						"file80.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file90.txt",
						"file90.txt");
		CommonUtilities
				.get(appContext,
						"http://192.168.1.6/server/debuging/httpThroughput.php?file=file100.txt",
						"file100.txt");
	}

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
			e.printStackTrace();
		}
		try {
			if (mMockGpsProviderTask != null) {
				mMockGpsProviderTask.interrupt();
				mMockGpsProviderTask = null;
			}
			unregisterReceiver(mHandleGPSstart);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

}
