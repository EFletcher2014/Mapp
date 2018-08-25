package com.mycompany.sip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

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
    private static final String KEY_PK = "PrimaryKey";
    private static final String KEY_NAME = "siteName";
    private static final String KEY_NUMBER = "siteNumber";
    private static final String KEY_LOC = "location";
    private static final String KEY_DESC = "description";
    private static final String KEY_DATE = "dateDiscovered";

    public SiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SITES_TABLE = "CREATE TABLE " + TABLE_SITES + "("
                + KEY_PK + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, " + KEY_NUMBER + " TEXT, "
                + KEY_LOC + " TEXT, " + KEY_DESC + " TEXT, " + KEY_DATE  + " TEXT)";
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
    public void addSite(Site site){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, site.getName()); // Site name
        values.put(KEY_NUMBER, site.getNumber()); // Site Number
        values.put(KEY_LOC, site.getLocation());
        values.put(KEY_DESC, site.getDescription());
        values.put(KEY_DATE, site.getDateOpened());

        // Inserting Row
        System.out.println("HERE!!!!!!!!!!!" + db.insert(TABLE_SITES, null, values));
        db.close(); // Closing database connection
    }

    //Getting single site
    public Site getSite(int pk){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_SITES, new String[] {KEY_PK,
                        KEY_NAME, KEY_NUMBER, KEY_LOC, KEY_DESC, KEY_DATE}, KEY_PK + "=?",
                new String[] { String.valueOf(pk) }, null, null, null, null);

        Site site = null;

        System.out.println(cursor);
        if (cursor.moveToFirst()) { //Changed from if (cursor != null) by Emily Fletcher 10/30/2017
            System.out.println(cursor.getCount());
            //TODO: make sure this is the correct order
            site = new Site(cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(0)));
        }
            // return site
        cursor.close();
        return site;
    }

    // Getting All sites
    public List<Site> getAllSites() {
        List<Site> siteList = new ArrayList<Site>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SITES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Site site = new Site(cursor.getString(1), cursor.getString(2), cursor.getString(5), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(0)));

                // Adding site to list
                siteList.add(site);
            } while (cursor.moveToNext());
        }

        // return site list
        cursor.close();
        return siteList;
    }

    //Getting site count
    public int getSitesCount(){
        String countQuery = "SELECT  * FROM " + TABLE_SITES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }

    //Updating single site
    public int updateSite(Site site){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, site.getName());
        values.put(KEY_NUMBER, site.getNumber());
        values.put(KEY_NUMBER, site.getNumber());
        values.put(KEY_LOC, site.getLocation());
        values.put(KEY_DESC, site.getDescription());
        values.put(KEY_DATE, site.getDateOpened());

        // updating row
        //TODO: add pk to site?
        return db.update(TABLE_SITES, values, KEY_PK + " = ?",
                new String[] { String.valueOf(site.getPk()) });
    }


    //Deleting single site
    public void deleteSite(Site site){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SITES, KEY_PK + " = ?",
                new String[] { String.valueOf(site.getPk()) });
        db.close();
    }
}
