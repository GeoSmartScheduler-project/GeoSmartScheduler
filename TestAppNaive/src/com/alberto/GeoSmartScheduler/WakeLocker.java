package com.alberto.GeoSmartScheduler;

import android.content.Context;
import android.os.PowerManager;

/**
 * This class implements the methods used to handle the wakelock
 * 
 * @author Alberto García
 * 
 */
public abstract class WakeLocker {
	private static PowerManager.WakeLock wakeLock;

	/**
	 * This method acquires the wakelock
	 * 
	 * @param context
	 *            Application context
	 */
	public static void acquire(Context context) {
		if (wakeLock != null) {
			wakeLock.release();
		}

		PowerManager pm = (PowerManager) context
		        .getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
		        | PowerManager.ACQUIRE_CAUSES_WAKEUP
		        | PowerManager.ON_AFTER_RELEASE, "WakeLock");
		wakeLock.acquire();
	}

	/**
	 * This method releases the wakelock
	 */
	public static void release() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}
