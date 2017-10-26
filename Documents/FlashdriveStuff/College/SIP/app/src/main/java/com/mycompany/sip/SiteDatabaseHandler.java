package com.mycompany.sip;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Emily on 10/25/2017.
 * Accessed from www.androidhive.info
 */

public class SiteDatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "sitesManager";

    // Sites table name
    private static final String TABLE_SITES = "sites";

    // Sites Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";

    public SiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SITES_TABLE = "CREATE TABLE " + TABLE_SITES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT" + ")";
        db.execSQL(CREATE_SITES_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SITES);

        // Create tables again
        onCreate(db);
    }

    //Adding new site
    public void addSite(Site site){}

    //Getting single site
    public Site getSite(){}

    //Getting site count
    public int getSitesCount(){}

    //Updating single site
    public int updateSite(Site site){}


    //Deleting single site
    public void deleteSite(Site site){}
}
