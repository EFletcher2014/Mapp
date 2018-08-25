package com.mycompany.sip;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class FirebaseHandler {

    //instance
    private static FirebaseHandler instance = null;

    //Firebase
    FirebaseFirestore mappDB = FirebaseFirestore.getInstance();
    CollectionReference sitesRef; //todo: will be a docref once users added
    CollectionReference unitsRef;
    CollectionReference levelsRef;
    CollectionReference artifactBagsRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    Task<QuerySnapshot> task;
    File localFile;

    //Sites
    private static WeakReference<AllSitesActivity> siteActivityRef;

    //Units
    private static WeakReference<AllUnitsActivity> unitActivityRef;

    //Levels
    private static WeakReference<AllLevelsActivity> levelActivityRef;

    //Level documentation
    private static WeakReference<LevelDocument> levelDocActivityRef;

    //Artifact Bags
    private static WeakReference<AllArtifactBagsActivity> artifactBagsActivityRef;

    public static FirebaseHandler getInstance()
    {
        if(instance == null)
        {
            instance = new FirebaseHandler();
        }
        return instance;
    }

    public static void updateSiteActivity(AllSitesActivity activity) {
        siteActivityRef = new WeakReference<AllSitesActivity>(activity);
    }

    public static void updateUnitActivity(AllUnitsActivity activity) {
        unitActivityRef = new WeakReference<AllUnitsActivity>(activity);
    }

    public static void updateLevelActivity(AllLevelsActivity activity) {
        levelActivityRef = new WeakReference<AllLevelsActivity>(activity);
    }

    public static void updateLevelDocActivity(LevelDocument activity) {
        levelDocActivityRef = new WeakReference<LevelDocument>(activity);
    }

    public static void updateArtifactBagActivity(AllArtifactBagsActivity activity) {
        artifactBagsActivityRef = new WeakReference<AllArtifactBagsActivity>(activity);
    }

    public void getSitesListener() {
        sitesRef = mappDB.collection("sites");
        sitesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (siteActivityRef.get().isActive()) {
                    ArrayList<Site> sites = new ArrayList<Site>();

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null) {
                            Object tempName = doc.get("Name");
                            Object tempNum = doc.get("Number");
                            Object tempDesc = doc.get("Description");
                            Object tempDate = doc.get("DateDiscovered");
                            Object tempLat = doc.get("Latitude");
                            Object tempLong = doc.get("Longitude");

                            Site temp = new Site(doc.getId(), (tempName == null ? "" : tempName.toString()),
                                    (tempNum == null ? "" : tempNum.toString()), (tempDesc == null ? "" : tempDesc.toString()),
                                    (tempDate == null ? "" : tempDate.toString()), (tempLat == null ? 0.0 : Double.parseDouble(tempLat.toString())),
                                    (tempLong == null ? 0.0 : Double.parseDouble(tempLong.toString())));

                            sites.add(temp);
                        }
                    }
                    //new LoadAllSites().execute();
                    siteActivityRef.get().loadSites(sites);
                }
            }
        });
    }

    public void createSite(Site newSite) {
        new CreateNewSite(newSite).execute();
    }

    class CreateNewSite extends AsyncTask<String, String, String> {
        boolean success = false;
        Site newSite;
        Map<String, Object> temp = new HashMap<>();

        CreateNewSite(Site s)
        {
            newSite = s;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        //TODO: is this necessary?
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            temp.put("Description", newSite.getDescription());
            temp.put("Name", newSite.getName());
            temp.put("Number", newSite.getNumber());
            temp.put("DateDiscovered", newSite.getDateOpened());
            temp.put("Latitude", newSite.getDatum().latitude);
            temp.put("Longitude", newSite.getDatum().longitude);
        }

        /**
         * Creating site
         */
        protected String doInBackground(String... args) {
            mappDB.collection("sites").add(temp);
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {

        }

    }

    public void siteSelected(final Site selectedSite)
    {
        unitsRef = mappDB.collection("sites").document(selectedSite.getID()).collection("units");
        unitsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (unitActivityRef.get().isActive()) {
                    ArrayList<Unit> units = new ArrayList<Unit>();

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null) {
                            Object tempNSC = doc.get("NSCoor");
                            Object tempEWC = doc.get("EWCoor");
                            Object tempNSD = doc.get("NSDim");
                            Object tempEWD = doc.get("EWDim");
                            Object tempReas = doc.get("ReasonForOpening");
                            Object tempD = doc.get("DateOpened");

                            Unit temp = new Unit(selectedSite, doc.getId(), (tempNSC == null ? 0 : Integer.parseInt(tempNSC.toString())),
                                    (tempEWC == null ? 0 : Integer.parseInt(tempEWC.toString())),
                                    (tempNSD == null ? 0 : Integer.parseInt(tempNSD.toString())),
                                    (tempEWD == null ? 0 : Integer.parseInt(tempEWD.toString())),
                                    (tempD == null ? "" : tempD.toString()),
                                    (tempReas == null ? "" : tempReas.toString()));

                            units.add(temp);
                        }
                    }
                    unitActivityRef.get().loadUnits(units);
                }
            }
        });

        levelsRef = mappDB.collection("sites").document(selectedSite.getID()).collection("levels");
        levelsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                ArrayList<Level> levels = new ArrayList<Level>();

                if(levelActivityRef != null && levelActivityRef.get() != null && levelActivityRef.get().isActive()) {

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null && doc.get("UnitID") != null && doc.get("UnitID").toString().equals(levelActivityRef.get().getUnit().getID())) {
                            final Object tempID = doc.getId();
                            final Object tempNum = doc.get("Number");
                            final Object tempbD = doc.get("BegDepth");
                            final Object tempeD = doc.get("EndDepth");
                            final Object tempexM = doc.get("ExcavationMethod");
                            final Object tempNotes = doc.get("Notes");

                            Level temp = new Level(selectedSite, levelActivityRef.get().getUnit(),
                                    tempID.toString(),
                                    Integer.parseInt(tempNum.toString()),
                                    Double.parseDouble(tempbD.toString()),
                                    Double.parseDouble(tempeD.toString()),
                                    tempexM.toString(),
                                    tempNotes.toString(),
                                    null);

                            levels.add(temp);
                        }
                    }
                    levelActivityRef.get().loadLevels(levels);
                }
            }
        });

        artifactBagsRef = mappDB.collection("sites").document(selectedSite.getID()).collection("artifactBags");
        artifactBagsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                ArrayList<ArtifactBag> artifactBags = new ArrayList<ArtifactBag>();

                if(artifactBagsActivityRef != null && artifactBagsActivityRef.get() != null && artifactBagsActivityRef.get().isActive()) {

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null && doc.get("LevelID") != null && doc.get("LevelID").toString().equals(artifactBagsActivityRef.get().getLevel().getID())) {
                            final Object tempID = doc.getId();
                            final Object tempANum = doc.get("AccessionNumber");
                            final Object tempCNum = doc.get("CatalogNumber");
                            final Object tempCon = doc.get("Contents");

                            ArtifactBag temp = new ArtifactBag(selectedSite, artifactBagsActivityRef.get().getLevel().getUnit(), artifactBagsActivityRef.get().getLevel(),
                                    tempID.toString(),
                                    tempANum.toString(),
                                    Integer.parseInt(tempCNum.toString()),
                                    tempCon.toString());

                            artifactBags.add(temp);
                        }
                    }
                    artifactBagsActivityRef.get().loadArtifactBags(artifactBags);
                }
            }
        });
    }

    public void getLevelsFromUnit(final Unit selectedUnit)
    {
        levelsRef.whereEqualTo("UnitID", selectedUnit.getID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Level> levels = new ArrayList<Level>();
                for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                {
                    if (doc != null) {
                        final Object tempID = doc.getId();
                        final Object tempNum = doc.get("Number");
                        final Object tempbD = doc.get("BegDepth");
                        final Object tempeD = doc.get("EndDepth");
                        final Object tempexM = doc.get("ExcavationMethod");
                        final Object tempNotes = doc.get("Notes");

                        Level temp = new Level(selectedUnit.getSite(), selectedUnit,
                                tempID.toString(),
                                Integer.parseInt(tempNum.toString()),
                                Double.parseDouble(tempbD.toString()),
                                Double.parseDouble(tempeD.toString()),
                                tempexM.toString(),
                                tempNotes.toString(),
                                null);

                        levels.add(temp);
                    }
                }
                levelActivityRef.get().loadLevels(levels);
            }
        });
    }

    public void getArtifactBagsFromLevel(final Level selectedLevel)
    {
        artifactBagsRef.whereEqualTo("LevelID", selectedLevel.getID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
            ArrayList<ArtifactBag> aBags = new ArrayList<ArtifactBag>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (doc != null) {
                    final Object tempID = doc.getId();
                    final Object tempA = doc.get("AccessionNumber");
                    final Object tempCatNum = doc.get("CatalogNumber");
                    final Object tempContents = doc.get("Contents");

                    ArtifactBag temp = new ArtifactBag(selectedLevel.getSite(), selectedLevel.getUnit(),
                            selectedLevel,
                            tempID.toString(),
                            tempA.toString(),
                            Integer.parseInt(tempCatNum.toString()),
                            tempContents.toString());

                    aBags.add(temp);
                }
            }
            artifactBagsActivityRef.get().loadArtifactBags(aBags);
            }
        });
    }

    public void createUnit(Unit newUnit)
    {
        new CreateNewUnit(newUnit).execute();
    }

    class CreateNewUnit extends AsyncTask<String, String, String> {
        boolean success = false;
        Unit newUnit;
        Map<String, Object> temp = new HashMap<>();

        CreateNewUnit(Unit u)
        {
            newUnit = u;
        }

        /**
         * Before starting background thread Show Progress Dialog
         */
        //TODO: is this necessary?
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            temp.put("NSCoor", newUnit.getNSCoor());
            temp.put("EWCoor", newUnit.getEWCoor());
            temp.put("NSDim", newUnit.getNSDim());
            temp.put("EWDim", newUnit.getEWDim());
            temp.put("DateOpened", newUnit.getDateOpened());
            temp.put("ReasonForOpening", newUnit.getReasonForOpening());
        }

        /**
         * Creating unit
         */
        protected String doInBackground(String... args) {
            mappDB.collection("sites").document(newUnit.getSite().getID()).collection("units").add(temp);
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {

        }

    }

    public void createLevel(Level newLevel)
    {
        new CreateNewLevel(newLevel).execute();
    }

    /**
     * Background Async Task to Create new level
     * */
    class CreateNewLevel extends AsyncTask<String, String, String> {

        Level newLevel;
        Map<String, Object> temp = new HashMap<>();

        public CreateNewLevel(Level level)
        {
            newLevel = level;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating level
         * */
        protected String doInBackground(String... args) {

            Map<String, Object> temp = new HashMap<>();
            temp.put("UnitID", newLevel.getUnit().getID());
            temp.put("Number", newLevel.getNumber());
            temp.put("Notes", newLevel.getNotes());
            temp.put("ExcavationMethod", newLevel.getExcavationMethod());
            temp.put("BegDepth", newLevel.getBegDepth());
            temp.put("EndDepth", newLevel.getEndDepth());

            if(newLevel.getID() == null)
            {
                mappDB.collection("sites").document(newLevel.getSite().getID()).collection("levels").document().set(temp);
            }
            else {
                mappDB.collection("sites").document(newLevel.getSite().getID()).collection("levels").document(newLevel.getID()).set(temp);
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
        }

    }

    public void createArtifactBag(ArtifactBag aBag)
    {
        new CreateNewArtifactBag(aBag).execute();
    }

    class CreateNewArtifactBag extends AsyncTask<String, String, String> {

        ArtifactBag newABag;

        public CreateNewArtifactBag(ArtifactBag n)
        {
            newABag = n;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating artifactBag
         * */
        protected String doInBackground(String... args) {

            Map<String, Object> temp = new HashMap<>();
            temp.put("LevelID", newABag.getLevel().getID());
            temp.put("AccessionNumber", newABag.getAccessionNumber());
            temp.put("CatalogNumber", newABag.getCatalogNumber());
            temp.put("Contents", newABag.getContents());

            if(newABag.getID() == null || newABag.getID() == "") {
                mappDB.collection("sites").document(newABag.getSite().getID()).collection("artifactBags").document().set(temp);
            }
            else
            {
                mappDB.collection("sites").document(newABag.getSite().getID()).collection("artifactBags").document(newABag.getID()).set(temp);
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

        }

    }

    public Uri getImage(Level l, String path, String name)
    {
        downloadImage dl = new downloadImage(l, path, name);
        dl.execute();
        return dl.getLocalImageUri();
    }

    class downloadImage extends AsyncTask<String, String, String> {
        Level level;
        String remLocation;
        String locLocation;
        String name;
        Uri localImageUri;

        public downloadImage(Level l, String lLoc, String n) {
            level = l;
            name = n;
            remLocation = lLoc + n;
            locLocation = lLoc;
        }

        @Override
        protected void onPreExecute() {

        }

        protected String doInBackground(String... args) {
            StorageReference levelImageRef = storageRef.child(remLocation);
            try {
                File tempF = new File(locLocation);
                tempF.mkdirs();
                localFile = File.createTempFile(name,".jpg", tempF);
                levelImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String tempPath = localFile.getPath();

                        localImageUri = Uri.parse(tempPath);
                    }
                });
            } catch (IOException x) {
                System.err.println(x);

                //TODO: handle errors
            }
            return null;
        }
        protected void onPostExecute(String file_url) {


        }

        public Uri getLocalImageUri()
        {
            return localImageUri;
        }
    }


    //TODO: add methods and classes to add units, levels, and artifacts
}
