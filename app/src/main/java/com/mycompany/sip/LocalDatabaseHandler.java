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

public class LocalDatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version

    private static final String LOG = "DatabaseHelper";

    private static int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mapp";

    // Sites table name
    private static final String TABLE_SITES = "sites";
    private static final String TABLE_UNITS = "units";
    private static final String TABLE_LEVELS = "levels";
    private static final String TABLE_ARTIFACTS = "artifacts";

    // Sites Table Column names
    private static final String KEY_PK = "PrimaryKey";
    private static final String KEY_NAME = "siteName";
    private static final String KEY_NUMBER = "siteNumber";
    private static final String KEY_LOC = "location";
    private static final String KEY_DESC = "description";
    private static final String KEY_DATE = "dateDiscovered";

    //Units Table Column names
    private static final String KEY_FK = "foreignKey";
    private static final String KEY_DATUM = "datum";
    private static final String KEY_NSDIM = "nsDim";
    private static final String KEY_EWDIM = "ewDim";
    private static final String KEY_DATEOPEN = "dateOpened";
    private static final String KEY_EXCS = "excavators";
    private static final String KEY_REAS = "reasonForOpening";

    //Levels Table Column names
    private static final String KEY_LVLNUM = "lvlNum";
    private static final String KEY_BD = "begDepth";
    private static final String KEY_ED = "endDepth";
    private static final String KEY_DATESTARTED = "dateStarted";
    private static final String KEY_EXCMETH = "excavationMethod";
    private static final String KEY_NOTES = "notes";

    //Artifacts Table Column names
    private static final String KEY_ANUM = "accNum";
    private static final String KEY_CNUM = "catNum";
    private static final String KEY_CONTENTS = "contents";

    //Table Create Statements
    String CREATE_SITES_TABLE = "CREATE TABLE " + TABLE_SITES + "("
            + KEY_PK + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, " + KEY_NUMBER + " TEXT, "
            + KEY_LOC + " TEXT, " + KEY_DESC + " TEXT, " + KEY_DATE  + " TEXT)";

    String CREATE_UNITS_TABLE = "CREATE TABLE " + TABLE_UNITS + "("
            + KEY_PK + " INTEGER PRIMARY KEY, " + KEY_FK + " INTEGER, " + KEY_DATUM + " TEXT, "
            + KEY_NSDIM + " REAL, " + KEY_EWDIM + " REAL, " + KEY_DATEOPEN + " TEXT, "
            + KEY_EXCS + " TEXT, " + KEY_REAS + " TEXT)";

    String CREATE_LEVELS_TABLE = "CREATE TABLE " + TABLE_LEVELS + "("
            + KEY_PK + " INTEGER PRIMARY KEY, " + KEY_FK + " INTEGER, " + KEY_LVLNUM + " INTEGER, "
            + KEY_BD + " REAL, " + KEY_ED + " REAL, " + KEY_DATESTARTED + " TEXT, " + KEY_EXCMETH + " TEXT)";

    String CREATE_ARTIFACTS_TABLE = "CREATE TABLE " + TABLE_ARTIFACTS + "("
            + KEY_PK + " INTEGER PRIMARY KEY, " + KEY_PK + " INTEGER, "
            + KEY_ANUM + " TEXT, " + KEY_CNUM + " INTEGER, " + KEY_CONTENTS + " TEXT)";

    public LocalDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SITES_TABLE);
        db.execSQL(CREATE_UNITS_TABLE);
        db.execSQL(CREATE_LEVELS_TABLE);
        db.execSQL(CREATE_ARTIFACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UNITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEVELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTIFACTS);

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

    //TODO: add methods for units, levels, and artifacts
    //Adding new unit
    public void addUnit(Unit unit, int fk){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FK, fk); //Foreign Key
        values.put(KEY_DATUM, unit.getDatum()); // Datum
        values.put(KEY_NSDIM, unit.getNsDimension());
        values.put(KEY_EWDIM, unit.getEwDimension());
        values.put(KEY_DATEOPEN, unit.getDateOpened());
        values.put(KEY_EXCS, unit.getExcavators());
        values.put(KEY_REAS, unit.getReasonForOpening());

        // Inserting Row
        db.insert(TABLE_UNITS, null, values);
        db.close(); // Closing database connection
    }

    //Getting single unit
    public Unit getUnit(int pk){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_UNITS, new String[] {KEY_PK, KEY_FK,
                        KEY_DATUM, KEY_NSDIM, KEY_EWDIM, KEY_DATEOPEN, KEY_EXCS, KEY_REAS}, KEY_PK + "=?",
                new String[] { String.valueOf(pk) }, null, null, null, null);

        Unit unit = null;

        System.out.println(cursor);
        if (cursor.moveToFirst()) { //Changed from if (cursor != null) by Emily Fletcher 10/30/2017
            System.out.println(cursor.getCount());
            //TODO: make sure this is the correct order
            unit = new Unit(cursor.getString(2), cursor.getString(5), cursor.getString(3), cursor.getString(4), getSite(Integer.parseInt(cursor.getString(1))), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(0)));
        }
        // return unit
        cursor.close();
        return unit;
    }

    // Getting All units
    public List<Unit> getAllUnits() {
        List<Unit> unitList = new ArrayList<Unit>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_UNITS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                //TODO: is cursor.getString(8) the right one? Set in Server
                Unit unit = new Unit(cursor.getString(2), cursor.getString(5), cursor.getString(3), cursor.getString(4), getSite(Integer.parseInt(cursor.getString(1))), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(0)));

                // Adding site to list
                unitList.add(unit);
            } while (cursor.moveToNext());
        }

        // return unit list
        cursor.close();
        return unitList;
    }

    //Getting unit count
    public int getUnitsCount(){
        String countQuery = "SELECT  * FROM " + TABLE_UNITS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }

    //Updating single unit
    public int updateUnit(Unit unit, int fk){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FK, fk);
        values.put(KEY_DATUM, unit.getDatum()); // Datum
        values.put(KEY_NSDIM, unit.getNsDimension());
        values.put(KEY_EWDIM, unit.getEwDimension());
        values.put(KEY_DATEOPEN, unit.getDateOpened());
        values.put(KEY_EXCS, unit.getExcavators());
        values.put(KEY_REAS, unit.getReasonForOpening());

        System.out.println(values);

        // updating row
        //TODO: add pk to unit?
        return db.update(TABLE_UNITS, values, KEY_PK + " = ?",
                new String[] { String.valueOf(unit.getPk()) });
    }

    //Adding new level
    public void addLevel(Level level, int fk){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FK, fk); //Foreign Key
        values.put(KEY_LVLNUM, level.getNumber()); // Level Number
        values.put(KEY_BD, level.getBegDepth());
        values.put(KEY_ED, level.getEndDepth());
        values.put(KEY_DATESTARTED, level.getDateStarted());
        values.put(KEY_EXCMETH, level.getExcavationMethod());
        //values.put(KEY_NOTES, level.getNotes());

        // Inserting Row
        db.insert(TABLE_LEVELS, null, values);
        db.close(); // Closing database connection
    }

    //Getting single level
    public Level getLevel(int pk){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_LEVELS, new String[] {KEY_PK, KEY_FK,
                        KEY_LVLNUM, KEY_BD, KEY_ED, KEY_DATESTARTED, KEY_EXCMETH/*, KEY_NOTES*/}, KEY_PK + "=?",
                new String[] { String.valueOf(pk) }, null, null, null, null);

        Level level = null;

        System.out.println(cursor);
        if (cursor.moveToFirst()) { //Changed from if (cursor != null) by Emily Fletcher 10/30/2017
            System.out.println(cursor.getCount());
            //TODO: make this correct
            Unit un = getUnit(Integer.parseInt(cursor.getString(1)));
            level = new Level(Integer.parseInt(cursor.getString(2)), Double.parseDouble(cursor.getString(3)), Double.parseDouble(cursor.getString(4)), un.getSite(), un, cursor.getString(5), cursor.getString(6), ""/*server does not have notes yet, but MO does*/, Integer.parseInt(cursor.getString(0)));
        }
        // return level
        cursor.close();
        return level;
    }

    // Getting All levels
    public List<Level> getAllLevels() {
        List<Level> levelList = new ArrayList<Level>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LEVELS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                //TODO: make this correct

                Unit un = getUnit(Integer.parseInt(cursor.getString(1)));
                Level level = new Level(Integer.parseInt(cursor.getString(2)), Double.parseDouble(cursor.getString(3)), Double.parseDouble(cursor.getString(4)), un.getSite(), un, cursor.getString(5), cursor.getString(6), ""/*server does not have notes yet, but MO does*/, Integer.parseInt(cursor.getString(0)));

                // Adding site to list
                levelList.add(level);
            } while (cursor.moveToNext());
        }

        // return level list
        cursor.close();
        return levelList;
    }

    //Getting level count
    public int getLevelsCount(){
        String countQuery = "SELECT  * FROM " + TABLE_LEVELS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }

    //Updating single level
    public int updateLevel(Level level, int fk){
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println(db.toString());

        ContentValues values = new ContentValues();
        values.put(KEY_FK, fk); //Foreign Key
        values.put(KEY_LVLNUM, level.getNumber()); // Level Number
        values.put(KEY_BD, level.getBegDepth());
        values.put(KEY_ED, level.getEndDepth());
        values.put(KEY_DATESTARTED, level.getDateStarted());
        values.put(KEY_EXCMETH, level.getExcavationMethod());
        //values.put(KEY_NOTES, level.getNotes());

        System.out.println(values);

        // updating row
        //TODO: add pk to level?
        return db.update(TABLE_LEVELS, values, KEY_PK + " = ?",
                new String[] { String.valueOf(level.getPk())});
        /*return db.update(TABLE_LEVELS, values, KEY_PK + " = ?",
                new String[] { String.valueOf(level.getPk()) });*/
    }

    //Adding new artifact
    public void addArtifact(Artifact artifact, int fk){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FK, fk); //Foreign Key
        values.put(KEY_ANUM, artifact.getAccessionNumber()); // Artifact Accession Number
        values.put(KEY_CNUM, artifact.getCatalogNumber());
        values.put(KEY_CONTENTS, artifact.getContents());

        // Inserting Row
        db.insert(TABLE_ARTIFACTS, null, values);
        db.close(); // Closing database connection
    }

    //Getting single artifact
    public Artifact getArtifact(int pk){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_ARTIFACTS, new String[] {KEY_PK, KEY_FK,
                        KEY_ANUM, KEY_CNUM, KEY_CONTENTS}, KEY_PK + "=?",
                new String[] { String.valueOf(pk) }, null, null, null, null);

        Artifact artifact = null;

        System.out.println(cursor);
        if (cursor.moveToFirst()) { //Changed from if (cursor != null) by Emily Fletcher 10/30/2017
            System.out.println(cursor.getCount());
            //TODO: make this correct
            Level l = getLevel(Integer.parseInt(cursor.getString(1)));
            artifact = new Artifact(l.getSite(), l.getUnit(), l, cursor.getString(2), Integer.parseInt(cursor.getString(3)), cursor.getString(4), Integer.parseInt(cursor.getString(0)));
        }
        // return artifact
        cursor.close();
        return artifact;
    }

    // Getting All artifacts
    public List<Artifact> getAllArtifacts() {
        List<Artifact> artifactList = new ArrayList<Artifact>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ARTIFACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                //TODO: make this correct
                Level l = getLevel(Integer.parseInt(cursor.getString(1)));
                Artifact artifact = new Artifact(l.getSite(), l.getUnit(), l, cursor.getString(2), Integer.parseInt(cursor.getString(3)), cursor.getString(4), Integer.parseInt(cursor.getString(0)));

                // Adding artifact to list
                artifactList.add(artifact);
            } while (cursor.moveToNext());
        }

        // return artifact list
        cursor.close();
        return artifactList;
    }

    //Getting artifact count
    public int getArtifactsCount(){
        String countQuery = "SELECT  * FROM " + TABLE_ARTIFACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }

    //Updating single artifact
    public int updateArtifact(Artifact artifact, int fk){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FK, fk); //Foreign Key
        values.put(KEY_ANUM, artifact.getAccessionNumber()); // Artifact Accession Number
        values.put(KEY_CNUM, artifact.getCatalogNumber());
        values.put(KEY_CONTENTS, artifact.getContents());

        // updating row
        //TODO: add pk to artifact?
        return db.update(TABLE_ARTIFACTS, values, KEY_PK + " = ?",
                new String[] { String.valueOf(artifact.getPk()) });
    }
}
