package com.mycompany.sip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class selectActivity extends AppCompatActivity {

    private static boolean fabMenuDeployed=false;
    private static DrawingView imageDraw;
    public static Bitmap bitmap;
    private static ViewSwitcher switcher;
    private TextView hi;
    private TextView gr;
    private TextView ks;
    private static View context;
    private int rotation;

    private static Unit unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("start");
        bitmap=null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        fabMenuDeployed=false;
        context = findViewById(R.id.selectActivity);
        Intent openIntent = getIntent();
        final Uri selectedImageUri = openIntent.getData();
        unit = openIntent.getParcelableExtra("unit");
        rotation = openIntent.getIntExtra("rotation", 0);

        switcher = (ViewSwitcher) findViewById(R.id.switchDrawView);
        if(switcher.getNextView()!=findViewById(R.id.draw))
        {
            switcher.showNext();
        }


        if(selectedImageUri!=null)
        {
            System.out.println("not null");
            try {
                imageDraw = (DrawingView) findViewById(R.id.draw);
                System.out.println("Adding bitmap");
                Bitmap bm = MediaStore.Images.Media.getBitmap(this.getApplicationContext().getContentResolver(), selectedImageUri);
                bitmap = rotateBitmap(bm, rotation);
                imageDraw.setCanvasBitmap(bitmap);
                System.out.println("set Canvas Bitmap");
            } catch (IOException e) {
                System.err.println("Error: image URI is null");
            }
            switcher.showNext();
        }
        System.out.println("SURI: " + selectedImageUri);

        //Added 8/1/2018

        final ListView artifacts = (ListView) findViewById(R.id.artifactListMap);
        final Button addArtifact = (Button) findViewById(R.id.addArtifactButton);

        //ImageView selectImageView = (ImageView) findViewById(R.id.select_image_view);

        /*if (selectedImageUri == null) {
            //Should probably switch later to not be a toast
            CharSequence toastMessage = "Please return to previous screen to select an image of your unit";
            Toast toast = Toast.makeText(selectImageView.getContext(), toastMessage, Toast.LENGTH_LONG);
            toast.show();
        }
        selectImageView.setImageURI(selectedImageUri);*/

        /*final LinearLayout FABMenu = (LinearLayout) findViewById(R.id.fabMenu);
        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.editfab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fabMenuDeployed) {
                    FABMenu.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_menu);
                    FABMenu.startAnimation(anim);
                    fabMenuDeployed = true;
                } else {
                    fabMenuDeployed = false;
                    Animation hide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_menu);
                    FABMenu.startAnimation(hide);
                    imageDraw.noDraw();
                    //FABMenu.setVisibility(View.GONE);
                }
            }
        });*/

        /*hi  = (TextView) findViewById(R.id.highlightLabel);
        FloatingActionButton highlight = (FloatingActionButton) findViewById(R.id.selectfab);*/
        addArtifact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(fabMenuDeployed)
                {
                    if(hi.getVisibility()==View.VISIBLE)
                    {
                        hi.setVisibility(View.GONE);
                    }
                    else
                    {
                        hi.setVisibility(View.VISIBLE);
                        gr.setVisibility(View.GONE);
                        ks.setVisibility(View.GONE);
                    }*/

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
                //}
            }
        });
        /*gr = (TextView) findViewById(R.id.gridLabel);
        FloatingActionButton grid = (FloatingActionButton) findViewById(R.id.gridfab);
        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fabMenuDeployed)
                {
                    if(gr.getVisibility()==View.VISIBLE)
                    {
                        gr.setVisibility(View.GONE);
                    }
                    else
                    {

                        gr.setVisibility(View.VISIBLE);
                        hi.setVisibility(View.GONE);
                        ks.setVisibility(View.GONE);
                    }

                    if(selectedImageUri!=null)
                    {
                        if(imageDraw.getTool().equals("grid"))
                        {
                            imageDraw.noDraw();
                        }
                        else {
                            imageDraw.grid();
                        }
                    }
                }
            }
        });

        ks = (TextView) findViewById(R.id.keystoneLabel);
        final FloatingActionButton keystone = (FloatingActionButton) findViewById(R.id.keystonefab);
        keystone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fabMenuDeployed)
                {
                    if(ks.getVisibility()==View.VISIBLE)
                    {
                        ks.setVisibility(View.GONE);
                    }
                    else
                    {

                        ks.setVisibility(View.VISIBLE);
                        hi.setVisibility(View.GONE);
                        gr.setVisibility(View.GONE);
                    }

                    if(selectedImageUri!=null)
                    {
                        if(imageDraw.getTool().equals("keystone"))
                        {
                            imageDraw.noDraw();
                        }
                        else {
                            imageDraw.keystone();

                        }
                    }
                }
            }
        });*/
    }

    public static void saveLayer()
    {
        //TODO: make different titles/functionality for features/artifacts etc
        System.out.println("Saving!");
        AlertDialog.Builder alert = new AlertDialog.Builder(imageDraw.getContext());
        alert.setTitle("Would you like to save your artifact?");
        EditText name = new EditText(imageDraw.getContext());
        name.setHint("Name your selection");
        alert.setView(name);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.setDrawingCacheEnabled(true);
                imageDraw.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                bitmap = Bitmap.createBitmap(imageDraw.getDrawingCache());
                System.out.println("Yes!\nDrawing cache Bitmap: " + bitmap);
                imageDraw.save(bitmap);
                //TODO: get info about artifact and save to server
            }

        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("No! :(");
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

    public static String[] getUnitDimensions()
    {
        String NS = unit.getNsDimension();
        String EW = unit.getEwDimension();

        String[] dimensions = new String[2];

        dimensions[0]=NS;
        dimensions[1]=EW;

        return dimensions;
    }
    //From https://stackoverflow.com/questions/31781150/auto-image-rotated-from-portrait-to-landscape
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
        System.out.println("going back!");
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
