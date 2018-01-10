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

                //Get entries from local server and loop through to add them to the remote
                ArrayList<Site> allLocSites = (ArrayList) ldb.getAllSites();
                //System.out.println("Local sites: " + allLocSites);

                for (int i = 0; i < allLocSites.size(); i++) {
                    Site temp = allLocSites.get(i);
                    //System.out.println("Analyzing site: " + temp);
                    if (!allRemSites.contains(temp)) {
                        Site temp1 = new Site(temp.getName(), temp.getNumber(), temp.getDateOpened(), temp.getLocation(), temp.getDescription(), -1);
                        rdb.CreateNewSite(temp1);
                        //System.out.println("Added new site: " + temp1);
                    } else {
                        rdb.CreateNewSite(temp);
                        //System.out.println("Updated site: " + temp.getPk());
                    }
                }

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
                }

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
                    //System.out.println("Updated level: " + allLocLevels.get(i).getPk());

                }

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
                    //System.out.println("Updated artifact: " + allLocArtifacts.get(i).getPk());
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