package com.mycompany.sip;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mycompany.sip.Site;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.*;

//Async Class to Update the local and remote servers
class UpdateDBs extends AsyncTask<String, String, String>
{
    //TODO: compare local and remote timestamps to ensure newest record is being preserved
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
        if(rdb.isOnline() && offlineSince.after(new Timestamp(0))) {     //If rdb is newly online--was just offline and offlineSince hasn't been changed yet

            try {


                /*//Get all entries in remote server
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
                }*/

                //Get entries from local server and loop through to add them to the remote
                ArrayList<Site> allLocSites = (ArrayList) ldb.getAllSitesUpdatedAfter(offlineSince);
                //System.out.println("Local sites: " + allLocSites);

//                for (int i = 0; i < allLocSites.size(); i++) {
//                    Site temp = allLocSites.get(i);
//                    //System.out.println("Analyzing site: " + temp);
//                    if (temp.getFirstCreated().after(offlineSince))/*!allRemSites.contains(temp))*/ {    //If created while offline add to remote
//                        Site temp1 = new Site(temp.getName(), temp.getNumber(), temp.getDateOpened(), temp.getLocation(), temp.getDescription(), -1, new Timestamp(0), new Timestamp(0));
//                        rdb.CreateNewSite(temp1);
//                        //System.out.println("Added new site: " + temp1);
//                    } else if (temp.getLastUpdated().after(offlineSince)){     //If updated while offline add update to remote
//                        rdb.CreateNewSite(temp);
//                        //System.out.println("Updated site: " + temp.getPk());
//                    }
//                }

                //Update Remote server
                ArrayList<Unit> allLocUnits = (ArrayList) ldb.getAllUnitsUpdatedAfter(offlineSince);

//                for (int i = 0; i < allLocUnits.size(); i++) {
//                    Unit temp = allLocUnits.get(i);
//                    if (temp.getFirstCreated().after(offlineSince)/*!allRemUnits.contains(temp)*/) {    //If created while offline add to remote
//                        Unit temp1 = new Unit(temp.getDatum(), temp.getDateOpened(), temp.getNsDimension(), temp.getEwDimension(), temp.getSite(), temp.getExcavators(), temp.getReasonForOpening(), -1, new Timestamp(0), new Timestamp(0));
//                        rdb.createNewUnit(temp1);
//                    } else if(temp.getLastUpdated().after(offlineSince)){    //If updated while offline add update to remote
//                        rdb.createNewUnit(temp);
//                    }
//                }

                //Update Remote Server
                ArrayList<Level> allLocLevels = (ArrayList) ldb.getAllLevelsUpdatedAfter(offlineSince);

//                for (int i = 0; i < allLocLevels.size(); i++) {
//                    Level temp = allLocLevels.get(i);
//                    if (temp.getFirstCreated().after(offlineSince)/*!allRemLevels.contains(temp)*/) {    //If created while offline add to remote
//                        Level temp1 = new Level(temp.getNumber(), temp.getBegDepth(), temp.getEndDepth(), temp.getSite(), temp.getUnit(), temp.getDateStarted(), temp.getExcavationMethod(), temp.getNotes(), -1, new Timestamp(0), new Timestamp(0));
//                        rdb.createNewLevel(temp1);
//                    } else  if(temp.getLastUpdated().after(offlineSince)){     //if updated while offline add updates to remote
//                        rdb.createNewLevel(temp);
//                    }
//                    //System.out.println("Updated level: " + allLocLevels.get(i).getPk());
//
//                }

                //Update remote server
                ArrayList<Artifact> allLocArtifacts = (ArrayList) ldb.getAllArtifactsUpdatedAfter(offlineSince);

//                for (int i = 0; i < allLocArtifacts.size(); i++) {
//                    Artifact temp = allLocArtifacts.get(i);
//
//                    if (temp.getFirstCreated().after(offlineSince)/*!allRemArtifacts.contains(temp)*/) {   //if created while offline add to remote
//                        Artifact temp1 = new Artifact(temp.getSite(), temp.getUnit(), temp.getLevel(), temp.getAccessionNumber(), temp.getCatalogNumber(), temp.getContents(), -1, new Timestamp(0), new Timestamp(0));
//                        rdb.createNewArtifact(temp1);
//                    } else if (temp.getLastUpdated().after(offlineSince)){     //if updated while offline add updates to remote
//                        rdb.createNewArtifact(temp);
//                    }
//                    //System.out.println("Updated artifact: " + allLocArtifacts.get(i).getPk());
//                }

            //Get all entries in remote server
            ArrayList<Site> allRemSites = rdb.LoadAllSites(offlineSince);

            ArrayList<Unit> allRemUnits = rdb.loadAllUnits(null, offlineSince);

            ArrayList<Level> allRemLevels = rdb.loadAllLevels(null, offlineSince);

            ArrayList<Artifact> allRemArtifacts = rdb.loadAllArtifacts(null, offlineSince);

            //Loop through all sites to create or update entries
            while(allLocSites.size()>0)
            {
                if(allLocSites.get(0).getRemotePK()<0)
                {
                    allRemSites.add(allLocSites.get(0)); //TODO: figure out how to save the remote key to the local db here
                    allLocSites.remove(0);
                }
                else
                {
                    Boolean contains = false;
                    int loc = -1;
                    for(int j=0; j<allRemSites.size(); j++)
                    {
                        if(allRemSites.get(j).getRemotePK()==(allLocSites.get(0).getRemotePK()))
                        {
                            contains=true;
                            loc = j;
                            break;
                        }
                    }

                    if(contains)
                    {
                        if(allRemSites.get(loc).getLastUpdated().after(allLocSites.get(0).getLastUpdated()))
                        {
                            ldb.updateSite(allRemSites.get(loc));
                            allRemSites.remove(loc);
                        }
                        else
                        {
                            rdb.CreateNewSite(allLocSites.get(0)); //TODO: figure out how to save the remote key to the local db here
                            allLocSites.remove(0);
                        }
                    }
                    else
                    {
                        rdb.CreateNewSite(allLocSites.get(0));
                        allLocSites.remove(0);
                    }
                }
            }

            for(int j=0; j<allRemSites.size(); j++)
            {
                ldb.addSite(allRemSites.get(j));

            }
            allRemSites.clear();

            //TODO: repeat for units, levels, artifacts



        //TODO: return something useful
    } catch (NullPointerException e) {
        //TODO: return something else useful
    }


//            //Loop through all sites to save new creations/updates
//            for (int j = 0; j<allRemSites.size(); j++)
//            {
//                if(allRemSites.get(j).getFirstCreated().after(offlineSince)) //If created since offline
//                {
//                    ldb.addSite(allRemSites.get(j));
//                }
//                else if(allRemSites.get(j).getLastUpdated().after(offlineSince)) //If updated since offline
//                {
//                    ldb.updateSite(allRemSites.get(j));
//                }
//
//                //Get all units associated with this site
//                allRemUnits.addAll(rdb.loadAllUnits(allRemSites.get(j)));
//
//                //Loop through all units
//                for (int k = 0; k<allRemUnits.size(); k++)
//                {
//                    if(allRemUnits.get(k).getFirstCreated().after(offlineSince))
//                    {
//                        ldb.addUnit(allRemUnits.get(k));
//                    }
//                    else if(allRemUnits.get(k).getLastUpdated().after(offlineSince)) //If updated since offline
//                    {
//                        ldb.updateUnit(allRemUnits.get(k));
//                    }
//
//                    //Get all levels associated with this unit
//                    allRemLevels.addAll(rdb.loadAllLevels(allRemUnits.get(k)));
//
//
//                    //loop through levels to add new updates/creations
//                    for (int l = 0; l<allRemLevels.size(); l++)
//                    {
//                        if(allRemLevels.get(l).getFirstCreated().after(offlineSince)) {
//                            ldb.addLevel(allRemLevels.get(l));
//                        }
//                        else if(allRemLevels.get(l).getLastUpdated().after(offlineSince)) //If updated since offline
//                        {
//                            ldb.updateLevel(allRemLevels.get(l));
//                        }
//
//                        //Get all artifacts associated with this level
//                        allRemArtifacts.addAll(rdb.loadAllArtifacts(allRemLevels.get(l)));
//
//                        //Loop through all artifacts to add new updates/creations
//                        for (int m = 0; m<allRemArtifacts.size(); m++)
//                        {
//                            if(allRemArtifacts.get(m).getFirstCreated().after(offlineSince))
//                            {
//                                ldb.addArtifact(allRemArtifacts.get(m));
//                            }
//                            else if(allRemArtifacts.get(m).getLastUpdated().after(offlineSince)) //If updated since offline
//                            {
//                                ldb.updateArtifact(allRemArtifacts.get(m));
//                            }
//                        }
//                        allRemArtifacts.clear();
//                    }
//                    allRemLevels.clear();
//                }
//                allRemUnits.clear();
//            }

            /*for (int j = 0; j < allRemSites.size(); j++) {
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

            ldb.update(allRemSites, allRemUnits, allRemLevels, allRemArtifacts);*/
        }
        else
        {
            //Not online
        }
        return null;
    }
}