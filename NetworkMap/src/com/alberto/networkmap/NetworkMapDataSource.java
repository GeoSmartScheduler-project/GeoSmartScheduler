package com.alberto.networkmap;

import com.alberto.networkmap.MySQLiteOpenHelper.table_bandwidth_map;
import com.alberto.networkmap.MySQLiteOpenHelper.table_paths_bandwidth_map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * This class managed the operations with the Database where the network
 * performance map is stored
 * 
 * @author Alberto García
 * 
 */
public class NetworkMapDataSource {

	private final String TAG = "NetworkMapDataSource";
	private SQLiteDatabase db;
	private MySQLiteOpenHelper dbHelper = null;

	public NetworkMapDataSource(Context context) {
		dbHelper = new MySQLiteOpenHelper(context);
	}

	/**
	 * Open a connection with the database
	 */
	public void open() {

		try {
			if (dbHelper != null) {
				db = dbHelper.getWritableDatabase();
				// Log.d(TAG, "DB opened");
			}
		} catch (SQLiteException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Close a connection with the database
	 */
	public void close() {
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
			// Log.d(TAG,"DB closed");
		}

	}

	/**
	 * This method insert a new network performance sample into the table
	 * {@code bandwidth_map}
	 * 
	 * @param oLocation
	 *            The {@code UTMLocation} for which the network performance
	 *            sample was taken
	 * @param bandwidth
	 *            The network performance sample which is associated with the
	 *            location
	 */
	public void insertBWSample(UTMLocation oLocation, float bandwidth) {
		// INSERT INTO `bandwidth_map`( `UTMzone`, `UTMband`, `UTMnorthing`,
		// `UTMeasting`, `Bandwidth`, `N_Samples`) VALUES (?,?,?,?,?,?)

		// Set values for the fields of the table
		ContentValues values = new ContentValues();
		values.put(table_bandwidth_map.UTM_ZONE, oLocation.getZone());
		values.put(table_bandwidth_map.UTM_BAND, oLocation.getBand());
		values.put(table_bandwidth_map.UTM_EASTING, oLocation.getUTMe());
		values.put(table_bandwidth_map.UTM_NORTHING, oLocation.getUTMn());
		values.put(table_bandwidth_map.BANDWIDTH, bandwidth);
		values.put(table_bandwidth_map.N_SAMPLES, 1);

		// Insert values
		long idRow = db.insert(MySQLiteOpenHelper.TABLE_BANDWIDTH_MAP, null,
		        values);
		if (idRow == -1) {
			Log.e(TAG, "SQL error in the method insertBWSample() ");
		} else {
			// Log.d(TAG, "Successfully updated with method insertBWSample()");
		}
	}

	/**
	 * This method update a network performance estimate with the info of the
	 * new sample into the table {@code bandwidth_map}
	 * 
	 * @param oLocation
	 *            The {@code UTMLocation} for which the network performance
	 *            sample was taken
	 * @param bandwidth
	 *            The network performance sample which is associated with the
	 *            location
	 */
	public void updateBWSample(UTMLocation oLocation, float bandwidth) {
		// UPDATE `bandwidth_map` SET `Bandwidth`=?,`N_Samples`=`N_Samples`+1
		// WHERE `UTMzone`=? AND `UTMband`=? AND `UTMeasting`=? AND
		// `UTMnorthing`=?

		// bandwidth formula => BW = s x bandwidth + (1-s) BW
		// s=0.125

		// Set the arguments for the where clause
		String[] whereArgs = new String[] {
		        Integer.toString(oLocation.getZone()), oLocation.getBand(),
		        Integer.toString(oLocation.getUTMe()),
		        Integer.toString(oLocation.getUTMn()) };
		// Set sql query
		String sql = "UPDATE bandwidth_map SET bandwidth=("
		        + Float.toString(bandwidth)
		        + "*0.125)+((1-0.125)*bandwidth),n_Samples=n_Samples+1, last_Sample = CURRENT_TIMESTAMP "
		        + "WHERE `utmZone`=? AND `utmBand`=? AND `utmEasting`=? AND `utmNorthing`=? ";

		// Update the table
		try {
			db.execSQL(sql, whereArgs);
			// Log.d(TAG, "Successfully updated with method updateBWSample()");
		} catch (SQLiteException e) {
			// Log.d(TAG, "Unable to update DB with updateBWSample()");
			e.printStackTrace();

		}

	}

	/**
	 * This method retrieves the data stored in the table {@code bandwidth_map}
	 * for the given location
	 * 
	 * @param oLocation
	 *            The {@code UTMLocation} for which the network performance
	 *            sample is retrieved
	 * 
	 * @return A Cursor which can be used to access the results of the query
	 */
	public Cursor selectBWSample(UTMLocation oLocation) {
		// SELECT `Bandwidth` FROM `bandwidth_map` WHERE `UTMzone`=? AND
		// `UTMband`=? AND `UTMeasting`=? AND `UTMnorthing`=?

		// Set the fields to retrieve
		String[] columns = new String[] { table_bandwidth_map.BANDWIDTH };

		// Set the where clause
		String whereClause = "`utmZone`=? AND `utmBand`=? AND `utmEasting`=? AND `utmNorthing`=? ";

		// Set the selection arguments to find the correct row
		String[] selectionArgs = new String[] {
		        Integer.toString(oLocation.getZone()), oLocation.getBand(),
		        Integer.toString(oLocation.getUTMe()),
		        Integer.toString(oLocation.getUTMn()) };
		String groupBy = null;
		String having = null;
		String orderBy = null;

		// Execute the query
		Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_BANDWIDTH_MAP,
		        columns, whereClause, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	/**
	 * This method retrieves the data stored in the table {@code bandwidth_map}
	 * for the location represented for each parameter that compound a UTM
	 * location
	 * 
	 * @param oZone
	 *            The location UTM zone
	 * @param oBand
	 *            The location UTM band
	 * @param oUTMe
	 *            The location UTM easting
	 * @param oUTMn
	 *            The location UTM northing
	 * 
	 * @return A Cursor which can be used to access the results of the query
	 */
	public Cursor selectBWSample(int oZone, String oBand, int oUTMe, int oUTMn) {
		// SELECT `Bandwidth` FROM `bandwidth_map` WHERE `UTMzone`=? AND
		// `UTMband`=? AND `UTMeasting`=? AND `UTMnorthing`=?
		// Set the fields to retrieve
		String[] columns = new String[] { table_bandwidth_map.BANDWIDTH };
		// Set the where clause
		String whereClause = "`utmZone`=? AND `utmBand`=? AND `utmEasting`=? AND `utmNorthing`=? ";
		// Set the selection arguments to find the correct row
		String[] selectionArgs = new String[] { Integer.toString(oZone), oBand,
		        Integer.toString(oUTMe), Integer.toString(oUTMn) };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		// Execute the query
		Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_BANDWIDTH_MAP,
		        columns, whereClause, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	/**
	 * This method insert a new row in the table {@code paths_bandwidth_map}
	 * representing a transaction between two locations
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * @param endLocation
	 *            The {@code UTMLocation} where the transition ends
	 * @param probability
	 *            The probability associated to this transition
	 */
	public void insertPath(UTMLocation startLocation, UTMLocation endLocation,
	        float probability) {
		// INSERT INTO `paths_bandwidth_map`
		// (`id`, `StartUTMzone`, `StartUTMband`, `StartUTMnorthing`,
		// `StartUTMeasting`,
		// `EndUTMzone`, `EndUTMband`, `EndUTMnorthing`, `EndUTMeasting`,
		// `Times`, `Probability`) VALUES (?,?,?,?,?,?,?,?,?,?)

		// Set the values for the fields in the table
		ContentValues values = new ContentValues();
		values.put(table_paths_bandwidth_map.START_UTM_ZONE,
		        startLocation.getZone());
		values.put(table_paths_bandwidth_map.START_UTM_BAND,
		        startLocation.getBand());
		values.put(table_paths_bandwidth_map.START_UTM_EASTING,
		        startLocation.getUTMe());
		values.put(table_paths_bandwidth_map.START_UTM_NORTHING,
		        startLocation.getUTMn());
		values.put(table_paths_bandwidth_map.END_UTM_ZONE,
		        endLocation.getZone());
		values.put(table_paths_bandwidth_map.END_UTM_BAND,
		        endLocation.getBand());
		values.put(table_paths_bandwidth_map.END_UTM_EASTING,
		        endLocation.getUTMe());
		values.put(table_paths_bandwidth_map.END_UTM_NORTHING,
		        endLocation.getUTMn());
		values.put(table_paths_bandwidth_map.TIMES, 1);
		values.put(table_paths_bandwidth_map.PROBABILITY, probability);

		// Insert the new row in the table
		long idRow = db.insert(MySQLiteOpenHelper.TABLE_PATHS_BANDWIDTH_MAP,
		        null, values);

		if (idRow == -1) {
			Log.e(TAG, "SQL error in the method insertPath() ");
		} else {
			// Log.d(TAG, "Successfully updated with method insertPath()");
		}
	}

	/**
	 * This method updates a row of the table {@code paths_bandwidth_map} with
	 * the new data for the transition represented in that row
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * @param endLocation
	 *            The {@code UTMLocation} where the transition ends
	 * @param probability
	 *            The probability associated to this transition
	 */
	public void updateProbabilityPath(UTMLocation startLocation,
	        UTMLocation endLocation, float probability) {
		// UPDATE `paths_bandwidth_map` SET `Probability`=?
		// WHERE `StartUTMzone`=? AND `StartUTMband`=? AND `StartUTMnorthing`=?
		// AND `StartUTMeasting`=?
		// AND `EndUTMzone`=? AND `EndUTMband`=? AND `EndUTMnorthing`=? AND
		// `EndUTMeasting`=?

		// Set where arguments to find the correct row
		String[] whereArgs = new String[] {
		        Integer.toString(startLocation.getZone()),
		        startLocation.getBand(),
		        Integer.toString(startLocation.getUTMe()),
		        Integer.toString(startLocation.getUTMn()),
		        Integer.toString(endLocation.getZone()), endLocation.getBand(),
		        Integer.toString(endLocation.getUTMe()),
		        Integer.toString(endLocation.getUTMn()) };

		// Set the sql query
		String sql = "UPDATE `paths_bandwidth_map` SET `probability`="
		        + probability
		        + " WHERE `startUTMzone`=? AND `startUTMband`=? AND `startUTMeasting`=? AND `startUTMnorthing`=? "
		        + " AND `endUTMzone`=? AND `endUTMband`=? AND `endUTMeasting`=? AND `endUTMnorthing`=? ";

		// Execute the query
		try {
			db.execSQL(sql, whereArgs);
			// Log.d(TAG,
			// "Successfully updated with method updateProbabilityPath()");
		} catch (SQLiteException e) {
			// Log.d(TAG, "Unable to update DB with updateProbabilityPath()");
			e.printStackTrace();

		}
	}

	/**
	 * This method updates the field {@code Times} into the table
	 * {@code paths_bandwidth_map} which represents the number of times that a
	 * transition between locations has been detected
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * @param endLocation
	 *            The {@code UTMLocation} where the transition ends
	 */
	public void updateTimesPath(UTMLocation startLocation,
	        UTMLocation endLocation) {
		// UPDATE `paths_bandwidth_map` SET `Times`=`Times`+1
		// WHERE `StartUTMzone`=? AND `StartUTMband`=? AND `StartUTMnorthing`=?
		// AND `StartUTMeasting`=?
		// AND `EndUTMzone`=? AND `EndUTMband`=? AND `EndUTMnorthing`=? AND
		// `EndUTMeasting`=?

		// Set where arguments to find the correct row
		String[] whereArgs = new String[] {
		        Integer.toString(startLocation.getZone()),
		        startLocation.getBand(),
		        Integer.toString(startLocation.getUTMe()),
		        Integer.toString(startLocation.getUTMn()),
		        Integer.toString(endLocation.getZone()), endLocation.getBand(),
		        Integer.toString(endLocation.getUTMe()),
		        Integer.toString(endLocation.getUTMn()) };

		// Set the sql query
		String sql = "UPDATE `paths_bandwidth_map` SET `times`=`times`+1"
		        + " WHERE `startUTMzone`=? AND `startUTMband`=? AND `startUTMeasting`=? AND `startUTMnorthing`=? "
		        + " AND `endUTMzone`=? AND `endUTMband`=? AND `endUTMeasting`=? AND `endUTMnorthing`=? ";

		// Execute the query
		try {
			db.execSQL(sql, whereArgs);
			// Log.d(TAG, "Successfully updated with method updateTimesPath()");
		} catch (SQLiteException e) {
			// Log.d(TAG, "Unable to update DB with updateTimesPath()");
			e.printStackTrace();

		}
	}

	/**
	 * This method select the data for the transition stored in the table
	 * {@code paths_bandwidth_map}
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * @param endLocation
	 *            The {@code UTMLocation} where the transition ends
	 * 
	 * @return A Cursor which can be used to access the results of the query
	 */
	public Cursor selectPath(UTMLocation startLocation, UTMLocation endLocation) {
		// SELECT * FROM `paths_bandwidth_map` WHERE `StartUTMzone`=? AND
		// `StartUTMband`=? AND `StartUTMnorthing`=? AND `StartUTMeasting`=?
		// AND `EndUTMzone`=? AND `EndUTMband`=? AND `EndUTMnorthing`=? AND
		// `EndUTMeasting`=?

		String[] columns = null;
		// Set where clause
		String whereClause = "`startUTMzone`=? AND `startUTMband`=? AND `startUTMeasting`=? AND `startUTMnorthing`=? "
		        + "AND `endUTMzone`=? AND `endUTMband`=? AND `endUTMeasting`=? AND `endUTMnorthing`=? ";

		// Set the arguments to select the correct row
		String[] selectionArgs = new String[] {
		        Integer.toString(startLocation.getZone()),
		        startLocation.getBand(),
		        Integer.toString(startLocation.getUTMe()),
		        Integer.toString(startLocation.getUTMn()),
		        Integer.toString(endLocation.getZone()), endLocation.getBand(),
		        Integer.toString(endLocation.getUTMe()),
		        Integer.toString(endLocation.getUTMn()) };

		String groupBy = null;
		String having = null;
		String orderBy = null;
		// Execute the query
		Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_PATHS_BANDWIDTH_MAP,
		        columns, whereClause, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	/**
	 * This method retrieves the most likely transition for the starting given
	 * location
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * 
	 * @return A Cursor which can be used to access the results of the query
	 */
	public Cursor selectMostLikelyPath(UTMLocation startLocation) {
		// SELECT * FROM `paths_bandwidth_map` WHERE `StartUTMzone`=? AND
		// `StartUTMband`=? AND `StartUTMnorthing`=? AND `StartUTMeasting`=?
		// ORDER BY `probability` DESC LIMIT 1

		String[] columns = null;
		// Set where clause
		String whereClause = "`startUTMzone`=? AND `startUTMband`=? AND `startUTMeasting`=? AND `startUTMnorthing`=? ";

		// Set the arguments to select the correct row
		String[] selectionArgs = new String[] {
		        Integer.toString(startLocation.getZone()),
		        startLocation.getBand(),
		        Integer.toString(startLocation.getUTMe()),
		        Integer.toString(startLocation.getUTMn()) };

		String groupBy = null;
		String having = null;
		// Set the order for the results
		String orderBy = "`probability` DESC";
		// Set the limit of results to 1 to get the most likely transition
		String limit = "1";
		// Execute the query
		Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_PATHS_BANDWIDTH_MAP,
		        columns, whereClause, selectionArgs, groupBy, having, orderBy,
		        limit);
		return cursor;
	}

	/**
	 * This method retrieves all the transitions that start in the given
	 * location
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * 
	 * @return A Cursor which can be used to access the results of the query
	 */
	public Cursor selectAllPath(UTMLocation startLocation) {
		// SELECT * FROM `paths_bandwidth_map` WHERE `StartUTMzone`=? AND
		// `StartUTMband`=? AND `StartUTMnorthing`=? AND `StartUTMeasting`=?

		String[] columns = null;
		// Set where clause
		String whereClause = "`startUTMzone`=? AND `startUTMband`=? AND `startUTMeasting`=? AND `startUTMnorthing`=?";

		// Set the arguments to select the correct row
		String[] selectionArgs = new String[] {
		        Integer.toString(startLocation.getZone()),
		        startLocation.getBand(),
		        Integer.toString(startLocation.getUTMe()),
		        Integer.toString(startLocation.getUTMn()) };

		String groupBy = null;
		String having = null;
		String orderBy = null;
		// Execute the query
		Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_PATHS_BANDWIDTH_MAP,
		        columns, whereClause, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	/**
	 * This method states whether or not exist a row into the table
	 * {@code bandwidth_map} for the given location
	 * 
	 * @param oLocation
	 *            The {@code UTMLocation} to check the existence of data
	 * 
	 * @return {@code TRUE} if there is a row otherwise {@code FALSE}
	 */
	public boolean existsBWSample(UTMLocation oLocation) {

		Cursor cur = selectBWSample(oLocation);

		if (cur.getCount() != 0) {
			cur.close();
			return true;
		} else {
			cur.close();
			return false;
		}
	}

	/**
	 * This method states whether or not exist a row into the table
	 * {@code paths_bandwidth_map} for the transition represented for the given
	 * locations
	 * 
	 * @param startLocation
	 *            The {@code UTMLocation} where the transition starts
	 * @param endLocation
	 *            The {@code UTMLocation} where the transition ends
	 * 
	 * @return {@code TRUE} if there is a row otherwise {@code FALSE}
	 */
	public boolean existsPath(UTMLocation startLocation, UTMLocation endLocation) {

		Cursor cur = selectPath(startLocation, endLocation);

		if (cur.getCount() != 0) {
			cur.close();
			return true;
		} else {
			cur.close();
			return false;
		}
	}

}
