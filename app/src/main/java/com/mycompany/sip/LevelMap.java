package com.mycompany.sip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.TAG_ARTIFACT;
import static com.mycompany.sip.Global.TAG_ARTIFACT_BAG;
import static com.mycompany.sip.Global.TAG_FEATURE;
import static com.mycompany.sip.Global.TAG_PID;

public class LevelMap extends AppCompatActivity {

    public static boolean isActive;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

    private DrawingView imageDraw;
    public static Bitmap bitmap;
    private static ViewSwitcher switcher;
    private static ViewSwitcher keySwitcher;
    private AlertDialog.Builder artifactAlert;
    private AlertDialog.Builder featureAlert;
    private ArrayList<Artifact> allArtifacts = new ArrayList<>();
    private ArrayList<Artifact> newArtifacts = new ArrayList<>();
    private ArrayList<ArtifactBag> allArtifactBags = new ArrayList<>();
    private ArrayList<Feature> allSiteFeatures = new ArrayList<>();
    private ArrayList<Feature> features = new ArrayList<>();
    private ArrayList<Feature> newFeatures = new ArrayList<>();
    ArrayList<HashMap<String, String>> siteFeaturesList;
    ArrayList<HashMap<String, String>> featuresList;
    ArrayList<HashMap<String, String>> artifactsList;
    ArrayList<HashMap<String, String>> artifactBagsList;
    private ListView artifacts;
    private ListView featuresLV;
    private SpinnerAdapter aBagAdapter;
    private Spinner aBagChoose;
    private SpinnerAdapter featureAdapter;
    private Spinner featureChoose;
    private String featureID;
    private EditText name;
    private String aBagID;
    private View saveArtifact;
    private View saveFeature;
    private Uri selectedImageUri;
    private File cache;
    private ArrayList<String> artifactsImages = new ArrayList<>();
    private ArrayList<String> featuresImages = new ArrayList<>();
    private String displayedImage = "";
    private String drawType = "";

    private static Unit unit;
    private Level level;

    //Used to tell FirebaseHandler and DrawingView if this activity is active
    @Override
    public void onStart()
    {
        super.onStart();
        isActive = true;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        isActive = false;
    }

