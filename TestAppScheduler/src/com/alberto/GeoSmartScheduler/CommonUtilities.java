package com.alberto.GeoSmartScheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.util.Log;

import com.alberto.networkmap.NetworkMonitor;

/**
 * This class contains several values an method commonly used in the application
 * 
 * @author Alberto García
 * 
 */
public final class CommonUtilities {

	static final String MY_GPS = "MY_GPS_PROVIDER";
	// give your server registration URL here
	static final String SERVER_URL_REGISTER = "http://192.168.1.6/server/register.php";
	static final String SERVER_URL_REQUEST = "http://192.168.1.6/server/request.php";
	static final String SERVER_URL_RSS = "http://192.168.1.6/server/sendRSS.php";
	static final String SERVER_URL_IMAGE = "http://192.168.1.6/server/sendImage.php";
	static final String SERVER_URL_FILE = "http://192.168.1.6/server/sendFile.php";
	static final String SERVER_URL_TEST = "http://192.168.1.6/server/test/test.php";
	// Google project id
	static final String SENDER_ID = "243839256156";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "CommonUtilities";
	static final String TAG_NOTIFICATIONS = "GSS-notifications";
	static final String TAG_DOWNLOAD_TIME = "GSS-download-time";
	static final String TAG_DOWNLOAD_LOCATION = "GSS-download-location";
	static final String TAG_HTTP_THROUGHPUT = "GSS-http-throughput";

	static final String DISPLAY_MESSAGE_ACTION = "com.alberto.GeoSmartScheduler.DISPLAY_MESSAGE";
	static final String START_TEST = "com.alberto.GeoSmartScheduler.START_TEST";

	static final String EXTRA_MESSAGE = "message";

	// Threshold used by the scheduler
	static final float THRESHOLD = 2000;

