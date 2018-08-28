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
    ArrayList<HashMap<String, String>> artifactsList;
    ArrayList<HashMap<String, String>> artifactBagsList;
    private ListView artifacts;
    private SpinnerAdapter aBagAdapter;
    private Spinner aBagChoose;
    private EditText name;
    private String aBagID;
    private View saveArtifact;

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
        final Uri selectedImageUri = openIntent.getData();
        level = openIntent.getParcelableExtra("level");
        unit = level.getUnit();
        rotation = openIntent.getIntExtra("rotation", 0);

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
            } catch (IOException e) {
                System.err.println("Error: image URI is null");
            }
            switcher.showNext();
        }


        artifacts = (ListView) findViewById(R.id.artifactListMap);

        //load all artifacts
        fbh.getArtifactsFromLevel(level);
        fbh.getArtifactBagsFromLevel(level);

        final Button addArtifact = (Button) findViewById(R.id.addArtifactButton);

        //if button to add an artifact is clicked, allow user to draw on the image
        addArtifact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(selectedImageUri!=null)//TODO: make this turn off automatically
            {
                if(imageDraw.getTool().equals("highlight"))
                {
                    imageDraw.noDraw();
                }
                else {
                    imageDraw.highlight();
                }
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

    public Level getLevel()
    {
        return level;
    }

    //When the user is done drawing (removes their finger from the screen), this dialog is displayed
    public void saveLayer()
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

        alert.setTitle("Would you like to save your artifact?");
        alert.setView(saveArtifact);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.setDrawingCacheEnabled(true);
                imageDraw.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                bitmap = Bitmap.createBitmap(imageDraw.getDrawingCache());
                imageDraw.save(bitmap);
                aBagChoose.getSelectedItem();

                //TODO: get info about artifact and save to server
                Artifact a = new Artifact(unit.getSite(), unit, level, (new ArtifactBag(null, null, null, aBagID, "", -1, "")), "", name.getText().toString());
                fbh.createArtifact(a);
            }

        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /*imageDraw.setDrawingCacheEnabled(true);
                imageDraw.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                imageDraw.destroyDrawingCache();*/
                imageDraw.undo();
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
