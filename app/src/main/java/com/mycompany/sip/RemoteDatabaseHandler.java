package com.mycompany.sip;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.mycompany.sip.Global.*;

/**
 * Created by Erik on 12/7/2017.
 */

public class RemoteDatabaseHandler {
    LocalDatabaseHandler ldb;
    Context con;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Boolean online = true;

    //Upload stuff
    int serverResponseCode = 0;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    JSONParser jsonParser = new JSONParser();//TODO: is it necessary to have two?

    //TODO: Should somehow tell caller if device is offline
    //TODO: If offline and updating, don't want to add more entries to local db
    public RemoteDatabaseHandler(Context c)
    {
        con = c;
        ldb = new LocalDatabaseHandler(con);
    }
    public ArrayList LoadAllSites(Timestamp offline)
    {
        JSONArray sites = null;
        ArrayList<Site> allSites = new ArrayList<>();


        //HashMap to be passed to ListView, contains site's name and primary key
        ArrayList<HashMap<String, String>> sitesList = new ArrayList<>();

        // Building Parameters
        HashMap params = new HashMap();

        //if a timestamp was passed, get all sites updated after that time
        if(offline!=null)
        {
            params.put("lastOnline", offline);
        }

        // getting JSON string from URL
        JSONObject json = jParser.makeHttpRequest(url_all_sites, "GET", params);

        try {
            // Check your log cat for JSON reponse
            Log.d("All sites: ", json.toString());
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // sites found
                    // Getting Array of sites
                    sites = json.getJSONArray(TAG_SITES);

                    // looping through All sites
                    for (int i = 0; i < sites.length(); i++) {
                        JSONObject c = sites.getJSONObject(i);

                        // Storing each json item in variable
                        String pk = c.getString(TAG_PID);
                        String name = c.getString(TAG_SITENAME);
                        String siteNumber = c.getString(TAG_SITENUM);
                        String location = c.getString(TAG_LOC);
                        String description = c.getString(TAG_DESC);
                        String date = c.getString(TAG_DATEDISC);
                        String dateUpdated = c.getString(TAG_DATEUPDATED);
                        String dateCreated = c.getString(TAG_DATECREATED);

                        //Creating model object named temp
                        Site temp = null;
                        try {
                            temp = new Site(name, siteNumber, date, location, description, -1, Integer.parseInt(pk), new Timestamp(sdf.parse(dateCreated).getTime()), new Timestamp(sdf.parse(dateUpdated).getTime()));
                            allSites.add(temp); //Adding temp to list of sites
                        }catch(ParseException e)
                        {
                            System.out.println(e);
                        }
                    }
                } else {
                    // no sites found
                    // Launch Add New product Dialog
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            online=true;
        }
        catch(NullPointerException e)
        {
            online=false;
            //Server isn't running/reachable, so just get stuff from local one
            System.out.println("Server isn't running, use local sites instead!!!");
            allSites = (ArrayList) ldb.getAllSites();
            System.out.println("Sites RDB recovered: " + allSites);
        }
            return allSites;
    }

    public int CreateNewSite(Site site)
    {
        // Building Parameters
        HashMap params = new HashMap();

        //Getting data from site, which was created by new site dialog
        //TODO: could this data be changed before/during execute, causing problems?
        params.put("siteName", site.getName());
        params.put("siteNumber", site.getNumber());
        params.put("location", site.getLocation());
        params.put("description", site.getDescription());
        params.put("dateDiscovered", site.getDateOpened());

        // getting JSON Object
        // Note that create site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_create_site,
                "POST", params); //TODO: make this variable, based on if PK is set and if sitenum is a duplicate

        System.out.println("Json: " + json);

        try {
            // check log cat for response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                //TODO: get pk and pass it to updatedbs so it can be saved to the ldb

                if (success == 1) {
                    int pk = json.getInt(TAG_PID);
                    // successfully created site
                    // closing this screen
                    online=true;

                    return pk;
                } else {
                    // failed to create site
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //TODO: figure out where this should go
            //Saves site to local SQLite database
            //ldb.addSite(site);

        }catch(NullPointerException e)
        {
            online=false;
            //Server is unreachable, save to local server instead
            System.out.println("Adding site to local server, remote server not accessible.");
            ldb.addSite(site); //TODO: ldb's primary keys must be the same as the remote server's, but this one isn't there and won't be until the user connects
            //TODO: to the internet again. So what should we do? Let it default set for now and update it when we back up to remote server?
            //TODO: Then the ldb.update methods will have to be able to update PKs which I'm not sure is allowed...
            return -1;
        }
        return -2;
    }

