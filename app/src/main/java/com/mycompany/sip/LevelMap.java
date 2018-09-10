package com.mycompany.sip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
    private static View context;
    private int rotation;
    private AlertDialog.Builder alert;
    private ArrayList<Artifact> allArtifacts = new ArrayList<>();
    private ArrayList<ArtifactBag> allArtifactBags = new ArrayList<>();
    private ArrayList<Feature> allSiteFeatures = new ArrayList<>();
    private ArrayList<Feature> features = new ArrayList<>();
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
    private ArrayList<File> artifactsImages = new ArrayList<>();
    private ArrayList<File> featuresImages = new ArrayList<>();
    private File displayedImage = null;
    private String drawType = "";

    private static Unit unit;
    private Level level;

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
        fbh.updateLevelMapActivity(this);
        imageDraw.updateLevelMapActivity(this);
        setContentView(R.layout.activity_select);
        context = findViewById(R.id.selectActivity);
        Intent openIntent = getIntent();
        selectedImageUri = openIntent.getData();
        level = openIntent.getParcelableExtra("level");
        unit = level.getUnit();
        rotation = openIntent.getIntExtra("rotation", 0);
        cache = this.getCacheDir();

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
            try {
                imageDraw = (DrawingView) findViewById(R.id.draw);  //the canvas to draw on
                Bitmap bm = MediaStore.Images.Media.getBitmap(this.getApplicationContext().getContentResolver(), selectedImageUri);
                bitmap = rotateBitmap(bm, rotation);
                imageDraw.setCanvasBitmap(bitmap);


                LayoutInflater inflater = getLayoutInflater();
                saveArtifact = inflater.inflate(R.layout.new_artifact_dialog, null);
                saveFeature = inflater.inflate(R.layout.new_feature_dialog, null);

                aBagChoose = (Spinner) saveArtifact.findViewById(R.id.artifactBagSelect);
                aBagChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Object item = parent.getItemAtPosition(pos);
                        aBagID = ((HashMap<String, String>) item).get(TAG_PID);
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                name = (EditText) saveArtifact.findViewById(R.id.artifactName);

                featureChoose = (Spinner) saveFeature.findViewById(R.id.featureSelect);
                featureChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        Object item = parent.getItemAtPosition(pos);
                        featureID = ((HashMap<String, String>) item).get(TAG_PID);
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            } catch (IOException e) {
                System.err.println("Error: image URI is null");
            }
            switcher.showNext();
        }


        //TODO: make this align right
        artifacts = (ListView) findViewById(R.id.artifactListMap);
        artifacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(displayedImage == null || displayedImage != artifactsImages.get((int) id)) {
                    try {
                        imageDraw.setCanvasBitmap(MediaStore.Images.Media.getBitmap(artifacts.getContext().getContentResolver(), Uri.fromFile(artifactsImages.get((int) id))));
                        displayedImage = artifactsImages.get((int) id);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                }
                else
                {
                    if(displayedImage == artifactsImages.get((int) id)) {
                        try {
                            Bitmap bm = MediaStore.Images.Media.getBitmap(artifacts.getContext().getContentResolver(), selectedImageUri);
                            bitmap = rotateBitmap(bm, rotation);
                            imageDraw.setCanvasBitmap(bitmap);
                            displayedImage = null;
                        } catch(IOException e)
                        {
                            System.out.println(e);
                        }
                    }
                }
                switcher.showNext();
                switcher.showNext();
            }
        });

        featuresLV = (ListView) findViewById(R.id.featureListMap);
        featuresLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(displayedImage == null || displayedImage != featuresImages.get((int) id)) {
                    try {
                        imageDraw.setCanvasBitmap(MediaStore.Images.Media.getBitmap(featuresLV.getContext().getContentResolver(), Uri.fromFile(featuresImages.get((int) id))));
                        displayedImage = featuresImages.get((int) id);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                }
                else
                {
                    if(displayedImage == featuresImages.get((int) id)) {
                        try {
                            Bitmap bm = MediaStore.Images.Media.getBitmap(artifacts.getContext().getContentResolver(), selectedImageUri);
                            bitmap = rotateBitmap(bm, rotation);
                            imageDraw.setCanvasBitmap(bitmap);
                            displayedImage = null;
                        } catch(IOException e)
                        {
                            System.out.println(e);
                        }
                    }
                }
                switcher.showNext();
                switcher.showNext();
            }
        });


        //load all artifacts
        fbh.getArtifactsFromLevel(level);
        fbh.getArtifactBagsFromLevel(level);
        fbh.getFeaturesFromLevel(level);
        fbh.getFeaturesFromSite();

        final Button addArtifact = (Button) findViewById(R.id.addArtifactButton);

        //if button to add an artifact is clicked, allow user to draw on the image
        addArtifact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(selectedImageUri!=null)//TODO: make this turn off automatically
            {
                createArtifact();
            }
            }
        });

        final Button addFeature = (Button) findViewById(R.id.addFeatureButton);

        //if button to add a feature is clicked, allow user to draw on the image
        addFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(selectedImageUri!=null)//TODO: make this turn off automatically
                {
                    createFeature();
                }
            }
        });
    }

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
                 * Updating parsed JSON data into ListView
                 * */
                aBagAdapter = new SimpleAdapter(
                        LevelMap.this, artifactBagsList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_ARTIFACT_BAG},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain artifact bag's id and name
            }
        });

        if(aBagChoose != null)
        {
            aBagChoose.setAdapter(aBagAdapter);
        }

    }

    public void loadArtifacts(ArrayList<Artifact> newArtifacts)
    {
        //adding new artifacts passed from FirebaseHandler
        for (int i = 0; i < newArtifacts.size(); i++) {
            Artifact temp = newArtifacts.get(i);
            int index = allArtifacts.indexOf(temp);
            if (index < 0) {
                allArtifacts.add(temp);
            } else {
                allArtifacts.set(index, temp);
            }
        }

        //ArrayList containing artifact info and id to populate listview
        artifactsList = new ArrayList<HashMap<String, String>>();

        //Looping through all artifacts to add them to listview
        for(int i = 0; i < allArtifacts.size(); i++)
        {
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
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        LevelMap.this, artifactsList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_ARTIFACT},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain artifact's id and name

                // updating listview
                artifacts.setAdapter(adapter);
            }
        });
    }

    public void loadFeatures(ArrayList<Feature> newFeatures)
    {
        //adding new features passed from FirebaseHandler
        for (int i = 0; i < newFeatures.size(); i++) {
            Feature temp = newFeatures.get(i);
            int index = features.indexOf(temp);
            if (index < 0) {
                features.add(temp);
            } else {
                features.set(index, temp);
            }
        }

        //ArrayList containing feature info and id to populate listview
        featuresList = new ArrayList<HashMap<String, String>>();

        //Looping through all features to add them to listview
        for(int i = 0; i < features.size(); i++)
        {
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
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        LevelMap.this, featuresList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_FEATURE},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain feature's id and name

                // updating listview
                featuresLV.setAdapter(adapter);
            }
        });
    }

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

        if(featureChoose != null)
        {
            featureChoose.setAdapter(featureAdapter);
        }

    }

    public void linkFeature(Feature f, Level l)
    {
        for(int i = 0; i<allSiteFeatures.size(); i++)
        {
            if(allSiteFeatures.get(i).equals(f))
            {
                allSiteFeatures.get(i).addLevel(l);
                if(l.equals(level) && !features.contains(allSiteFeatures.get(i)))
                {
                    ArrayList<Feature> temp = new ArrayList<>();
                    temp.add(allSiteFeatures.get(i));
                    loadFeatures(temp);
                }
            }
        }
    }

    public void loadArtifactImage(File newImage)
    {
        if(!artifactsImages.contains(newImage)) {
            artifactsImages.add(newImage);
        }
    }

    public void loadFeatureImage(File newImage)
    {
        if(!featuresImages.contains(newImage))
        {
            featuresImages.add(newImage);
        }
    }

    public void saveImage()
    {
        imageDraw.setDrawingCacheEnabled(true);
        imageDraw.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap tempBitmap = Bitmap.createBitmap(imageDraw.getDrawingCache());

        if(drawType.equals("artifact")) {
            //TODO: will also need to figure out how this will work when offline
            File tempF = new File(cache, level.getSite().getNumber() + "/" + level.getUnit().getDatum() + "/level" + level.getNumber());
            if (!tempF.exists()) {
                tempF.mkdirs();
            }
            File localFile = new File(tempF, allArtifacts.get(allArtifacts.size() - 1).getID() + ".jpg");

            try {
                localFile = File.createTempFile(allArtifacts.get(allArtifacts.size() - 1).getID(), ".jpg", tempF);


                FileOutputStream fOut = new FileOutputStream(localFile);

                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
            } catch (IOException e) {
                System.out.println(e);
            }

            artifactsImages.add(localFile);
            fbh.setImage(level.getSite().getNumber() + "/" + level.getUnit().getDatum() + "/level" + level.getNumber() + "/", allArtifacts.get(allArtifacts.size() - 1).getID(), ".jpg", Uri.fromFile(localFile));
        }
        else
        {
            if(drawType.equals("feature"))
            {
                //TODO: will also need to figure out how this will work when offline
                File tempF = new File(cache, level.getSite().getID() + "/");
                if (!tempF.exists()) {
                    tempF.mkdirs();
                }

                //TODO: do we need this local file thing twice
                File localFile = new File(tempF, level.getID() + "-" + features.get(features.size() - 1).getID() + ".jpg");

                try {
                    localFile = File.createTempFile(level.getID() + "-" + features.get(features.size() - 1).getID(), ".jpg", tempF);


                    FileOutputStream fOut = new FileOutputStream(localFile);

                    tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                    fOut.flush(); // Not really required
                    fOut.close(); // do not forget to close the stream
                } catch (IOException e) {
                    System.out.println(e);
                }

                featuresImages.add(localFile);
                fbh.setImage(level.getSite().getNumber() + "/", level.getID() + "-" + features.get(features.size() -1).getID(), ".jpg", Uri.fromFile(localFile));

            }
        }
        drawType = "";
        imageDraw.undo();
    }

    public Level getLevel()
    {
        return level;
    }

    //When the user clicks the add artifact button this dialog is displayed
    public void createArtifact()
    {
        //TODO: make different titles/functionality for features/artifacts etc
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(LevelMap.this);
        }

        alert.setTitle("Create a new artifact: ");
        alert.setView(saveArtifact);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                aBagChoose.getSelectedItem();
                Artifact a = new Artifact(unit.getSite(), unit, level, (new ArtifactBag(null, null, null, aBagID, "", -1, "")), "", name.getText().toString());
                fbh.createArtifact(a);

                //TODO: make it clear that the user must now highlight
                if(imageDraw.getTool().equals("highlight"))
                {
                    imageDraw.noDraw();
                }
                else {
                    imageDraw.highlight();
                    drawType = "artifact";
                }

            }

        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog d = alert.create();
        d.show();
        imageDraw.setDrawingCacheEnabled(false);
    }

    //When the user clicks the add feature button this dialog is displayed
    public void createFeature()
    {
        //TODO: make different titles/functionality for features/artifacts etc
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(LevelMap.this);
        }

        alert.setTitle("Link a new feature: ");
        alert.setView(saveFeature);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                featureChoose.getSelectedItem();
                ArrayList<Level> levels = new ArrayList<>();
                levels.add(level);
                Feature temp = new Feature(featureID, "", -1, level.getSite(), levels);

                //TODO: Should only allow user to select a feature which isn't already linked
                if(!features.contains(temp)) {
                    fbh.createFeatureLink(temp, level);
                }


                //TODO: make it clear that the user must now highlight
                if(imageDraw.getTool().equals("highlight"))
                {
                    imageDraw.noDraw();
                }
                else {
                    imageDraw.highlight();
                    drawType = "feature";
                }

            }

        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog d = alert.create();
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
    //From https://stackoverflow.com/questions/31781150/auto-image-rotated-from-portrait-to-landscape
    //ensures that the image is rotated as it was on the MapHome activity
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        matrix.setRotate(orientation);

        try {
            System.out.println("rotating!");
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed()
    {
        /*Intent intent = new Intent();
        intent.putExtra("bitmap", bitmap);
        setResult(33, intent);
        finish();*/

        //save to file
        String filename = unit.getDatum();
        File file = new File(context.getContext().getFilesDir(), filename);

        /*if(file.exists())
        {
            file.delete();
        }*/
        try
        {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri newURI = Uri.fromFile(file);
        System.out.println(newURI);
        Intent intent = new Intent();
        intent.putExtra("newURI", newURI);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    //TODO: save layers/descriptions to server
    //TODO: figure out what to do with grids
}