	/**
	 * Notifies UI to display a message.
	 * 
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

	/**
	 * This method issues a POST request to retrieve several tweets
	 * 
	 * @param URL
	 *            Server URL
	 * @param jsonIDtweets
	 *            Tweet ID formated as JSON
	 * @param numberTweets
	 *            Number of tweets to be retrieved
	 * @return
	 */
	static JSONObject post(String URL, JSONObject jsonIDtweets,
	        String numberTweets) {
		try {

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(URL);

			JSONObject jsonObjSend = new JSONObject();
			try {
				// Add key/value pairs
				jsonObjSend.put("num_tweets", numberTweets);
				jsonObjSend.put("array_id_twt", jsonIDtweets);
				// Output the JSON object we're sending to Logcat:
				// Log.d(TAG, jsonObjSend.toString(2));

			} catch (JSONException e) {
				e.printStackTrace();
			}
			StringEntity se;
			se = new StringEntity(jsonObjSend.toString());

			// Set HTTP parameters
			httpPost.setEntity(se);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// Execute the request
			// Log.d(CommonUtilities.TAG_DOWNLOAD,
			// System.currentTimeMillis()+"|"+jsonIDtweets.toString());
			long tStart = System.currentTimeMillis();
			HttpResponse resp = httpClient.execute(httpPost);
			long tEnd = System.currentTimeMillis();
			Log.i(TAG_DOWNLOAD_TIME, System.currentTimeMillis() + "|"
			        + jsonIDtweets.getString("id0") + "|" + tStart + "|" + tEnd
			        + "|" + (tEnd - tStart));
			// Log.d(TAG, "HttpResponse received in [" + (tEnd-tStart)+ "ms]");
			// Log.d(CommonUtilities.TAG_DOWNLOAD,
			// System.currentTimeMillis()+"|"+jsonIDtweets.toString());
			// Get hold of the response entity (-> the data):
			HttpEntity entity = resp.getEntity();
			StatusLine statLine = resp.getStatusLine();

			if (statLine.getStatusCode() == HttpStatus.SC_ACCEPTED
			        && entity != null) {
				// Read the content stream
				InputStream instream = entity.getContent();
				// long size = entity.getContentLength();
				// convert content stream to a String
				String resultString = convertStreamToString(instream);
				instream.close();
				resultString = resultString.substring(2,
				        resultString.length() - 2); // remove wrapping "[" and
				                                    // "]"

				// Transform the String into a JSONObject
				JSONObject jsonObjRecv = new JSONObject(resultString);

				return jsonObjRecv;
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return null;
	}

	/**
	 * 
	 * To convert the InputStream to String we use the BufferedReader.readLine()
	 * method. We iterate until the BufferedReader return null which means
	 * there's no more data to read. Each line will appended to a StringBuilder
	 * and returned as String.
	 * 
	 * @param is
	 *            The input stream to be converted
	 * @return The string obtained from the input stream
	 */
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * This method issues a GET request to the given URL requesting an specific
	 * tweet
	 * 
	 * @param context
	 *            Application context
	 * @param URL
	 *            Server URL
	 * @param idtwt
	 *            Tweet ID
	 * @return {@code TRUE} if success otherwise {@code FALSE}
	 */
	static boolean get(Context context, String URL, String idtwt) {

		int statusCode;
		byte[] readBuffer = new byte[1024];
		int readLen;
		int totalBodyLen = 0;
		InputStream inputStream;
		long size = 0;
		long ContentLength = 0;
		long HeadersLen = 0;
		NetworkMonitor mNetworkMonitor = new NetworkMonitor(context);

		try {

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(URL);

			// Monitoring Start
			mNetworkMonitor.StartMonitoring();
			long RxBytesStart = TrafficStats.getTotalRxBytes();
			long tStart = System.currentTimeMillis();
			// Execute the request
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine != null) {
				statusCode = statusLine.getStatusCode();

				if (statusCode == HttpStatus.SC_OK) {
					HttpEntity responseEntity = response.getEntity();
					ContentLength = responseEntity.getContentLength();

					if (responseEntity != null) {
						inputStream = responseEntity.getContent();
						while ((readLen = inputStream.read(readBuffer)) > 0) {
							totalBodyLen += readLen;
						}
						// Monitoring Stop
						long tEnd = System.currentTimeMillis();
						long RxBytesEnd = TrafficStats.getTotalRxBytes();
						mNetworkMonitor.StopMonitoring();
						/*
						 * calculate headers length, add +2 to each line because
						 * of the "\r\n" and extra 2 bytes at the end of the
						 * header for other "\r\n"
						 */
						Header[] responseHeaders = response.getAllHeaders();
						if (responseHeaders != null) {
							String headers = "";
							for (Header hdr : responseHeaders) {
								HeadersLen += hdr.toString().length() + 2;
								headers += hdr.toString() + "\r\n";
							}
							headers += "\r\n";
							HeadersLen += 2;
							// Log.d(TAG, headers);
						}
						size = HeadersLen + totalBodyLen;
						// Log information
						// Log.d(TAG, "Header length: " + HeadersLen
						// + "|Body length: " + totalBodyLen);
						long timeInMillis = System.currentTimeMillis();
						/*
						 * Log.i(TAG_DOWNLOAD_TIME, getDate(timeInMillis,
						 * "dd/MM/yyyy HH:mm:ss.SSS") + "|" + timeInMillis + "|"
						 * + idtwt + "|" + tStart + "|" + tEnd + "|" + (tEnd -
						 * tStart));
						 */

						appendLog(
						        "DOWNLOAD_TIME.txt",
						        getDate(timeInMillis, "dd/MM/yyyy HH:mm:ss.SSS")
						                + "|"
						                + timeInMillis
						                + "|"
						                + idtwt
						                + "|"
						                + tStart
						                + "|"
						                + tEnd
						                + "|"
						                + (tEnd - tStart));
						/*
						 * Log.i(TAG_HTTP_THROUGHPUT, getDate(timeInMillis,
						 * "dd/MM/yyyy HH:mm:ss.SSS") + "|" + timeInMillis + "|"
						 * + idtwt + "|" + (tEnd - tStart) + "|" + (RxBytesEnd -
						 * RxBytesStart) + "|" + size + "|" + (((RxBytesEnd -
						 * RxBytesStart) * 8) / (tEnd - tStart)) + "|" +
						 * (((size) * 8) / (tEnd - tStart)));
						 */
						appendLog(
						        "HTTP_THROUGHPUT.txt",
						        getDate(timeInMillis, "dd/MM/yyyy HH:mm:ss.SSS")
						                + "|"
						                + timeInMillis
						                + "|"
						                + idtwt
						                + "|"
						                + (tEnd - tStart)
						                + "|"
						                + (RxBytesEnd - RxBytesStart)
						                + "|"
						                + size
						                + "|"
						                + (((RxBytesEnd - RxBytesStart) * 8) / (tEnd - tStart))
						                + "|"
						                + (((size) * 8) / (tEnd - tStart)));

						displayMessage(context, "File downloaded: " + idtwt
						        + "|Bandwidth experimented (TrafficStats)["
						        + ((RxBytesEnd - RxBytesStart) * 8)
						        / (tEnd - tStart) + "Kbps]"
						        + "|Bandwidth experimented (read size)["
						        + ((size) * 8) / (tEnd - tStart) + "Kbps]");
						return true;
					}
					mNetworkMonitor.CancelMonitoring();
					return false;
				}
				mNetworkMonitor.CancelMonitoring();
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Return date in specified format.
	 * 
	 * @param milliSeconds
	 *            Date in milliseconds
	 * @param dateFormat
	 *            Date format
	 * @return String representing date in specified format
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getDate(long milliSeconds, String dateFormat) {
		// Create a DateFormatter object for displaying date in specified
		// format.
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		// Create a date object that will convert the milliseconds to date.
		return formatter.format(new java.util.Date(milliSeconds));
	}

	/**
	 * This method create a log file if not exist and write data of the
	 * application in the file
	 * 
	 * @param log
	 *            Name of the log accessed
	 * @param text
	 *            Line to write in the log
	 */
	public static void appendLog(String log, String text) {
		File logFile = new File("sdcard/" + log);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
			        true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