    public int updateSite(Site site)
    {
        // Building Parameters
        HashMap params = new HashMap();

        //Getting data from site, which was created by new site dialog
        //TODO: could this data be changed before/during execute, causing problems?
        params.put("PrimaryKey", site.getRemotePK());
        params.put("siteName", site.getName());
        params.put("siteNumber", site.getNumber());
        params.put("location", site.getLocation());
        params.put("description", site.getDescription());
        params.put("dateDiscovered", site.getDateOpened());

        // getting JSON Object
        // Note that update site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_update_site,
                "PUT", params);

        System.out.println("Json: " + json);

        try {
            // check log cat for response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                //TODO: get pk and pass it to updatedbs so it can be saved to the ldb

                if (success == 1) {
                    int pk = json.getInt(TAG_PID);
                    // successfully updated site
                    // closing this screen
                    online=true;

                    return pk;
                } else {
                    // failed to update site
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //TODO: figure out where this should go
            //Saves site to local SQLite database
            //ldb.addSite(site);

        }catch(NullPointerException e)
        {
            online=false;
//            //Server is unreachable, save to local server instead
//            System.out.println("Adding site to local server, remote server not accessible.");
//            ldb.addSite(site);
            return -1;
        }
        return -2;
    }

    public ArrayList loadAllUnits(Site site, Timestamp offline)
    {
        JSONArray units = null;
        ArrayList<Unit> allUnits = new ArrayList<>();
        // Building Parameters
        HashMap params = new HashMap();
        if(site!=null) { //if a site was passed, get all units associated with that site
            params.put("foreignKey", site.getRemotePK());
            System.out.println("getting units from site with rpk: " + site.getRemotePK());
        }
        else
        {
            //if a timestamp was passed, get all units updated after that time
            if(offline!=null)
            {
                params.put("lastOnline", offline);
            }
        }

        // getting JSON string from URL
        JSONObject json = jParser.makeHttpRequest(url_all_units, "GET", params);

        try {
                // Check your log cat for JSON reponse
                Log.d("All units: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // units found
                        // Getting Array of units
                        units = json.getJSONArray(TAG_UNITS);

                        // looping through All units
                        for (int i = 0; i < units.length(); i++) {
                            JSONObject c = units.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString(TAG_PID);
                            String fk = c.getString(TAG_FK);
                            String name = c.getString(TAG_UNITNAME);
                            String nsDim = c.getString(TAG_NS);
                            String ewDim = c.getString(TAG_EW);
                            String date = c.getString(TAG_DATEOPEN);
                            String excs = c.getString(TAG_EXCS);
                            String reas = c.getString(TAG_REAS);
                            String dateCreated = c.getString(TAG_DATECREATED);
                            String dateUpdated = c.getString(TAG_DATEUPDATED);

                            Unit temp = null;
                            if(site!=null) {
                                temp = new Unit(name, date, nsDim, ewDim, site, excs, reas, -1, Integer.parseInt(id), Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateUpdated));
                            }
                            else //if no site was passed, just give ldb the site's rpk
                            {
                                temp = new Unit(name, date, nsDim, ewDim,
                                        new Site("", "", "", "", "", -1, Integer.parseInt(fk), null, null),
                                        excs, reas, -1, Integer.parseInt(id), Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateUpdated));
                            }
                            allUnits.add(temp);

                            //save to local database
                            /*if (ldb.updateUnit(temp) == 0) {
                                System.out.println("Adding new unit " + temp + " to SQLite DB");
                                System.out.println(temp.getPk() + " " + ldb.getUnit(temp.getPk()));
                                ldb.addUnit(temp);
                            } else {
                                System.out.println("Unit " + temp + " already exists and was updated");
                            }
                            System.out.println(ldb.getUnitsCount());
                            System.out.println(ldb.getUnit(temp.getPk()) + " " + temp.getPk());
                            System.out.println(ldb.getAllUnits().toString());*/
                        }
                    } else {
                        //units don't exist
                    }
                    online=true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch(NullPointerException e)
            {
                online=false;
                System.out.println("Getting all local units from Site: " + site.getRemotePK());
                allUnits = (ArrayList) ldb.getAllUnitsFromSite(site.getRemotePK());
                System.out.println("All local units: " + allUnits);
            }
            return allUnits;
    }

    public int createNewUnit(Unit unit)
    {
        // Building Parameters
        HashMap params = new HashMap();
        params.put("foreignKey", unit.getSite().getRemotePK()); //TODO: What if site isn't saved yet?
        params.put("datum", unit.getDatum());
        params.put("nsDim", unit.getNsDimension());
        params.put("ewDim", unit.getEwDimension());
        params.put("excavators", unit.getExcavators());
        params.put("dateOpened", unit.getDateOpened());
        params.put("reasonForOpening", unit.getReasonForOpening());

        // getting JSON Object
        // Note that create site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_create_unit,
                "POST", params);

        // check log cat fro response
        try {
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                int rpk = json.getInt(TAG_PID);

                if (success == 1) {
                    // successfully created unit
                    // closing this screen
                    online=true;
                    return rpk;
                } else {
                    // failed to create unit
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }catch(NullPointerException e)
        {
            online=false;
            ldb.addUnit(unit); //TODO: ldb's primary keys must be the same as the remote server's, but this one isn't there and won't be until the user connects
            //TODO: to the internet again. So what should we do? Let it default set for now and update it when we back up to remote server?
            //TODO: Then the ldb.update methods will have to be able to update PKs which I'm not sure is allowed...
            //TODO: Since both servers will have the same set of primary keys I guess we could just go with it and set the remote server's
            //TODO: when we're updating...But then we have to do more PHP stuff I think
            // closing this screen
            return -1;
        }
        return -2;
    }

    public int updateUnit(Unit unit)
    {
        // Building Parameters
        HashMap params = new HashMap();
        params.put("PrimaryKey", unit.getRemotePK());
        params.put("foreignKey", unit.getSite().getRemotePK()); //TODO: What if site isn't saved yet?
        params.put("datum", unit.getDatum());
        params.put("nsDim", unit.getNsDimension());
        params.put("ewDim", unit.getEwDimension());
        params.put("excavators", unit.getExcavators());
        params.put("dateOpened", unit.getDateOpened());
        params.put("reasonForOpening", unit.getReasonForOpening());

        // getting JSON Object
        // Note that update site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_update_unit,
                "PUT", params);

        // check log cat fro response
        try {
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                int rpk = json.getInt(TAG_PID);

                if (success == 1) {
                    // successfully updated unit
                    // closing this screen
                    online=true;
                    return rpk;
                } else {
                    // failed to updated unit
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }catch(NullPointerException e)
        {
            online=false;
            //ldb.addUnit(unit);
            // closing this screen
            return -1;
        }
        return -2;
    }

    public ArrayList loadAllLevels(Unit unit, Timestamp offline)
    {
        ArrayList<Level> allLevels = new ArrayList<>();
        JSONArray levels = null;

        HashMap params = new HashMap();

        if(unit!=null) { //if a unit was passed, get all levels associated with that unit
            params.put("foreignKey", unit.getRemotePK());
        }
        else
        {
            //if a timestamp was passed, get all levels updated after that time
            if(offline!=null)
            {
                params.put("lastOnline", offline);
            }
        }

        // getting JSON string from URL
        JSONObject json = jParser.makeHttpRequest(url_all_levels, "GET", params);

        try {
            // Check your log cat for JSON reponse
            Log.d("All levels: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // levels found
                    // Getting Array of levels
                    levels = json.getJSONArray(TAG_LEVELS);

                    // looping through All sites
                    for (int i = 0; i < levels.length(); i++) {
                        JSONObject c = levels.getJSONObject(i);

                        //TODO: entire thing breaks if any input is null--figure out which input can be null, and make sure the user knows which ones can't

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String fk = c.getString(TAG_FK);
                        int num = c.getInt(TAG_LVLNUM);
                        Double bd = c.getDouble(TAG_BD);
                        Double ed = c.getDouble(TAG_ED);
                        String date = c.getString(TAG_DATE);
                        String excm = c.getString(TAG_EXCM);
                        String n = c.getString(TAG_NOTES);
                        String imPath = null;
                        String dateCreated = c.getString(TAG_DATECREATED);
                        String dateUpdated = c.getString(TAG_DATEUPDATED);
                        try {
                            imPath = c.getString(TAG_IMPATH);
                            System.out.println("received level path: " + imPath);
                        }catch(JSONException e)
                        {
                            System.out.println("No image path sent");
                            e.printStackTrace();
                        }

                        Level temp = null;
                        if(unit!=null)
                        {
                            temp = new Level(num, bd, ed, unit.getSite(), unit, date, excm, n, -1, Integer.parseInt(id), Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateUpdated));
                        }
                        else
                        {
                            temp = new Level(num, bd, ed, null,
                                    new Unit("", "", "", "", null, "", "", -1, Integer.parseInt(fk), null, null),
                                    date, excm, n, -1, Integer.parseInt(id), Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateUpdated));
                        }
                        allLevels.add(temp);
                        temp.setImagePath(imPath);

                        //If user selected an image, save that to server too
                        try {
                            if (imPath != null && !imPath.equals("")) {
                                Uri tempUri = Uri.parse(imPath);
                                URL tempURL = null;
                                Boolean errorFlag = false;
                                try {
                                    tempURL = new URL(tempUri.toString());
                                }
                                catch(IllegalArgumentException | MalformedURLException e)//if the level has a Uri saved as its path instead of a url
                                {
                                    System.out.println("Calling uploadImage");
                                    errorFlag=true;
                                    uploadImage(tempUri, temp);
                                }

                                if (!errorFlag && tempURL == null) //if the level has a Uri saved as its path instead of a url
                                {
                                    System.out.println("Calling uploadImage");
                                    uploadImage(tempUri, temp);
                                }
                            }
                        } catch(NullPointerException e)
                        {
                            //for some reason I think this was throwing a null pointer
                            System.out.println("Attempting to upload an image threw a nullpointerexception");
                        }

                        String name = temp.toString();
                    }
                } else {
                    // no levels found
                    // Launch Add New level Activity
                    //TODO: Change it so it isn't getApplicationContext
                        /*Intent i = new Intent(getApplicationContext(),
                                MapHome.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);*/
                }
                online=true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e)
        {
            online=false;
            System.out.println("Couldn't connect to remote server, loading levels from local sever instead");
            allLevels = (ArrayList) ldb.getAllLevelsFromUnit(unit.getRemotePK());
        }
        return allLevels;
    }

    public int createNewLevel(Level level)
    {

        // Building Parameters
        HashMap params = new HashMap();

        params.put("foreignKey", level.getUnit().getRemotePK());  //TODO: what if unit isn't saved yet?
        params.put("lvlNum", level.getNumber());
        params.put("begDepth", level.getBegDepth());
        params.put("endDepth", level.getEndDepth());
        params.put("dateStarted", level.getDateStarted());
        params.put("excavationMethod", level.getExcavationMethod());
        params.put("notes", level.getNotes());
        params.put("imagePath", level.getImagePath());
        System.out.println("level path: " + level.getImagePath());

        // getting JSON Object
        // Note that create site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_create_level,
                "POST", params);

        try {
            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                int rpk = json.getInt(TAG_PID);

                if (success == 1) {
                    // closing this screen
                    //finish();
                    //startActivity(getIntent());
                    online=true;
                    return rpk;
                } else {
                    // failed to create level
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e)
        {
            online=false;
            System.out.println("Adding level " + level + " to SQLite database");
            ldb.addLevel(level);//TODO: ldb's primary keys must be the same as the remote server's, but this one isn't there and won't be until the user connects
            //TODO: to the internet again. So what should we do? Let it default set for now and update it when we back up to remote server?
            //TODO: Then the ldb.update methods will have to be able to update PKs which I'm not sure is allowed...
            //TODO: Since both servers will have the same set of primary keys I guess we could just go with it and set the remote server's
            //TODO: when we're updating...But then we have to do more PHP stuff I think
            System.out.println("Some bs: " + ldb.getAllLevelsFromUnit(level.getUnit().getRemotePK()));
            return -1;
        }
        return -1;
    }

    public int updateLevel(Level level)
    {

        // Building Parameters
        HashMap params = new HashMap();

        if(level.getRemotePK()!=-1)//if not a new level in remote, update existing.
        {
            params.put("PrimaryKey", level.getRemotePK());
        }
        params.put("foreignKey", level.getUnit().getRemotePK());  //TODO: what if unit isn't saved yet?
        params.put("lvlNum", level.getNumber());
        params.put("begDepth", level.getBegDepth());
        params.put("endDepth", level.getEndDepth());
        params.put("dateStarted", level.getDateStarted());
        params.put("excavationMethod", level.getExcavationMethod());
        params.put("notes", level.getNotes());
        params.put("imagePath", level.getImagePath());
        System.out.println("level path: " + level.getImagePath());

        // getting JSON Object
        // Note that update site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_update_level,
                "PUT", params);

        try {
            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                int rpk = json.getInt(TAG_PID);

                if (success == 1) {
                    // closing this screen
                    //finish();
                    //startActivity(getIntent());
                    online=true;
                    return rpk;
                } else {
                    // failed to update level
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e)
        {
            online=false;
            System.out.println("Adding level " + level + " to SQLite database");
            //System.out.println(ldb.getAllLevelsFromUnit(level.getUnit().getRemotePK()));
            return -1;
        }
        return -1;
    }

    public int uploadImage(Uri currPath, Level lvl)
    {
        JSONObject jObj=null;
        StringBuilder result=null;

        String filePath = currPath.getPath();
        String fileName = getFileName(currPath);
        String[] tmp = fileName.split(".");
        String bareFileName = "";
        try {
            bareFileName = tmp[0];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Weird file name, no period found");
            bareFileName = fileName;
        }
        System.out.println("currPath " + currPath);
        System.out.println("filePath " + filePath);

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = null;
        try {
            sourceFile = new File(filePath);
            System.out.println("Got file from path");
        }catch(Exception e)
        {
            System.out.println("URI error: " + e);
        }

        if (sourceFile == null /*|| !sourceFile.isFile()*/) {

            Log.e("uploadFile", "Source File not exist :"
                    +filePath);

            String msg = ("Source File not exist path: "
                    +filePath);
            System.out.println(msg);
            System.out.println("File at path: " + sourceFile + " " + sourceFile.isFile());

            return 0;

        }
        else if(sourceFile != null)
        {
            try {

                // open a URL connection to the Servlet
                InputStream fileInputStream = con.getContentResolver().openInputStream(currPath);
                System.out.println("inputstream: " + fileInputStream.available());
                //FileInputStream fileInputStream = new FileInputStream(sourceFile);

                //adding PrimaryKey to url
                String temp = url_upload_image + "?" + "PrimaryKey=" + lvl.getRemotePK() + "";
                URL url = new URL(temp);
                System.out.println("file upload url: " + url);


                System.out.println("sending file path to server");
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                //conn.setRequestProperty("uploaded_file", filePath);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                System.out.println("image name???: " + getFileName(currPath));
                dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\""
                                + fileName + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    System.out.println("inputstream: " + fileInputStream.available());
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                fileInputStream.close();

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);



                //close the streams //
                dos.flush();
                dos.close();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                            +" http://www.androidexample.com/media/uploads/";
                    System.out.println(msg);
                }
                System.out.println("sent file from path");

                try {
                    //Receive the response from the server
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    Log.d("JSON Parser", "result: " + result.toString());
                    System.out.println("Path result 1: " + result.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(result==null)
                {
                    System.out.println("Failed to connect to server");
                }
                else {
                    jObj = new JSONObject(result.toString());
                    System.out.println("Path result: " + jObj.toString());
                }
                conn.disconnect();
            } catch (MalformedURLException ex) {

                ex.printStackTrace();
                String msg = ("MalformedURLException Exception : check script url.");
                System.out.println(msg + " path");

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();
                String msg = ("Got Exception : see logcat path");
                Log.e("Upload file to server", "Exception : "
                        + e.getMessage(), e);
            }
            return serverResponseCode;

        } // End else block
        return 0;
    }

    public ArrayList loadAllArtifacts(Level level, Timestamp offline)
    {
        ArrayList<Artifact> allArtifacts = new ArrayList<>();
        JSONArray artifacts = null;
        // Building Parameters
        HashMap params = new HashMap();

        if(level!=null) { //if a level was passed, get all artifacts associated with that level
        params.put("foreignKey", level.getRemotePK());
        }
        else
        {
            //if a timestamp was passed, get all artifacts updated after that time
            if(offline!=null)
            {
                params.put("lastOnline", offline);
            }
        }

        // getting JSON string from URL
        JSONObject json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

        try {
            // Check your log cat for JSON reponse
            Log.d("All artifacts: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // artifacts found
                    // Getting Array of artifacts
                    artifacts = json.getJSONArray(TAG_ARTIFACTS);

                    // looping through All sites
                    for (int i = 0; i < artifacts.length(); i++) {
                        JSONObject c = artifacts.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String fk = c.getString(TAG_FK);
                        String anum = c.getString(TAG_ANUM);
                        int cnum = c.getInt(TAG_CNUM);
                        String cont = c.getString(TAG_CONT);
                        String dateCreated = c.getString(TAG_DATECREATED);
                        String dateUpdated = c.getString(TAG_DATEUPDATED);

                        Artifact temp = null;
                        if(level!=null) {
                            temp = new Artifact(level.getSite(), level.getUnit(), level, anum, cnum, cont, -1, Integer.parseInt(id), Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateUpdated));

                        }else
                        {
                            temp = new Artifact(null, null,
                                    new Level(-1, -1.0, -1.0, null, null, "", "", "", -1, Integer.parseInt(fk), null, null),
                                    anum, cnum, cont, -1, Integer.parseInt(id), Timestamp.valueOf(dateCreated), Timestamp.valueOf(dateUpdated));

                        }
                        allArtifacts.add(temp);
                    }
                } else {
                    // no artifacts found
                }
                online=true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e)
        {
            online=false;
            allArtifacts = (ArrayList) ldb.getAllArtifactsFromLevel(level.getRemotePK());
        }
        return allArtifacts;
    }

    public int createNewArtifact(Artifact artifact)
    {
        // Building Parameters
        HashMap params = new HashMap();

        params.put("foreignKey", artifact.getLevel().getRemotePK()); //TODO: What if level isn't saved yet?
        params.put("accNum", artifact.getAccessionNumber());
        params.put("catNum", artifact.getCatalogNumber());
        params.put("contents", artifact.getContents());

        // getting JSON Object
        // Note that create site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_create_artifact,
                "POST", params);

        try {
            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                int rpk = json.getInt(TAG_PID);

                if (success == 1) {
                    online=true;
                    // closing this screen
                    return rpk;
                } else {
                    // failed to create artifact
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e)
        {
            online=false;
            ldb.addArtifact(artifact);//TODO: ldb's primary keys must be the same as the remote server's, but this one isn't there and won't be until the user connects
            //TODO: to the internet again. So what should we do? Let it default set for now and update it when we back up to remote server?
            //TODO: Then the ldb.update methods will have to be able to update PKs which I'm not sure is allowed...
            //TODO: Since both servers will have the same set of primary keys I guess we could just go with it and set the remote server's
            //TODO: when we're updating...But then we have to do more PHP stuff I think
            // closing this screen
            return -1;
        }
        return -2;

    }

    public int updateArtifact(Artifact artifact)
    {
        // Building Parameters
        HashMap params = new HashMap();
        params.put("PrimaryKey", artifact.getRemotePK());
        params.put("foreignKey", artifact.getLevel().getRemotePK()); //TODO: What if level isn't saved yet?
        params.put("accNum", artifact.getAccessionNumber());
        params.put("catNum", artifact.getCatalogNumber());
        params.put("contents", artifact.getContents());

        // getting JSON Object
        // Note that update site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_update_artifact,
                "PUT", params);

        try {
            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);
                int rpk = json.getInt(TAG_PID);

                if (success == 1) {
                    online=true;
                    // closing this screen
                    return rpk;
                } else {
                    // failed to update artifact
                    return -2;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(NullPointerException e)
        {
            online=false;
            //ldb.addArtifact(artifact);
            // closing this screen
            return -1;
        }
        return -2;

    }

    public boolean isOnline()
    {
        HashMap params = new HashMap();
        // getting JSON string from URL
        //TODO: pick something which will be faster
        JSONObject json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

        try {
            // Check your log cat for JSON reponse
            Log.d("All artifacts: ", json.toString());
            //if(!onlineSince.after(new Timestamp(0))) {   //if onlineSince is 00-00-0000 00:00:00 then server wasn't previously online
             //   Global.setOnlineSince(new Timestamp(System.currentTimeMillis()));    //set new timestamp for time online TODO: this isn't perfect
                //new UpdateDBs.execute();                                                                           //Update first, then set OfflineSince to 00-00-0000 00:00:00
               // Global.setOfflineSince(new Timestamp(0));
            //}
            return true;
        }catch(NullPointerException e)
        {
            if(offlineSince!=null && offlineSince.after(new Timestamp(0))) {    //if offlineSince is 00-00-0000 00:00:00 then server wasn't previously offline
                Global.setOnlineSince(new Timestamp(0));   //set onlineSince to
                Global.setOfflineSince(new Timestamp(System.currentTimeMillis()));
            }
            return false;
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = con.getApplicationContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //Get image from server
    public void getImage(URL url, Level lvl)
    {
        try
        {
            InputStream is = (InputStream) url.getContent();

            //TODO: find a way to create unique names
            String filename = lvl.getSite().getNumber() + lvl.getUnit().getDatum() + lvl.getNumber();

            FileOutputStream outputStream = con.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(is.read()); //TODO: use buffers
            outputStream.close();
        } catch(IOException e)
        {
            System.out.println("Could not get file from url");
        }
    }
}
