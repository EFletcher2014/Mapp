package com.mycompany.sip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;


/**
 * This class displays a canvas in which the user can select artifacts/features on the image of the level selected on the "MapHome" (level information) activity
 * it also displays a list of artifacts and features which have been saved to this level
 *
 * Currently, it contains a ListFragment (MapKeyFragment) which isn't displaying a populated list. It should be updated to display the canvas in a fragment as well.
 *
 * TODO: fix artifacts list view
 * TODO: update to display canvas in a fragment as well
 * TODO: add features functionality and views
 */
public class selectActivity extends AppCompatActivity implements MapKeyFragment.OnFragmentInteractionListener {

    private static DrawingView imageDraw;  //the custom canvas which allows the user to draw on the image of their site
    public static Bitmap bitmap;
    private static ViewSwitcher switcher;  //used to prevent the user from drawing on a null image and force them to return to the previous activity to select one
    private static View context;
    private int rotation;  //the amount that the user rotated the image on the level information screen so that it displays the same way here
    private static Artifact artifact;  //null until the user selects one

    private static Level level;  //passed from MapHome

    private static MapKeyFragment mf;  //the list fragment which displays the map's key--a  list of artifacts, a list of features, and buttons to add new ones

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmap = null;
        setContentView(R.layout.activity_select);
        context = findViewById(R.id.selectActivity);

        Intent openIntent = getIntent(); //intent from MapHome TODO: may be from different activities as I add them

        final Uri selectedImageUri = openIntent.getData();  //the URI of the image selected in MapHome
        level = openIntent.getParcelableExtra("level");     //the level passed from MapHome
        rotation = openIntent.getIntExtra("rotation", 0);   //the amount the image was rotated in MapHome--used to make sure it displays the same way here

        switcher = (ViewSwitcher) findViewById(R.id.switchDrawView);

        if (switcher.getNextView() != findViewById(R.id.draw)) {  //this seems to set the view switcher to always display the
            switcher.showNext();                                  //null image warning first TODO: is this necessary?
        }


        if (selectedImageUri != null) {   //if an image was selected, add it to the draw view and switch to display it
            try {
                imageDraw = (DrawingView) findViewById(R.id.draw);
                Bitmap bm = MediaStore.Images.Media.getBitmap(this.getApplicationContext().getContentResolver(), selectedImageUri);
                bitmap = rotateBitmap(bm, rotation);
                imageDraw.setCanvasBitmap(bitmap);
            } catch (IOException e) {
                System.err.println("Error: image URI is null"); //TODO: handle this another way
            }
            switcher.showNext();
        }

        mf = new MapKeyFragment(); //creates a MapKeyFragment

        LayoutInflater inflater = (LayoutInflater) imageDraw.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View m = inflater.inflate(R.layout.fragment_map, null); //creates canvas

        final ListView a = (ListView) m.findViewById(android.R.id.list); //references artifacts ListView in MapKeyFragment
        ListAdapter  adapter = mf.refreshArtifactsLV(null, imageDraw.getContext(), a); //attempts to refresh listview to display all created artifacts
                                                                    //but doesn't work. Passes null because no new artifacts have been created yet
        a.setAdapter(adapter);
        System.out.println("adapter " + a.getAdapter());

        a.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                     @Override
                                     public void onItemClick(AdapterView<?> parent, View view,
                                                             int position, long id) {
                                        System.out.println("clicked clicked clicked");
                                         //TODO: display the user's drawing on the canvas when that artifact is selected

                                     }
                                 });

        Button addArtifact = (Button) findViewById(R.id.addArtifactButton); //button which user clicks to draw on canvas

        addArtifact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(selectedImageUri!=null) //if an image is selected, user can click button to draw on canvas. Clicking again cancels
                    {
                        if(imageDraw.getTool().equals("highlight")) //allows user to draw on canvas
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


    @Override
    public void onFragmentInteraction(Uri uri) { //necessary to implement fragment TODO: should probably do something

    }

    /**
     * Called when the user completes a drawing (stops touching the screen). Creates a dialog prompting the user to save their selection
     */
    public static void saveLayer()
    {
        //TODO: make different titles/functionality for features/artifacts

        AlertDialog.Builder alert;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(imageDraw.getContext(), R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(imageDraw.getContext());
        }

        alert.setTitle("Would you like to save your artifact?"); //TODO: change for feature implementation

        final EditText name = new EditText(imageDraw.getContext()); //allows user to input a name for their artifact/feature


        //TODO: is this necessary? the view was already created
        LayoutInflater inflater = (LayoutInflater) imageDraw.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View m = inflater.inflate(R.layout.fragment_map, null);
        final ListView a = (ListView) m.findViewById(android.R.id.list);
        name.setHint("Name your selection");
        alert.setView(name);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.setDrawingCacheEnabled(true);
                imageDraw.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                bitmap = Bitmap.createBitmap(imageDraw.getDrawingCache());
                System.out.println("Yes!\nDrawing cache Bitmap: " + bitmap);
                imageDraw.save(bitmap);

                artifact = new Artifact(level.getSite(), level.getUnit(), level, new ArtifactBag(level.getSite(), level.getUnit(), level, "", -1, -1, new Timestamp(0)), -1, -1, name.getText().toString(), bitmap, new Timestamp(0), new Timestamp(0));
                mf.refreshArtifactsLV(artifact, imageDraw.getContext(), a);

            }

        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.undo();
            }
        });

        AlertDialog d = alert.create();
        d.show();
        imageDraw.noDraw();  //takes canvas out of draw mode
        imageDraw.setDrawingCacheEnabled(false);
    }

    //used by DrawingView to get unit dimensions. Was used in a previous functionality that wasn't useful, but will likely be useful
    // in the full site map activity again.
    public static String[] getUnitDimensions()
    {
        String NS = level.getUnit().getNsDimension();
        String EW = level.getUnit().getEwDimension();

        String[] dimensions = new String[2];

        dimensions[0]=NS;
        dimensions[1]=EW;

        return dimensions;
    }

    /**
     * From https://stackoverflow.com/questions/31781150/auto-image-rotated-from-portrait-to-landscape
     * rotates bitmap so it displays correctly, as user rotated it on MapHome activity
     */

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        matrix.setRotate(orientation);

        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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
        //save to file TODO: I think I just want to save this to the server, so this may not be necessary anymore
        String filename = level.getUnit().getDatum();
        File file = new File(context.getContext().getFilesDir(), filename);

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
        Intent intent = new Intent();
        intent.putExtra("newURI", newURI);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
