/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.benjaminlize.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
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

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {

        // First step: Get reference to writable database
        WeatherDbHelper dbHelper;
        SQLiteDatabase db;
        Cursor dbCursor;

        int LOCAL_COL_CITY_NAME;
        int LOCAL_COL_COORD_LAT;
        int LOCAL_COL_COORD_LONG;
        int LOCAL_COL_LOCATION_SETTING;

        float RETURNED_LAT;
        float RETURNED_LON;
        String RETURNED_CITY_NAME;
        String RETURNED_LOCATION_SETTING;

        long locationRowId;

        ContentValues testValues;
        ContentValues testValues1;

        dbHelper = new WeatherDbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        testValues = TestUtilities.createNorthPoleLocationValues();

        testValues1 = TestUtilities.createNorthPoleLocationValues1();

        // Insert ContentValues into database and get a row ID back

        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null , testValues);
        long locationRowId1 = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null , testValues1);

        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        // Query the database and receive a Cursor back

        dbCursor = db.query(WeatherContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null);

        // Move the cursor to a valid database row
        dbCursor.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        LOCAL_COL_CITY_NAME = dbCursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        LOCAL_COL_COORD_LAT = dbCursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        LOCAL_COL_COORD_LONG = dbCursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        LOCAL_COL_LOCATION_SETTING = dbCursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);


        RETURNED_LAT = dbCursor.getFloat(LOCAL_COL_COORD_LAT);
        RETURNED_LON = dbCursor.getFloat(LOCAL_COL_COORD_LONG);
        RETURNED_CITY_NAME = dbCursor.getString(LOCAL_COL_CITY_NAME);
        RETURNED_LOCATION_SETTING = dbCursor.getString(LOCAL_COL_LOCATION_SETTING);

        assertTrue("LAT DONT MATCH", RETURNED_LAT == TestUtilities.VALUE_COORD_LAT);
        assertTrue("LON DONT MATCH", RETURNED_LON == TestUtilities.VALUE_COORD_LOONG);
        assertTrue("CITY NAME DONT MATCH", RETURNED_CITY_NAME.equals(TestUtilities.VALUE_CITY_NAME));
        assertTrue("TEST LOCATION DONT MATCH", RETURNED_LOCATION_SETTING.equals(TestUtilities.TEST_LOCATION));

        // Finally, close the cursor and database
        dbCursor.close();
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database
        WeatherDbHelper dbHelper;
        SQLiteDatabase db;
        Cursor dbCursor;

        int LOCAL_COLUMN_LOCATION_TABLE_ID;
        int LOCAL_COLUMN_LOC_KEY;
        int LOCAL_COLUMN_DATE;
        int LOCAL_COLUMN_DEGREES;
        int LOCAL_COLUMN_HUMIDITY;
        int LOCAL_COLUMN_PRESSURE;
        int LOCAL_COLUMN_MAX_TEMP;
        int LOCAL_COLUMN_MIN_TEMP;
        int LOCAL_COLUMN_SHORT_DESC;
        int LOCAL_COLUMN_WIND_SPEED;
        int LOCAL_COLUMN_WEATHER_ID;

        int RETURNED_COLUMN_LOCATION_TABLE_ID;
        int RETURNED_COLUMN_LOC_KEY;
        int RETURNED_COLUMN_DATE;
        float RETURNED_COLUMN_DEGREES;
        float RETURNED_COLUMN_HUMIDITY;
        float RETURNED_COLUMN_PRESSURE;
        float RETURNED_COLUMN_MAX_TEMP;
        float RETURNED_COLUMN_MIN_TEMP;
        String RETURNED_COLUMN_SHORT_DESC;
        float RETURNED_WIND_SPEED;
        int RETURNED_WEATHER_ID;

        long locationRowId;
        long weatherRowId;

        ContentValues testValuesLocation;
        ContentValues testValuesWeather;

        dbHelper = new WeatherDbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        testValuesLocation = TestUtilities.createNorthPoleLocationValues();

        // Insert Location ContentValues into database and get a row ID back
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null , testValuesLocation);
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        dbCursor = db.query(WeatherContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null);
        dbCursor.moveToFirst();
        LOCAL_COLUMN_LOCATION_TABLE_ID = dbCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
        RETURNED_COLUMN_LOCATION_TABLE_ID = dbCursor.getInt(LOCAL_COLUMN_LOCATION_TABLE_ID);

        // make Weather Test Values
        testValuesWeather = TestUtilities.createWeatherValues(locationRowId);

        // pass Test Weather Values to Weather Table
        weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null , testValuesWeather);
        assertTrue("Error: Failure to insert Weather Values", weatherRowId != -1);

        // Querry the weather table
        dbCursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME, null, null, null, null, null, null);

        // Move the cursor to a valid database row
        dbCursor.moveToFirst();

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        LOCAL_COLUMN_LOC_KEY = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_LOC_KEY);
        LOCAL_COLUMN_DATE = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        LOCAL_COLUMN_DEGREES = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES);
        LOCAL_COLUMN_HUMIDITY = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY);
        LOCAL_COLUMN_PRESSURE = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE);
        LOCAL_COLUMN_MAX_TEMP = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        LOCAL_COLUMN_MIN_TEMP = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        LOCAL_COLUMN_SHORT_DESC = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
        LOCAL_COLUMN_WIND_SPEED = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED);
        LOCAL_COLUMN_WEATHER_ID = dbCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID);

        RETURNED_COLUMN_LOC_KEY = dbCursor.getInt(LOCAL_COLUMN_LOC_KEY);
        RETURNED_COLUMN_DATE = dbCursor.getInt(LOCAL_COLUMN_DATE);
        RETURNED_COLUMN_DEGREES = dbCursor.getFloat(LOCAL_COLUMN_DEGREES);
        RETURNED_COLUMN_HUMIDITY = dbCursor.getFloat(LOCAL_COLUMN_HUMIDITY);
        RETURNED_COLUMN_PRESSURE = dbCursor.getFloat(LOCAL_COLUMN_PRESSURE);
        RETURNED_COLUMN_MAX_TEMP = dbCursor.getFloat(LOCAL_COLUMN_MAX_TEMP);
        RETURNED_COLUMN_MIN_TEMP = dbCursor.getFloat(LOCAL_COLUMN_MIN_TEMP);
        RETURNED_COLUMN_SHORT_DESC = dbCursor.getString(LOCAL_COLUMN_SHORT_DESC);
        RETURNED_WIND_SPEED = dbCursor.getFloat(LOCAL_COLUMN_WIND_SPEED);
        RETURNED_WEATHER_ID = dbCursor.getInt(LOCAL_COLUMN_WEATHER_ID);

        assertTrue("DEGREES DONT MATCH", RETURNED_COLUMN_DEGREES == TestUtilities.VALUE_DEGREES);
        assertTrue("HUMIDITY DONT MATCH", RETURNED_COLUMN_HUMIDITY == TestUtilities.VALUE_HUMIDITY);
        assertTrue("PRESSURE DONT MATCH", RETURNED_COLUMN_PRESSURE == TestUtilities.VALUE_PRESSURE);
        assertTrue("DEGREES DONT MATCH", RETURNED_COLUMN_MAX_TEMP == TestUtilities.VALUE_MAX_TEMP);
        assertTrue("PRESSURE DONT MATCH", RETURNED_COLUMN_MIN_TEMP == (TestUtilities.VALUE_MIN_TEMP));
        assertTrue("SHORT DESC DONT MATCH", RETURNED_COLUMN_SHORT_DESC.equals(TestUtilities.VALUE_SHORT_DESC));
        assertTrue("WIND SPEED DONT MATCH", RETURNED_WIND_SPEED == (TestUtilities.VALUE_WIND_SPEED));
        assertTrue("WEATHER ID DONT MATCH", RETURNED_WEATHER_ID == ( TestUtilities.VALUE_WEATHER_ID));
        assertTrue("DATE DONT MATCH", RETURNED_COLUMN_DATE == TestUtilities.TEST_DATE);
        assertTrue("LOC KEY DONT MATCH", RETURNED_COLUMN_LOC_KEY == RETURNED_COLUMN_LOCATION_TABLE_ID);

        // Finally, close the cursor and database
        dbCursor.close();
        dbHelper.close();
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation() {
        return -1L;
    }
}
