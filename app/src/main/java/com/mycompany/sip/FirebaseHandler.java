package com.mycompany.sip;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Document;

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
    CollectionReference artifactsRef;
    CollectionReference featuresRef;
    CollectionReference featuresLinkRef;
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

    //Level map
    private static WeakReference<LevelMap> levelMapActivityRef;

    //Artifact Bags
    private static WeakReference<AllArtifactBagsActivity> artifactBagsActivityRef;

    //Features list
    private static ArrayList<Feature> siteFeatures = new ArrayList<>();

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

    public static void updateLevelMapActivity(LevelMap activity) {
        levelMapActivityRef = new WeakReference<LevelMap>(activity);
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
                if (siteActivityRef != null && siteActivityRef.get() != null && siteActivityRef.get().isActive()) {
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
                if (unitActivityRef != null && unitActivityRef.get() != null && unitActivityRef.get().isActive()) {
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
        final ArrayList<ArtifactBag> artifactBags = new ArrayList<ArtifactBag>();
        artifactBagsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {

                if((artifactBagsActivityRef != null && artifactBagsActivityRef != null && artifactBagsActivityRef.get().isActive())
                        || levelMapActivityRef != null && levelMapActivityRef.get() != null && levelMapActivityRef.get().isActive()) {

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null && doc.get("LevelID") != null
                                && ((artifactBagsActivityRef.get() != null && doc.get("LevelID").toString().equals(artifactBagsActivityRef.get().getLevel().getID()))
                                || (levelMapActivityRef.get() != null && doc.get("LevelID").toString().equals(levelMapActivityRef.get().getLevel().getID())))) {
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
                    if (artifactBagsActivityRef.get() != null && artifactBagsActivityRef.get().isActive()) {
                        artifactBagsActivityRef.get().loadArtifactBags(artifactBags);
                    } else {
                        if (levelMapActivityRef.get() != null && levelMapActivityRef.get().isActive()) {
                            levelMapActivityRef.get().loadArtifactBags(artifactBags);
                        }
                    }
                }
            }
        });

        artifactsRef = mappDB.collection("sites").document(selectedSite.getID()).collection("artifacts");
        artifactsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                final ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

                if(levelMapActivityRef != null && levelMapActivityRef.get() != null && levelMapActivityRef.get().isActive()) {

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null) {
                            final Object tempID = doc.getId();
                            final Object tempDesc = doc.get("Name");
                            doc.getDocumentReference("ArtifactBag").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                   final Object tempLevelID = documentSnapshot.get("LevelID");

                                   if(tempLevelID.toString().equals(levelMapActivityRef.get().getLevel().getID()))
                                   {
                                       final Object tempABagID = documentSnapshot.getId();
                                       Artifact temp = new Artifact(selectedSite, levelMapActivityRef.get().getLevel().getUnit(), levelMapActivityRef.get().getLevel(),
                                               new ArtifactBag(null, null, null, tempABagID.toString(), "", -1, ""),
                                               tempID.toString(),
                                               tempDesc.toString());

                                       artifacts.add(temp);
                                       levelMapActivityRef.get().loadArtifacts(artifacts);
                                   }
                                }
                            });
                        }
                    }
                }
            }
        });

        featuresRef = mappDB.collection("sites").document(selectedSite.getID()).collection("features");
        featuresRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                final ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc != null) {
                            final Object tempID = doc.getId();
                            final Object tempDesc = doc.get("Description");
                            final Object tempNum = doc.get("Number");

                            //TODO: link to levels somehow

                            Feature temp = new Feature (tempID.toString(), tempDesc.toString(), Integer.parseInt(tempNum.toString()), selectedSite, new ArrayList<Level>());

                            siteFeatures.add(temp);
                    }

                    //TODO: check if this is associated with the current level
                    if(levelMapActivityRef != null && levelMapActivityRef.get() != null && levelMapActivityRef.get().isActive()) {
                        levelMapActivityRef.get().loadAllSiteFeatures(siteFeatures);
                    }
                }
            }
        });

        featuresLinkRef = mappDB.collection("sites").document(selectedSite.getID()).collection("featureLinks");
        featuresLinkRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                final ArrayList<Feature> features = new ArrayList<Feature>();

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : snapshot) {
                    if (doc != null) {
                        final Object levID = doc.get("LevelID");
                        final Object featID = doc.get("FeatureID");

                        Level temp = new Level(null, null, levID.toString(), -1, 0.0, 0.0, "", "", null);
                        Feature tempF = new Feature(featID.toString(), "", -1, null, new ArrayList<Level>());

                        getImage(selectedSite + "/", levID.toString()+ "-" + featID.toString() + "/", siteActivityRef.get().getCacheDir(), "f");
                        if (levelMapActivityRef != null && levelMapActivityRef.get() != null
                                && levelMapActivityRef.get().getLevel().equals(temp)
                                && siteFeatures.contains(tempF))
                        {
                            levelMapActivityRef.get().linkFeature(siteFeatures.get(siteFeatures.indexOf(tempF)), temp);
                        }
                    }
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
            if (artifactBagsActivityRef != null && artifactBagsActivityRef.get() != null && artifactBagsActivityRef.get().isActive()) {
                artifactBagsActivityRef.get().loadArtifactBags(aBags);
            } else {
                if (levelMapActivityRef != null && levelMapActivityRef.get() != null && levelMapActivityRef.get().isActive()) {
                    levelMapActivityRef.get().loadArtifactBags(aBags);
                }
            }
        }
        });
    }

    public void getArtifactsFromLevel(final Level selectedLevel)
    {
        final ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
        artifactBagsRef.whereEqualTo("LevelID", selectedLevel.getID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot bagDoc : queryDocumentSnapshots) {
                    final Object bagID = bagDoc.getId();
                    final Object tempA = bagDoc.get("AccessionNumber");
                    final Object tempCatNum = bagDoc.get("CatalogNumber");
                    final Object tempContents = bagDoc.get("Contents");

                    final ArtifactBag tempABag = new ArtifactBag(selectedLevel.getSite(), selectedLevel.getUnit(),
                            selectedLevel,
                            bagID.toString(),
                            tempA.toString(),
                            Integer.parseInt(tempCatNum.toString()),
                            tempContents.toString());

                    artifactsRef.whereEqualTo("ArtifactBagID", bagID.toString()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                if (doc != null) {
                                    final Object tempID = doc.getId();
                                    final Object tempDesc = doc.get("Name");

                                    Artifact temp = new Artifact(selectedLevel.getSite(), selectedLevel.getUnit(),
                                            selectedLevel,
                                            tempABag,
                                            tempID.toString(),
                                            tempDesc.toString());

                                    if(levelMapActivityRef != null && levelMapActivityRef.get() != null)
                                    {
                                        getImage(levelMapActivityRef.get().getLevel().getSite().getNumber() + "/" + levelMapActivityRef.get().getLevel().getUnit().getDatum() + "/level" + levelMapActivityRef.get().getLevel().getNumber() + "/",
                                                temp.getID(), levelMapActivityRef.get().getCacheDir(), "a");
                                    }

                                    artifacts.add(temp);
                                }
                            }
                            if(levelMapActivityRef != null && levelMapActivityRef.get() != null) {
                                levelMapActivityRef.get().loadArtifacts(artifacts);
                            }
                        }
                    });
                }
            }
        });
    }

    public void getFeaturesFromSite()
    {
        featuresRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Feature> features = new ArrayList<Feature>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc != null) {
                        final Object tempID = doc.getId();
                        final Object tempDesc = doc.get("Description");
                        final Object tempNum = doc.get("Number");

                        Feature temp = new Feature(tempID.toString(), tempDesc.toString(), Integer.parseInt(tempNum.toString()), levelMapActivityRef.get().getLevel().getSite(), new ArrayList<Level>());

                        features.add(temp);
                    }
                }
                if(levelMapActivityRef != null && levelMapActivityRef.get() != null
                        && levelMapActivityRef.get().isActive()) {
                    levelMapActivityRef.get().loadAllSiteFeatures(features);
                }
            }
        });
    }

    public void getFeaturesFromLevel(final Level selectedLevel)
    {
        featuresLinkRef.whereEqualTo("LevelID", selectedLevel.getID()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                final ArrayList<Feature> features = new ArrayList<Feature>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc != null) {
                        final Object levID = doc.get("LevelID");
                        final Object featID = doc.get("FeatureID");

                        featuresRef.document(featID.toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot doc1) {
                                if (doc1 != null)
                                {
                                    final Object desc = doc1.get("Description");
                                    final Object num = doc1.get("Number");

                                    ArrayList<Level> levs = new ArrayList<>();
                                    levs.add(new Level(null, null, levID.toString(), -1, 0.0, 0.0, "", "", null));
                                    Feature temp = new Feature(featID.toString(), desc.toString(), Integer.parseInt(num.toString()), selectedLevel.getSite(), levs);

                                    features.add(temp);
                                }

                                if (levelMapActivityRef != null && levelMapActivityRef.get() != null
                                        && levelMapActivityRef.get().getLevel().getID().equals(levID.toString()))
                                {
                                    levelMapActivityRef.get().loadFeatures(features);
                                }
                            }
                        });
                    }
                }
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

    public void createArtifact(Artifact art)
    {
        new CreateNewArtifact(art).execute();
    }

    class CreateNewArtifact extends AsyncTask<String, String, String> {

        Artifact newArt;

        public CreateNewArtifact(Artifact n)
        {
            newArt = n;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating artifact
         * */
        protected String doInBackground(String... args) {

            Map<String, Object> temp = new HashMap<>();
            temp.put("ArtifactBag", mappDB.collection("sites").document(newArt.getSite().getID())
                    .collection("artifactBags").document(newArt.getArtifactBag().getID()));
            temp.put("ArtifactBagID", newArt.getArtifactBag().getID());
            temp.put("Name", newArt.getDescription());

            if(newArt.getID() == null || newArt.getID() == "") {
                mappDB.collection("sites").document(newArt.getSite().getID()).collection("artifacts").document().set(temp);
            }
            else
            {
                mappDB.collection("sites").document(newArt.getSite().getID()).collection("artifacts").document(newArt.getID()).set(temp);
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

        }

    }

    public void createFeature(Feature f)
    {
        new CreateNewFeature(f).execute();
    }

    class CreateNewFeature extends AsyncTask<String, String, String> {

        Feature newFeat;

        public CreateNewFeature(Feature f)
        {
            newFeat = f;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating feature
         * */
        protected String doInBackground(String... args) {

            Map<String, Object> temp = new HashMap<>();
            temp.put("Description", newFeat.getDescription());
            temp.put("Number", siteFeatures.size());

            if(newFeat.getID() == null || newFeat.getID() == "") {
                mappDB.collection("sites").document(newFeat.getSite().getID()).collection("features").document().set(temp);
            }
            else
            {
                mappDB.collection("sites").document(newFeat.getSite().getID()).collection("features").document(newFeat.getID()).set(temp);
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

        }

    }

    public void createFeatureLink(Feature f, Level l)
    {
        new CreateNewFeatureLink(f, l).execute();
    }

    class CreateNewFeatureLink extends AsyncTask<String, String, String> {

        Feature feat;
        Level lev;

        public CreateNewFeatureLink(Feature f, Level l)
        {
            feat = f;
            lev = l;
        }

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Creating feature link
         * */
        protected String doInBackground(String... args) {

            Map<String, Object> temp = new HashMap<>();
            temp.put("FeatureID", feat.getID());
            temp.put("LevelID", lev.getID());

            mappDB.collection("sites").document(lev.getSite().getID()).collection("featureLinks").document().set(temp);
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {

        }

    }

    public void setImage(String path, String name, String extension, Uri u)
    {
        StorageReference imageRef = storageRef.child(path + "" + name + "" + extension);
        UploadTask uploadTask = imageRef.putFile(u);
    }

    public void getImage(String path, String name, File f, String method)
    {
        downloadImage dl = new downloadImage(path, name, f, method);
        dl.execute();
    }

    class downloadImage extends AsyncTask<String, String, String> {
        String remLocation;
        String locLocation;
        String name;
        String type;
        Uri localImageUri;
        File dir;

        public downloadImage(String lLoc, String n, File f, String t) {
            name = n;
            type = t;
            remLocation = lLoc + n + ".jpg";
            locLocation = lLoc;
            dir = f;
        }

        @Override
        protected void onPreExecute() {

        }

        protected String doInBackground(String... args) {
            StorageReference imageRef = storageRef.child(remLocation);
            try {
                File tempF = new File(dir, locLocation);
                if(!tempF.exists()) {
                    tempF.mkdirs();
                }
                localFile = File.createTempFile(name,".jpg", tempF);
                imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Uri tempPath = Uri.fromFile(localFile);

                        localImageUri = tempPath;
                        if(levelDocActivityRef != null && levelDocActivityRef.get() != null && levelDocActivityRef.get().isActive())
                        {
                            levelDocActivityRef.get().setURI(localImageUri);
                        }
                        if(levelMapActivityRef != null && levelMapActivityRef.get() != null && levelMapActivityRef.get().isActive())
                        {
                            if(type.equals("a")) {
                                levelMapActivityRef.get().loadArtifactImage(localFile);
                            }
                            else
                            {
                                if(type.equals("f")) {
                                    levelMapActivityRef.get().loadFeatureImage(localFile);
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e);
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
    }

    public void getAllImagesFromLevel(String path, String name, File f)
    {
        downloadImagesFromLevel dl = new downloadImagesFromLevel(path, name, f);
        dl.execute();
    }

    class downloadImagesFromLevel extends AsyncTask<String, String, String> {
        String remLocation;
        String locLocation;
        String name;
        Uri localImageUri;
        File dir;

        public downloadImagesFromLevel(String lLoc, String n, File f) {
            name = n;
            remLocation = lLoc + n + ".jpg";
            locLocation = lLoc;
            dir = f;
        }

        @Override
        protected void onPreExecute() {

        }

        protected String doInBackground(String... args) {
            StorageReference levelImageRef = storageRef.child(remLocation);
            try {
                File tempF = new File(dir, locLocation);
                if (!tempF.exists()) {
                    tempF.mkdirs();
                }
                localFile = File.createTempFile(name, ".jpg", tempF);
                levelImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String tempPath = localFile.getPath();

                        localImageUri = Uri.parse(tempPath);
                        if (levelDocActivityRef.get().isActive()) {
                            levelDocActivityRef.get().setURI(localImageUri);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e);
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
    }


    //TODO: add methods and classes to add units, levels, and artifacts
}
