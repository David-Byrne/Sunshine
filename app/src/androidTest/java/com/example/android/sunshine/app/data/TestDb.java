package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
       // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable() {
        insertLocation();
    }

    public void testWeatherTable() {
        long locRowId = insertLocation();
        ContentValues weatherValues = TestUtilities.createWeatherValues(locRowId);

        assertFalse("Error: Location Not Inserted Correctly", locRowId == -1L);

        WeatherDbHelper dbHelper= new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long weatherRowID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,weatherValues);
        assertTrue(weatherRowID != -1);

        Cursor cursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME,
                null,//all columns
                null,//colums for where clause
                null,//values for where clause
                null,//columns to group by
                null,//columns to filter by row groups
                null);//sort order

        assertTrue("Error, no records returned from location query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Weather query validation failed", cursor, weatherValues);
        assertFalse("Error, more than one record returned from location query", cursor.moveToNext());

        cursor.close();
        db.close();

    }

    public long insertLocation() {
        WeatherDbHelper dbHelper= new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        long locationRowID = db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,testValues);
        assertTrue(locationRowID != -1);

        Cursor cursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
                null,//all columns
                null,//colums for where clause
                null,//values for where clause
                null,//columns to group by
                null,//columns to filter by row groups
                null);//sort order

        assertTrue("Error, no records returned from location query", cursor.moveToFirst());
        TestUtilities.validateCurrentRecord("Location query validation failed", cursor, testValues);
        assertFalse("Error, more than one record returned from location query", cursor.moveToNext());

        cursor.close();
        db.close();

        return locationRowID;
    }
}