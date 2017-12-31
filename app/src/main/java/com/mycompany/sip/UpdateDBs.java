package com.mycompany.sip;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mycompany.sip.Site;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.*;

//Async Class to Update the local and remote servers
class UpdateDBs extends AsyncTask<String, String, String>
{

    //TODO: figure out what to do if offline--maybe could put it after call to database is executed--also won't take as long
    LocalDatabaseHandler ldb;
    RemoteDatabaseHandler rdb;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    JSONParser jsonParser = new JSONParser();

    public UpdateDBs (Context c)
    {
        //Handler for remote database
        rdb = new RemoteDatabaseHandler(c);
        //Handler for local SQLite database to backup data to device
        ldb = new LocalDatabaseHandler(c);
    }

    protected String doInBackground(String ... args)
    {
        if(rdb.isOnline()) {
            // Check your log cat for JSON reponse
            try {


                //Get all entries in remote server
                ArrayList<Site> allRemSites = rdb.LoadAllSites();

                ArrayList<Unit> allRemUnits = new ArrayList<>();

                ArrayList<Level> allRemLevels = new ArrayList<>();

                ArrayList<Artifact> allRemArtifacts = new ArrayList<>();

                for (int j = 0; j < allRemSites.size(); j++) {
                    allRemUnits.addAll(rdb.loadAllUnits(allRemSites.get(j)));
                }
                //Get all levels associated with these units

                for (int j = 0; j < allRemUnits.size(); j++) {
                    allRemLevels.addAll(rdb.loadAllLevels(allRemUnits.get(j)));
                }

                //Get all artifacts associated with these levels
                JSONArray artifacts = null;

                for (int j = 0; j < allRemLevels.size(); j++) {
                    allRemArtifacts.addAll(rdb.loadAllArtifacts(allRemLevels.get(j)));
                }
                //ArrayList<Site> allRemSites = rdb.LoadAllSites();
                //
                //HashMap params = new HashMap();
                //
                //            //Get all sites from remote server
                //JSONObject json = jParser.makeHttpRequest(url_all_sites, "GET", params);
                //            JSONArray sites = null;
                //
                //            ArrayList<Site> allRemSites = new ArrayList<Site>();
                //
                //            Log.d("All sites: ", json.toString());
                //            try {
                //                // Checking for SUCCESS TAG
                //                int success = json.getInt(TAG_SUCCESS);
                //
                //                if (success == 1) {
                //                    // sites found
                //                    // Getting Array of sites
                //                    sites = json.getJSONArray(TAG_SITES);
                //
                //                    // looping through All sites
                //                    for (int i = 0; i < sites.length(); i++) {
                //                        JSONObject c = sites.getJSONObject(i);
                //
                //                        // Storing each json item in variable
                //                        String pk = c.getString(TAG_PID);
                //                        String name = c.getString(TAG_SITENAME);
                //                        String siteNumber = c.getString(TAG_SITENUM);
                //                        String location = c.getString(TAG_LOC);
                //                        String description = c.getString(TAG_DESC);
                //                        String date = c.getString(TAG_DATEDISC);
                //
                //                        //Creating model object named temp
                //                        Site temp = new Site(name, siteNumber, date, location, description, Integer.parseInt(pk));
                //                        allRemSites.add(temp); //Adding temp to list of sites
                //
                //                        //Backup local changes to remote server
                //                        //TODO: should be done async, to ensure all changes are saved, not just the ones being accessed
                //
                //                                /*//save to local database
                //                                if(ldb.getSite(temp.getPk())==null)//If the site doesn't exist already
                //                                {
                //                                    System.out.println("Adding new site " + temp + " to SQLite DB");
                //                                    System.out.println(temp.getPk() + " " + ldb.getSite(temp.getPk()));
                //                                    ldb.addSite(temp);
                //                                }
                //                                //TODO: Add any SQLite entries to the remote server, and update any remote entries
                //                                System.out.println(ldb.getSitesCount());
                //                                System.out.println(ldb.getSite(temp.getPk()));
                //                                System.out.println(ldb.getAllSites().toString());*/
                //                    }
                //                } else {
                //                    // no sites found
                //                    // Launch Add New product Dialog
                //                }
                //            }catch(JSONException e)
                //            {
                //
                //            }
                //Get entries from local server and loop through to add them to the remote
                ArrayList<Site> allLocSites = (ArrayList) ldb.getAllSites();
                System.out.println("Local sites: " + allLocSites);

                for (int i = 0; i < allLocSites.size(); i++) {
                    Site temp = allLocSites.get(i);
                    System.out.println("Analyzing site: " + temp);
                    if (!allRemSites.contains(temp)) {
                        Site temp1 = new Site(temp.getName(), temp.getNumber(), temp.getDateOpened(), temp.getLocation(), temp.getDescription(), -1);
                        rdb.CreateNewSite(temp1);
                        System.out.println("Added new site: " + temp1);
                    } else {
                        rdb.CreateNewSite(temp);
                        System.out.println("Updated site: " + temp.getPk());
                    }
                    //                if(!allRemSites.contains(temp))//if this site isn't on the remote server, add it //TODO: remove, should know to add or update site
                    //                {
                    //                    // Building Parameters
                    //                    params = new HashMap();
                    //
                    //                    //Adding data from local server to remote server
                    //                    //TODO: could this data be changed before/during execute, causing problems?
                    //                    params.put("PrimaryKey", temp.getPk());
                    //                    params.put("siteName", temp.getName());
                    //                    params.put("siteNumber", temp.getNumber());
                    //                    params.put("location", temp.getLocation());
                    //                    params.put("description", temp.getDescription());
                    //                    params.put("dateDiscovered", temp.getDateOpened());
                    //
                    //                    // getting JSON Object
                    //                    // Note that create site url accepts POST method
                    //                    json = jsonParser.makeHttpRequest(url_create_site,
                    //                            "POST", params);
                    //
                    //                    System.out.println(json);
                    //
                    //                    // check log cat for response
                    //                    Log.d("Create Response", json.toString());
                    //
                    //                    // check for success tag
                    //                    try {
                    //                        int success = json.getInt(TAG_SUCCESS);
                    //
                    //                        if (success == 1) {
                    //                            // successfully created site
                    //                            // TODO: return something
                    //                        }
                    //                    }catch(JSONException e)
                    //                    {
                    //                    }
                    //                }
                }

                //Get all units associated with these sites
                /*JSONArray units = null;

                ArrayList<Unit> allRemUnits = new ArrayList<Unit>();

                for (int j=0; j<allLocSites.size(); j++) {

                    rdb.loadAllUnits(allLocSites.get(j));
                    params = new HashMap();

                    params.put("ForeignKey", allRemSites.get(j).getPk());

                    // getting JSON string from URL
                    json = jParser.makeHttpRequest(url_all_units, "GET", params); //TODO: can I overwrite this like this?

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

                                Unit temp = new Unit(name, date, nsDim, ewDim, allRemSites.get(j), excs, reas, Integer.parseInt(id));
                                allRemUnits.add(temp);
                            }
                        } else {
                            //units don't exist
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                //Update Remote server
                ArrayList<Unit> allLocUnits = (ArrayList) ldb.getAllUnits();

                for (int i = 0; i < allLocUnits.size(); i++) {
                    Unit temp = allLocUnits.get(i);
                    if (!allRemUnits.contains(temp)) {
                        Unit temp1 = new Unit(temp.getDatum(), temp.getDateOpened(), temp.getNsDimension(), temp.getEwDimension(), temp.getSite(), temp.getExcavators(), temp.getReasonForOpening(), -1);
                        rdb.createNewUnit(temp1);
                    } else {
                        rdb.createNewUnit(temp);
                    }
                   /* params = new HashMap();

                    params.put("foreignKey", allLocUnits.get(i).getSite().getPk());
                    params.put("datum", allLocUnits.get(i).getDatum());
                    params.put("nsDim", allLocUnits.get(i).getNsDimension());
                    params.put("ewDim", allLocUnits.get(i).getEwDimension());
                    params.put("excavators", allLocUnits.get(i).getExcavators());
                    params.put("dateOpened", allLocUnits.get(i).getDateOpened());
                    params.put("reasonForOpening", allLocUnits.get(i).getReasonForOpening());

                    // getting JSON Object
                    // Note that create site url accepts POST method
                    json = jsonParser.makeHttpRequest(url_create_unit,
                            "POST", params);

                    // check log cat fro response
                    Log.d("Create Response", json.toString());

                    // check for success tag
                    try {
                        int success = json.getInt(TAG_SUCCESS);

                        if (success == 1) {
                            // successfully created unit
                            // closing this screen

                            //TODO: return something useful

                        } else {
                            // failed to create unit
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                }

                //Get all levels associated with these units
                /*JSONArray levels = null;
                ArrayList<Level> allRemLevels = new ArrayList<>();

                for(int j=0; j<allLocUnits.size(); j++)
                {
                    allRemLevels = rdb.loadAllLevels(allRemUnits.get(j));
                    params = new HashMap();
                    params.put("foreignKey", allRemUnits.get(j).getPk());

                    // getting JSON string from URL
                    json = jParser.makeHttpRequest(url_all_levels, "GET", params);

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

                                // Storing each json item in variable
                                String id = c.getString(TAG_PID);
                                int num = c.getInt(TAG_LVLNUM);
                                Double bd = c.getDouble(TAG_BD);
                                Double ed = c.getDouble(TAG_ED);
                                String date = c.getString(TAG_DATE);
                                String excm = c.getString(TAG_EXCM);

                                Level temp = new Level(num, bd, ed, allRemUnits.get(j).getSite(), allRemUnits.get(j), date, excm, "", Integer.parseInt(id));
                                allRemLevels.add(temp);
                            }
                        } else {
                            // no levels found
                            // Launch Add New level Activity
                            //TODO: Change it so it isn't getApplicationContext
                            *//*Intent i = new Intent(getApplicationContext(),
                                    MapHome.class);
                            // Closing all previous activities
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);*//*
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                //Update Remote Server
                ArrayList<Level> allLocLevels = (ArrayList) ldb.getAllLevels();

                for (int i = 0; i < allLocLevels.size(); i++) {
                    Level temp = allLocLevels.get(i);
                    if (!allRemLevels.contains(temp)) {
                        Level temp1 = new Level(temp.getNumber(), temp.getBegDepth(), temp.getEndDepth(), temp.getSite(), temp.getUnit(), temp.getDateStarted(), temp.getExcavationMethod(), temp.getNotes(), -1);
                        rdb.createNewLevel(temp1);
                    } else {
                        rdb.createNewLevel(temp);
                    }
                    System.out.println("Updated level: " + allLocLevels.get(i).getPk());
                    /*params = new HashMap();

                    if(allLocLevels.get(i).getPk()!=-1)//if not a new level, update existing. TODO: edit php to include this
                    {
                        params.put("PrimaryKey", allLocLevels.get(i).getPk());
                    }
                    params.put("foreignKey", allLocLevels.get(i).getUnit().getPk());
                    params.put("lvlNum", allLocLevels.get(i).getNumber());
                    params.put("begDepth", allLocLevels.get(i).getBegDepth());
                    params.put("endDepth", allLocLevels.get(i).getEndDepth());
                    params.put("dateStarted", allLocLevels.get(i).getDateStarted());
                    params.put("excavationMethod", allLocLevels.get(i).getExcavationMethod());
                    //TODO: add notes to the database
                    //TODO: add picture to the database

                    // getting JSON Object
                    // Note that create site url accepts POST method
                    json = jsonParser.makeHttpRequest(url_create_level,
                            "POST", params);

                    // check log cat fro response
                    Log.d("Create Response", json.toString());

                    // check for success tag
                    try {
                        int success = json.getInt(TAG_SUCCESS);

                        if (success == 1) {

                            // closing this screen
                            //finish();
                            //startActivity(getIntent());
                            //TODO: return something useful
                        } else {
                            // failed to create level
                            //TODO: return something useful
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                }

                //Get all artifacts associated with these levels
                /*ArrayList<Artifact> allRemArtifacts = new ArrayList<>();
                JSONArray artifacts = null;

                for(int j=0; j<allRemLevels.size(); j++)
                {
                    allRemArtifacts = rdb.loadAllArtifacts(allRemLevels.get(j));
                    params = new HashMap();
                    params.put("foreignKey", allRemLevels.get(j).getPk());

                    // getting JSON string from URL
                    json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

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
                                System.out.println(anum);
                                int cnum = c.getInt(TAG_CNUM);
                                String cont = c.getString(TAG_CONT);

                                Artifact temp = new Artifact(allRemLevels.get(j).getSite(), allRemLevels.get(j).getUnit(), allRemLevels.get(j), anum, cnum, cont, Integer.parseInt(id));
                                String name = temp.toString();
                                System.out.println(temp.toString());
                                allRemArtifacts.add(temp);
                            }
                        } else {
                            // no artifacts found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                //Update remote server
                ArrayList<Artifact> allLocArtifacts = (ArrayList) ldb.getAllArtifacts();

                for (int i = 0; i < allLocArtifacts.size(); i++) {
                    Artifact temp = allLocArtifacts.get(i);

                    if (!allRemArtifacts.contains(temp)) {
                        Artifact temp1 = new Artifact(temp.getSite(), temp.getUnit(), temp.getLevel(), temp.getAccessionNumber(), temp.getCatalogNumber(), temp.getContents(), -1);
                        rdb.createNewArtifact(temp1);
                    } else {
                        rdb.createNewArtifact(temp);
                    }
                    System.out.println("Updated artifact: " + allLocArtifacts.get(i).getPk());
                    /*params = new HashMap();


                    params.put("foreignKey", allLocArtifacts.get(i).getLevel().getPk());
                    params.put("accNum", allLocArtifacts.get(i).getAccessionNumber());
                    params.put("catNum", allLocArtifacts.get(i).getCatalogNumber());
                    params.put("contents", allLocArtifacts.get(i).getContents());

                    // getting JSON Object
                    // Note that create site url accepts POST method
                    json = jsonParser.makeHttpRequest(url_create_artifact,
                            "POST", params);

                    // check log cat fro response
                    Log.d("Create Response", json.toString());

                    // check for success tag
                    try {
                        int success = json.getInt(TAG_SUCCESS);

                        if (success == 1) {

                            // closing this screen
                            //TODO: return something useful
                        } else {
                            // failed to create artifact
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                }

                //TODO: have to reload everything--UGH can I just create a class to do all of this so I only have to call a method?
                //ldb.update(all);

                //TODO: return something useful
            } catch (NullPointerException e) {
                //TODO: return something else useful
            }

            //Get all entries in remote server
            ArrayList<Site> allRemSites = rdb.LoadAllSites();

            ArrayList<Unit> allRemUnits = new ArrayList<>();

            ArrayList<Level> allRemLevels = new ArrayList<>();

            ArrayList<Artifact> allRemArtifacts = new ArrayList<>();

            for (int j = 0; j < allRemSites.size(); j++) {
                allRemUnits.addAll(rdb.loadAllUnits(allRemSites.get(j)));
            }
            //Get all levels associated with these units

            for (int j = 0; j < allRemUnits.size(); j++) {
                allRemLevels.addAll(rdb.loadAllLevels(allRemUnits.get(j)));
            }

            //Get all artifacts associated with these levels
            JSONArray artifacts = null;

            for (int j = 0; j < allRemLevels.size(); j++) {
                allRemArtifacts.addAll(rdb.loadAllArtifacts(allRemLevels.get(j)));
            }

            ldb.update(allRemSites, allRemUnits, allRemLevels, allRemArtifacts);
        }
        else
        {

        }
        return null;
    }
}