    public boolean isActive()
    {
        return isActive;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bitmap=null;
        super.onCreate(savedInstanceState);
        fbh.updateLevelMapActivity(this); //give fbh and imageDraw a reference to this activity so they can get data from it
        imageDraw.updateLevelMapActivity(this);
        this.isActive = true;
        setContentView(R.layout.activity_level_map);
        Intent openIntent = getIntent();
        selectedImageUri = openIntent.getData();
        level = openIntent.getParcelableExtra("level");
        unit = level.getUnit();
        cache = this.getCacheDir();

        keySwitcher = findViewById(R.id.keySwitcher); //switcher view which will alternate between showing the key and an alert that the user might highlight something

        if(keySwitcher.getNextView() == findViewById(R.id.artifactFeatureList)) //should start by showing map key
        {
            keySwitcher.showNext();
        }

        //switcher switches between displaying a warning that an image must be added and an image, if there is one
        //TODO: is this still needed? The user can't get to this screen without an image selected
        switcher = (ViewSwitcher) findViewById(R.id.switchDrawView);
        if(switcher.getNextView()!=findViewById(R.id.draw))
        {
            switcher.showNext();
        }

        //If an image was selected in the levelDocument activity
        if(selectedImageUri!=null)
        {
            imageDraw = (DrawingView) findViewById(R.id.draw);  //the canvas to draw on
            imageDraw.setUri(selectedImageUri);
            imageDraw.setCanvasBitmap();

            LayoutInflater inflater = getLayoutInflater();
            saveArtifact = inflater.inflate(R.layout.new_artifact_dialog, null); //the alert to save a new artifact
            saveFeature = inflater.inflate(R.layout.link_feature_dialog, null); //the alert to save a new feature

            aBagChoose = (Spinner) saveArtifact.findViewById(R.id.artifactBagSelect); //in the saveArtifact alert, this allows the user to link their artifact to an artifact bag
            aBagChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    aBagID = ((HashMap<String, String>) item).get(TAG_PID);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            name = (EditText) saveArtifact.findViewById(R.id.artifactName);

            featureChoose = (Spinner) saveFeature.findViewById(R.id.featureSelect); //in the saveFeature alert, this allows a user to choose from existing features
            featureChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    featureID = ((HashMap<String, String>) item).get(TAG_PID);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            switcher.showNext(); //if image not null, this switcher should show the image, not the warning to save an image first
        }


        //TODO: make this align right. Will have to edit list view
        artifacts = (ListView) findViewById(R.id.artifactListMap); //list of artifacts to display in the map key
        artifacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(!displayedImage.equals(artifactsImages.get((int) id)) && !artifactsImages.get((int) id).equals(selectedImageUri.toString())){ //if that file isn't displayed currently:
                    //get the file associated with this artifact
                    File selectedFile = new File(artifacts.getContext().getCacheDir(), artifactsImages.get((int) id));
                    //display that file
                    imageDraw.setUri(Uri.fromFile(selectedFile));
                    imageDraw.setCanvasBitmap();
                    displayedImage = artifactsImages.get((int) id);
                }
                else //if the file is displayed currently
                {
                    imageDraw.setUri(selectedImageUri);
                    imageDraw.setCanvasBitmap();
                    displayedImage = "";
                }
                //refresh the image
                switcher.showNext();
                switcher.showNext();
            }
        });

        featuresLV = (ListView) findViewById(R.id.featureListMap); //list of features for the map key
        featuresLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!displayedImage.equals(featuresImages.get((int) id)) && !featuresImages.get((int) id).equals(selectedImageUri.toString())) { //if that file isn't displayed right now:
                    //get the file associated with this feature
                    File selectedFile = new File(featuresLV.getContext().getCacheDir(), featuresImages.get((int) id));
                    imageDraw.setUri(Uri.fromFile(selectedFile));
                    imageDraw.setCanvasBitmap();
                    displayedImage = featuresImages.get((int) id);
                }
                else // if that file is displayed right now
                {
                    //display the level map
                    imageDraw.setUri(selectedImageUri);
                    imageDraw.setCanvasBitmap();
                    displayedImage = "";
                }
                //refresh image
                switcher.showNext();
                switcher.showNext();
            }
        });


        //load items from Firebase
        fbh.getArtifactsFromLevel(level);
        fbh.getArtifactBagsFromLevel(level);
        fbh.getFeaturesFromLevel(level);
        fbh.getFeaturesFromSite(level.getSite());

        //the button to add a new artifact to the list
        final Button addArtifact = (Button) findViewById(R.id.addArtifactButton);

        if(!fbh.userIsExcavator(unit))
        {
            addArtifact.setVisibility(View.INVISIBLE);
        }
        else
        {
            addArtifact.setVisibility(View.VISIBLE);

            //if button to add an artifact is clicked, display dialog to create a new artifact
            addArtifact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selectedImageUri != null) //if an image is displayed, allow user to create an artifact
                    {
                        if(!allArtifactBags.isEmpty()) {

                            if (!displayedImage.equals("")) {
                                //display the level map
                                imageDraw.setUri(selectedImageUri);
                                imageDraw.setCanvasBitmap();
                                displayedImage = "";
                                switcher.showNext();
                                switcher.showNext();
                            }
                            createArtifact();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Cannot create an artifact without an artifact bag. Please return to the previous page and create one.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        //the button to add a new feature to the list
        final Button addFeature = (Button) findViewById(R.id.addFeatureButton);

        if(!fbh.userIsExcavator(unit))
        {
            addFeature.setVisibility(View.INVISIBLE);
        }
        else
        {
            addFeature.setVisibility(View.VISIBLE);

            //if button to add a feature is clicked, display dialog to create a new feature
            addFeature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(selectedImageUri != null) //if an image is displayed, allow user to create a feature
                    {
                        if(!allSiteFeatures.isEmpty()) {
                            if (!displayedImage.equals("")) {
                                //display the level map
                                imageDraw.setUri(selectedImageUri);
                                imageDraw.setCanvasBitmap();
                                displayedImage = "";
                                switcher.showNext();
                                switcher.showNext();
                            }
                            createFeature(); //displays an alert
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "This site has no features, so you cannot link one to this level. Please contact your site director to add a feature.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    //called by fbh to load the artifact bags associated with this level so that they can populate the aBagChoose
    //and the user can link their new artifact to one
    public void loadArtifactBags(ArrayList<ArtifactBag> newArtifactBags)
    {
        //adding new artifact bags passed from FirebaseHandler
        for (int i = 0; i < newArtifactBags.size(); i++) {
            ArtifactBag temp = newArtifactBags.get(i);
            int index = allArtifactBags.indexOf(temp);
            if (index < 0) {
                allArtifactBags.add(temp);
            } else {
                allArtifactBags.set(index, temp);
            }
        }

        //ArrayList containing artifact bag info and id to populate listview
        artifactBagsList = new ArrayList<HashMap<String, String>>();

        //Looping through all artifact bags to add them to listview
        for(int i = 0; i < allArtifactBags.size(); i++)
        {
            ArtifactBag temp = allArtifactBags.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_ARTIFACT_BAG, temp.toString());

            // adding HashList to ArrayList
            artifactBagsList.add(map);
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating ListView
                 * */
                aBagAdapter = new SimpleAdapter(
                        LevelMap.this, artifactBagsList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_ARTIFACT_BAG},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain artifact bag's id and name
            }
        });

        //if aBagChoose exists, populate it with these bags
        if(aBagChoose != null)
        {
            aBagChoose.setAdapter(aBagAdapter);
        }

    }

    //called by fbh to notify this activity that it needs to load new artifacts
    public void addArtifacts(ArrayList<Artifact> n)
    {
        //add artifacts to list of those to load
        newArtifacts.addAll(n);

        //if newArtifacts was empty before, this means it had completed loading, so we need to tell it to load again
        if(newArtifacts.size() == n.size())
        {
            loadArtifacts();
        }
    }

    //loads artifacts, makes sure they all have images, and then populates list with them
    public void loadArtifacts()
    {
        //if there are still artifacts to load
        while (!newArtifacts.isEmpty()) {
            Artifact temp = newArtifacts.get(0); //get the first one

            int index = allArtifacts.indexOf(temp);
            if (index < 0) { //if this artifact hasn't already been loaded to the list, add it
                allArtifacts.add(temp);
            } else { //otherwise, overwrite it
                allArtifacts.set(index, temp);
            }

            //gets image from that path
            File artifactImage = new File(this.getCacheDir(), newArtifacts.get(0).getImagePath());

            if(!artifactImage.exists()) //if that file doesn't exist, it needs to. Make it.
            {
                if(fbh.userIsExcavator(level.getUnit())) {
                    //Make user highlight this artifact on the canvas
                    imageDraw.highlight();
                    imageDraw.setDrawingCacheEnabled(true);
                    drawType = "artifact";

                    TextView title = findViewById(R.id.drawAlertTitle);
                    if (keySwitcher.getNextView() == findViewById(R.id.DrawAlert)) //display alert that user must highlight
                    {
                        title.setText("Artifact " + newArtifacts.get(0).toString()); //show artifact's name so user knows which one they're highlighting
                        keySwitcher.showNext();
                    }
                    break; //break while loop so user can highlight this artifact before others load
                }
                else
                {
                    if(index < 0 || artifactsImages.size() <= index) {
                        artifactsImages.add(selectedImageUri.toString());
                    }
                    else
                    {
                        artifactsImages.set(index, selectedImageUri.toString());
                    }
                    newArtifacts.remove(0); //remove from list since this user can't do anything
                }
            }
            else //if the image does exist, this artifact is all set and is officially loaded
            {
                //loads images
                if(!artifactsImages.contains(newArtifacts.get(0).getImagePath()))//adds image paths to a list
                {
                    if(index < 0 || artifactsImages.size() <= index) {
                        artifactsImages.add(newArtifacts.get(0).getImagePath());
                    }
                    else
                    {
                        artifactsImages.set(index, newArtifacts.get(0).getImagePath());
                    }
                }

                newArtifacts.remove(0); //remove it from the list which needs to be loaded
            }
        }

        if(newArtifacts.isEmpty()) {
            //ArrayList containing artifact info and id to populate listview
            artifactsList = new ArrayList<HashMap<String, String>>();

            //Looping through all artifacts to add them to listview
            for (int i = 0; i < allArtifacts.size(); i++) {
                Artifact temp = allArtifacts.get(i);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TAG_PID, temp.getID());
                map.put(TAG_ARTIFACT, temp.toString());

                // adding HashList to ArrayList
                artifactsList.add(map);
            }

            //adding arraylist to listview
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            LevelMap.this, artifactsList,
                            R.layout.list_item, new String[]{TAG_PID,
                            TAG_ARTIFACT},
                            new int[]{R.id.pid, R.id.name}); //listview entries will contain artifact's id and name

                    // updating listview
                    artifacts.setAdapter(adapter);
                }
            });

            //once artifacts are loaded, load features
            loadFeatures();
        }
    }

    //called by fbh to notify levelmap activity that new features need to be loaded
    public void addFeatures(ArrayList<Feature> n)
    {
        //adds new features to list which needs to be loaded
        newFeatures.addAll(n);

        if(newArtifacts.isEmpty() //if there are still artifacts to load, features will be loaded after them
                && newFeatures.size() == n.size()) //But if there are no artifacts to load and features were empty, features were finished loading.
        {
            loadFeatures();
        }
    }

    //loads features into list view and loads their images, then populates list view
    public void loadFeatures()
    {
        while (!newFeatures.isEmpty()) { //while there are still features to load
            Feature temp = newFeatures.get(0); //load the first

            int index = features.indexOf(temp);
            if (index < 0) {
                features.add(temp); //if feature isn't already in list of features, add it
            } else {
                features.set(index, temp); //else, overwrite it
            }

            //get file from that path
            File featureImage = new File(this.getCacheDir(), level.getSite().getID() + "/" + level.getID() + "-" + newFeatures.get(0).getID() + ".jpg");

            if(!featureImage.exists()) //if that image doesn't exist, it needs to. Make it
            {
                if(fbh.userIsExcavator(level.getUnit())) {
                    //make user highlight this feature
                    imageDraw.highlight();
                    imageDraw.setDrawingCacheEnabled(true);
                    drawType = "feature";

                    TextView title = findViewById(R.id.drawAlertTitle);
                    if (keySwitcher.getNextView() == findViewById(R.id.DrawAlert)) {
                        title.setText(newFeatures.get(0).toString());
                        keySwitcher.showNext(); //display draw alert for this feature
                    }
                    break; //break while loop so other features don't load while this one is being highlighted
                }
                else
                {
                    if(index < 0 || featuresImages.size() <= index) {
                        featuresImages.add(selectedImageUri.toString());
                    }
                    else
                    {
                        featuresImages.set(index, selectedImageUri.toString());
                    }
                    newFeatures.remove(0); //remove the feature since this user can't add image anyway
                }
            }
            else { //if the feature's image exists, it's all set!
                //load image associated with feature
                if(!featuresImages.contains(level.getSite().getID() + "/" + level.getID() + "-" + newFeatures.get(0).getID() + ".jpg")) //if feature image path isn't already loaded, add it
                {
                    if(index < 0 || featuresImages.size() <= index) {
                        featuresImages.add(level.getSite().getID() + "/" + level.getID() + "-" + newFeatures.get(0).getID() + ".jpg");
                    }
                    else
                    {
                        featuresImages.set(index, level.getSite().getID() + "/" + level.getID() + "-" + newFeatures.get(0).getID() + ".jpg");
                    }
                }

                newFeatures.remove(0); //remove it from list to load
            }
        }

        if(newFeatures.isEmpty()) { //only populate ListView if all features have been loaded
            //ArrayList containing feature info and id to populate listview
            featuresList = new ArrayList<HashMap<String, String>>();

            //Looping through all features to add them to listview
            for (int i = 0; i < features.size(); i++) {
                Feature temp = features.get(i);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TAG_PID, temp.getID());
                map.put(TAG_FEATURE, temp.toString());

                // adding HashList to ArrayList
                featuresList.add(map);
            }

            //adding arraylist to listview
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            LevelMap.this, featuresList,
                            R.layout.list_item, new String[]{TAG_PID,
                            TAG_FEATURE},
                            new int[]{R.id.pid, R.id.name}); //listview entries will contain feature's id and name

                    // updating listview
                    featuresLV.setAdapter(adapter);
                }
            });
        }
    }

    //called by fbh to populate featureChoose with the list of features associated with the site so that the user may choose one to link to their level
    public void loadAllSiteFeatures(ArrayList<Feature> newSiteFeatures)
    {
        //adding new site features passed from FirebaseHandler
        for (int i = 0; i < newSiteFeatures.size(); i++) {
            Feature temp = newSiteFeatures.get(i);
            int index = allSiteFeatures.indexOf(temp);
            if (index < 0) {
                allSiteFeatures.add(temp);
            } else {
                allSiteFeatures.set(index, temp);
            }
        }

        //ArrayList containing site feature info and id to populate listview
        siteFeaturesList = new ArrayList<HashMap<String, String>>();

        //Looping through all site features to add them to listview
        for(int i = 0; i < allSiteFeatures.size(); i++)
        {
            Feature temp = allSiteFeatures.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_FEATURE, temp.toString());

            // adding HashList to ArrayList
            siteFeaturesList.add(map);
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                featureAdapter = new SimpleAdapter(
                        LevelMap.this, siteFeaturesList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_FEATURE},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain site feature's id and name
            }
        });

        //populates list on new feature alert so that user can link their level to a new feature
        if(featureChoose != null)
        {
            featureChoose.setAdapter(featureAdapter);
        }

    }

    //called by fbh when it gets a new feature from the site
    public void linkFeature(Feature f, Level l)
    {
        for(int i = 0; i<allSiteFeatures.size(); i++)
        {
            if(allSiteFeatures.get(i).equals(f)) //finds the feature
            {
                allSiteFeatures.get(i).addLevel(l); //adds the given level to its list of levels. Not useful now, but might be for the full site activity
                if(l.equals(level) && !features.contains(allSiteFeatures.get(i))) //if linked to the current level, add it to the list to load
                {
                    ArrayList<Feature> temp = new ArrayList<>();
                    temp.add(allSiteFeatures.get(i));
                    addFeatures(temp);
                }
            }
        }
    }

    //called when the user clicks "save" on the draw alert
    public void saveImage(View view)
    {
        //create bitmap from canvas's drawing cache
        imageDraw.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap tempBitmap = Bitmap.createBitmap(imageDraw.getDrawingCache());

        if(drawType.equals("artifact")) { //if an artifact was highlighted
            //creates file
            File tempF = new File(cache, level.getSite().getID() + "/");
            if (!tempF.exists()) {
                tempF.mkdirs();
            }
            File localFile = new File(tempF, newArtifacts.get(0).getID() + ".jpg");

            try {
                //adds bitmap to that file
                FileOutputStream fOut = new FileOutputStream(localFile);

                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
            } catch (IOException e) {
            }

            fbh.setImage(level.getSite().getID() + "/", newArtifacts.get(0).getID(), ".jpg", Uri.fromFile(localFile)); //uploads image to firebase
        }
        else
        {
            if(drawType.equals("feature")) //saves that feature image
            {
                //loads file
                File tempF = new File(cache, level.getSite().getID() + "/");
                if (!tempF.exists()) {
                    tempF.mkdirs();
                }

                File localFile = new File(tempF, level.getID() + "-" + newFeatures.get(0).getID() + ".jpg");

                try {
                    //adds bitmap to that file
                    FileOutputStream fOut = new FileOutputStream(localFile);

                    tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                    fOut.flush(); // Not really required
                    fOut.close(); // do not forget to close the stream
                } catch (IOException e) {
                }

                fbh.setImage(level.getSite().getID() + "/", level.getID() + "-" + newFeatures.get(0).getID(), ".jpg", Uri.fromFile(localFile)); //uploads file to Firebase

            }
        }
        drawType = ""; //not highlighting anything
        imageDraw.noDraw();
        imageDraw.undo(); //return to original map
        imageDraw.setDrawingCacheEnabled(false);

        if(keySwitcher.getNextView() == findViewById(R.id.artifactFeatureList)) //switch to displaying key
        {
            keySwitcher.showNext();
            if(!newArtifacts.isEmpty()) { //if there are still artifacts to load, load them
                loadArtifacts();
            }
            else {
                if(!newFeatures.isEmpty()) { //if there aren't any artifacts to load, but there are features, load the features
                    loadFeatures();
                }
            }
        }
    }

    //called when the user clicks "clear" on the draw alert
    public void clearImage(View view)
    {
        imageDraw.undo(); //displays level map again, but alert stays visible
    }

    //used by fbh and imageDraw to get the level
    public Level getLevel()
    {
        return level;
    }

    //When the user clicks the add artifact button this dialog is displayed
    public void createArtifact()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            artifactAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            artifactAlert = new AlertDialog.Builder(LevelMap.this);
        }
        artifactAlert.setTitle("Create a new artifact: ");
        name.setText("");
        artifactAlert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                aBagChoose.getSelectedItem(); //gets the selected artifact bag to link to
                Artifact a = new Artifact(unit.getSite(), unit, level, (new ArtifactBag(null, null, null, aBagID, "", -1, "")), "", name.getText().toString());
                fbh.createArtifact(a); //create artifact in firebase
            }

        });
        artifactAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        //if this view has been displayed before, remove its view so it can be added again
        if(saveArtifact.getParent() != null)
        {
            ((ViewGroup) saveArtifact.getParent()).removeView(saveArtifact);
        }
        artifactAlert.setView(saveArtifact);

        AlertDialog d = artifactAlert.create();
        d.show();
        imageDraw.setDrawingCacheEnabled(false);
    }

    //When the user clicks the add feature button this dialog is displayed
    public void createFeature()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            featureAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            featureAlert = new AlertDialog.Builder(LevelMap.this);
        }

        featureAlert.setTitle("Link a new feature: ");

        featureAlert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                featureChoose.getSelectedItem(); //gets the feature the user wishes to link to this level
                ArrayList<Level> levels = new ArrayList<>();
                levels.add(level);
                Feature temp = new Feature(featureID, "", -1, level.getSite(), levels);

                //TODO: Should only allow user to select a feature which isn't already linked
                if(!features.contains(temp)) {
                    fbh.createFeatureLink(temp, level);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "That feature has already been linked to this level", Toast.LENGTH_SHORT).show();
                }
            }

        });
        featureAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        //if this view has already been displayed, remove it and add it again
        if(saveFeature.getParent() != null)
        {
            ((ViewGroup) saveFeature.getParent()).removeView(saveFeature);
        }
        featureAlert.setView(saveFeature);

        AlertDialog d = featureAlert.create();
        d.show();
        imageDraw.setDrawingCacheEnabled(false);
    }

    //used by DrawingView to get the unit's dimensions so the image can be formatted correctly
    public static int[] getUnitDimensions()
    {
        int NS = unit.getNsDimension();
        int EW = unit.getEwDimension();

        int[] dimensions = new int[2];

        dimensions[0]=NS;
        dimensions[1]=EW;

        return dimensions;
    }

    @Override
    public void onBackPressed()
    {
        if (!displayedImage.equals("") && imageDraw != null) {
            //display the level map
            imageDraw.setUri(selectedImageUri);
            imageDraw.setCanvasBitmap();
            displayedImage = "";
            switcher.showNext();
            switcher.showNext();
        }
        super.onBackPressed();
    }
}
