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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.*;

/**
 * Created by Erik on 12/7/2017.
 */

public class RemoteDatabaseHandler {
    LocalDatabaseHandler ldb;
    Context con;

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
    public ArrayList LoadAllSites()
    {
        JSONArray sites = null;
        ArrayList<Site> allSites = new ArrayList<>();

        //HashMap to be passed to ListView, contains site's name and primary key
        ArrayList<HashMap<String, String>> sitesList = new ArrayList<>();

        // Building Parameters
        HashMap params = new HashMap();
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

                        //Creating model object named temp
                        Site temp = new Site(name, siteNumber, date, location, description, Integer.parseInt(pk));
                        allSites.add(temp); //Adding temp to list of sites
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

    public boolean CreateNewSite(Site site)
    {
        // Building Parameters
        HashMap params = new HashMap();

        //Getting data from site, which was created by new site dialog
        //TODO: could this data be changed before/during execute, causing problems?
        params.put("PrimaryKey", site.getPk());
        params.put("siteName", site.getName());
        params.put("siteNumber", site.getNumber());
        params.put("location", site.getLocation());
        params.put("description", site.getDescription());
        params.put("dateDiscovered", site.getDateOpened());

        // getting JSON Object
        // Note that create site url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_create_site,
                "POST", params);

        System.out.println(json);

        try {
            // check log cat for response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created site
                    // closing this screen
                    online=true;
                    return true;
                } else {
                    // failed to create site
                    return false;
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
            return true;
        }
        return false;
    }

    public ArrayList loadAllUnits(Site site)
    {
        JSONArray units = null;
        ArrayList<Unit> allUnits = new ArrayList<>();
        // Building Parameters
        HashMap params = new HashMap();

        params.put("foreignKey", site.getPk());

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
                            String name = c.getString(TAG_UNITNAME);
                            String nsDim = c.getString(TAG_NS);
                            String ewDim = c.getString(TAG_EW);
                            String date = c.getString(TAG_DATEOPEN);
                            String excs = c.getString(TAG_EXCS);
                            String reas = c.getString(TAG_REAS);

                            Unit temp = new Unit(name, date, nsDim, ewDim, site, excs, reas, Integer.parseInt(id));
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
                System.out.println("Getting all local units from Site: " + site.getPk());
                allUnits = (ArrayList) ldb.getAllUnitsFromSite(site.getPk());
                System.out.println(allUnits);
            }
            return allUnits;
    }

    public boolean createNewUnit(Unit unit)
    {
        // Building Parameters
        HashMap params = new HashMap();
        params.put("PrimaryKey", unit.getPk());
        params.put("foreignKey", unit.getSite().getPk());
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

                if (success == 1) {
                    // successfully created unit
                    // closing this screen
                    online=true;
                    return true;
                } else {
                    // failed to create unit
                    return false;
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
            return true;
        }
        return false;
    }

    public ArrayList loadAllLevels(Unit unit)
    {
        ArrayList<Level> allLevels = new ArrayList<>();
        JSONArray levels = null;

        HashMap params = new HashMap();

        params.put("foreignKey", unit.getPk());

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
                        int num = c.getInt(TAG_LVLNUM);
                        Double bd = c.getDouble(TAG_BD);
                        Double ed = c.getDouble(TAG_ED);
                        String date = c.getString(TAG_DATE);
                        String excm = c.getString(TAG_EXCM);
                        String imPath = null;
                        try {
                            imPath = c.getString(TAG_IMPATH);
                            System.out.println("received level path: " + imPath);
                        }catch(JSONException e)
                        {
                            System.out.println("No image path sent");
                            e.printStackTrace();
                        }

                        Level temp = new Level(num, bd, ed, unit.getSite(), unit, date, excm, "", Integer.parseInt(id));
                        allLevels.add(temp);
                        temp.setImagePath(imPath);

                        //If user selected an image, save that to server too
                        try {
                            if (imPath != null && !imPath.equals("")) {
                                Uri tempUri = Uri.parse(imPath);
                                if (tempUri != null) //if the level has a Uri saved as its path instead of a url
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
            System.out.println("Coudln't connect to remote server, loading levels from local sever instead");
            allLevels = (ArrayList) ldb.getAllLevelsFromUnit(unit.getPk());
        }
        return allLevels;
    }

    public boolean createNewLevel(Level level)
    {

        // Building Parameters
        HashMap params = new HashMap();

        if(level.getPk()!=-1)//if not a new level, update existing. TODO: edit php to include this
        {
            params.put("PrimaryKey", level.getPk());
        }
        params.put("foreignKey", level.getUnit().getPk());
        params.put("lvlNum", level.getNumber());
        params.put("begDepth", level.getBegDepth());
        params.put("endDepth", level.getEndDepth());
        params.put("dateStarted", level.getDateStarted());
        params.put("excavationMethod", level.getExcavationMethod());
        //TODO: add notes to the database
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

                if (success == 1) {
                    // closing this screen
                    //finish();
                    //startActivity(getIntent());
                    online=true;
                    return true;
                } else {
                    // failed to create level
                    return false;
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
            System.out.println(ldb.getAllLevelsFromUnit(level.getUnit().getPk()));
            return true;
        }
        return false;
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
                String temp = url_upload_image + "?" + "PrimaryKey=" + lvl.getPk() + "";
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

    public ArrayList loadAllArtifacts(Level level)
    {
        ArrayList<Artifact> allArtifacts = new ArrayList<>();
        JSONArray artifacts = null;
        // Building Parameters
        HashMap params = new HashMap();

        params.put("foreignKey", level.getPk());

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
                        String anum = c.getString(TAG_ANUM);
                        int cnum = c.getInt(TAG_CNUM);
                        String cont = c.getString(TAG_CONT);

                        Artifact temp = new Artifact(level.getSite(), level.getUnit(), level, anum, cnum, cont, Integer.parseInt(id));
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
            allArtifacts = (ArrayList) ldb.getAllArtifactsFromLevel(level.getPk());
        }
        return allArtifacts;
    }

    public boolean createNewArtifact(Artifact artifact)
    {
        // Building Parameters
        HashMap params = new HashMap();
        params.put("PrimaryKey", artifact.getPk());
        params.put("foreignKey", artifact.getLevel().getPk());
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

                if (success == 1) {
                    online=true;
                    // closing this screen
                    return true;
                } else {
                    // failed to create artifact
                    return false;
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
            return true;
        }
        return false;

    }

    public boolean isOnline()
    {
        HashMap params = new HashMap();
        // getting JSON string from URL
        JSONObject json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

        try {
            // Check your log cat for JSON reponse
            Log.d("All artifacts: ", json.toString());
            return true;
        }catch(NullPointerException e)
        {
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
}
