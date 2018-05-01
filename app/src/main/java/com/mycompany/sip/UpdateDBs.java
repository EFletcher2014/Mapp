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
        System.out.println("Updating data");
        //TODO: update local db even when online
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
                Boolean contains = false;
                int loc = -1;
                for(int j=0; j<allRemSites.size(); j++)
                {
                    if((allLocSites.get(0).getRemotePK()>-1 && allRemSites.get(j).getRemotePK()==(allLocSites.get(0).getRemotePK()))
                            || allLocSites.get(0).equals(allRemSites.get(j)))
                    {//if sites have the same rpk or same site number, they are the same
                        contains=true;
                        loc = j;
                        break;
                    }
                }

                if(contains) //if site was updated both locally and remotely while offline
                {
                    if(allRemSites.get(loc).getLastUpdated().after(allLocSites.get(0).getLastUpdated())) //if remote site was updated most recently
                    {
                        ldb.updateSite(allRemSites.get(loc)); //update the local site to match it
                    }
                    else
                    {
                        int temppk=rdb.updateSite(allLocSites.get(0)); //otherwise, update remote site to match local
                        if(temppk>-1) //this means the update worked
                        {
                            allLocSites.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                            ldb.updateSite(allLocSites.get(0));
                        }
                    }
                    allRemSites.remove(loc); //was already synced, remove
                }
                else { //if local site wasn't also updated remotely
                    int temppk=rdb.updateSite(allLocSites.get(0)); //update it remotely
                    if(temppk>-1) //this means the update worked
                    {
                        allLocSites.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                        ldb.updateSite(allLocSites.get(0));
                    }
                }
                allLocSites.remove(0); //was already synced, remove
            }

            //If any remote sites are left over (weren't also updated locally while offline) update those now
            for(int j=0; j<allRemSites.size(); j++)
            {
                ldb.updateSite(allRemSites.get(j));

            }
            allRemSites.clear();

            //Loop through all units to create or update entries
            while(allLocUnits.size()>0)
            {
                Boolean contains = false;
                int loc = -1;
                for(int j=0; j<allRemUnits.size(); j++)
                {
                    if((allLocUnits.get(0).getRemotePK()>-1 && allRemUnits.get(j).getRemotePK()==(allLocUnits.get(0).getRemotePK()))
                            || allLocUnits.get(0).equals(allRemUnits.get(j)))
                    {//if units have the same rpk or same site number, they are the same
                        contains=true;
                        loc = j;
                        break;
                    }
                }

                if(contains) //if site was updated both locally and remotely while offline
                {
                    if(allRemUnits.get(loc).getLastUpdated().after(allLocUnits.get(0).getLastUpdated())) //if remote site was updated most recently
                    {
                        ldb.updateUnit(allRemUnits.get(loc)); //update the local site to match it
                    }
                    else
                    {
                        int temppk=rdb.updateUnit(allLocUnits.get(0)); //otherwise, update remote site to match local
                        if(temppk>-1) //this means the update worked
                        {
                            allLocUnits.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                            ldb.updateUnit(allLocUnits.get(0));
                        }
                    }
                    allRemUnits.remove(loc); //was already synced, remove
                }
                else { //if local site wasn't also updated remotely
                    int temppk=rdb.updateUnit(allLocUnits.get(0)); //update it remotely
                    if(temppk>-1) //this means the update worked
                    {
                        allLocUnits.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                        ldb.updateUnit(allLocUnits.get(0));
                    }
                }
                allLocUnits.remove(0); //was already synced, remove
            }

            //If any remote units are left over (weren't also updated locally while offline) update those now
            for(int j=0; j<allRemUnits.size(); j++)
            {
                ldb.updateUnit(allRemUnits.get(j));

            }
            allRemUnits.clear();

            //Loop through all levels to create or update entries
            while(allLocLevels.size()>0)
            {
                Boolean contains = false;
                int loc = -1;
                for(int j=0; j<allRemLevels.size(); j++)
                {
                    if((allLocLevels.get(0).getRemotePK()>-1 && allRemLevels.get(j).getRemotePK()==(allLocLevels.get(0).getRemotePK()))
                            || allLocLevels.get(0).equals(allRemLevels.get(j)))
                    {//if levels have the same rpk or same site number, they are the same
                        contains=true;
                        loc = j;
                        break;
                    }
                }

                if(contains) //if site was updated both locally and remotely while offline
                {
                    if(allRemLevels.get(loc).getLastUpdated().after(allLocLevels.get(0).getLastUpdated())) //if remote site was updated most recently
                    {
                        ldb.updateLevel(allRemLevels.get(loc)); //update the local site to match it
                    }
                    else
                    {
                        int temppk=rdb.updateLevel(allLocLevels.get(0)); //otherwise, update remote site to match local
                        if(temppk>-1) //this means the update worked
                        {
                            allLocLevels.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                            ldb.updateLevel(allLocLevels.get(0));
                        }
                    }
                    allRemLevels.remove(loc); //was already synced, remove
                }
                else { //if local site wasn't also updated remotely
                    int temppk=rdb.updateLevel(allLocLevels.get(0)); //update it remotely
                    if(temppk>-1) //this means the update worked
                    {
                        allLocLevels.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                        ldb.updateLevel(allLocLevels.get(0));
                    }
                }
                allLocLevels.remove(0); //was already synced, remove
            }

            //If any remote levels are left over (weren't also updated locally while offline) update those now
            for(int j=0; j<allRemLevels.size(); j++)
            {
                ldb.updateLevel(allRemLevels.get(j));

            }
            allRemLevels.clear();

            //Loop through all artifacts to create or update entries
            while(allLocArtifacts.size()>0)
            {
                Boolean contains = false;
                int loc = -1;
                for(int j=0; j<allRemArtifacts.size(); j++)
                {
                    if((allLocArtifacts.get(0).getRemotePK()>-1 && allRemArtifacts.get(j).getRemotePK()==(allLocArtifacts.get(0).getRemotePK()))
                            || allLocArtifacts.get(0).equals(allRemArtifacts.get(j)))
                    {//if artifacts have the same rpk or same site number, they are the same
                        contains=true;
                        loc = j;
                        break;
                    }
                }

                if(contains) //if site was updated both locally and remotely while offline
                {
                    if(allRemArtifacts.get(loc).getLastUpdated().after(allLocArtifacts.get(0).getLastUpdated())) //if remote site was updated most recently
                    {
                        ldb.updateArtifact(allRemArtifacts.get(loc)); //update the local site to match it
                    }
                    else
                    {
                        int temppk=rdb.updateArtifact(allLocArtifacts.get(0)); //otherwise, update remote site to match local
                        if(temppk>-1) //this means the update worked
                        {
                            allLocArtifacts.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                            ldb.updateArtifact(allLocArtifacts.get(0));
                        }
                    }
                    allRemArtifacts.remove(loc); //was already synced, remove
                }
                else { //if local site wasn't also updated remotely
                    int temppk=rdb.updateArtifact(allLocArtifacts.get(0)); //update it remotely
                    if(temppk>-1) //this means the update worked
                    {
                        allLocArtifacts.get(0).setRemotePK(temppk); //save the correct rpk to the local database
                        ldb.updateArtifact(allLocArtifacts.get(0));
                    }
                }
                allLocArtifacts.remove(0); //was already synced, remove
            }

            //If any remote artifacts are left over (weren't also updated locally while offline) update those now
            for(int j=0; j<allRemArtifacts.size(); j++)
            {
                ldb.updateArtifact(allRemArtifacts.get(j));

            }
            allRemArtifacts.clear();
                ldb.setLastUpdated(new Timestamp(System.currentTimeMillis()));



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
            if(rdb.isOnline())
            {
                System.out.println("Online syncing remote data locally...");

                //Get all entries in remote server
                ArrayList<Site> allRemSites = rdb.LoadAllSites(ldb.lastUpdated());

                ArrayList<Unit> allRemUnits = rdb.loadAllUnits(null, ldb.lastUpdated());

                ArrayList<Level> allRemLevels = rdb.loadAllLevels(null, ldb.lastUpdated());

                ArrayList<Artifact> allRemArtifacts = rdb.loadAllArtifacts(null, ldb.lastUpdated());

                for(int i=0; i<allRemSites.size(); i++)
                {
                    System.out.println("Remote site being added: " + allRemSites.get(i));
                    ldb.updateSite(allRemSites.get(i));
                }

                for(int i=0; i<allRemUnits.size(); i++)
                {
                    System.out.println("remote unit being added: " + allRemUnits.get(i));
                    ldb.updateUnit(allRemUnits.get(i));
                }

                for(int i=0; i<allRemLevels.size(); i++)
                {
                    System.out.println("remote level being added: " + allRemLevels.get(i) + " " + allRemLevels.get(i).getRemotePK());
                    ldb.updateLevel(allRemLevels.get(i));
                }

                for(int i=0; i<allRemArtifacts.size(); i++)
                {
                    ldb.updateArtifact(allRemArtifacts.get(i));
                }

                ldb.setLastUpdated(new Timestamp(System.currentTimeMillis()));
                System.out.println("Updating all sites: " + ldb.getAllSites());
            }
            else
            {
                System.out.println("Thinks it's offline...");
            }
        }
        return null;
    }
}