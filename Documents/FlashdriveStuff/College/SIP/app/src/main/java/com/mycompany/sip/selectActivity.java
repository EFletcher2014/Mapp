package com.mycompany.sip;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;

public class selectActivity extends AppCompatActivity {

    private static boolean fabMenuDeployed=false;
    private static DrawingView imageDraw;
    public Bitmap bitmap;
    private static ViewSwitcher switcher;
    private TextView hi;
    private TextView gr;
    private TextView ks;
    private static View context;

    private static Unit unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        fabMenuDeployed=false;
        context = findViewById(R.id.selectActivity);
        Intent openIntent = getIntent();
        final Uri selectedImageUri = openIntent.getData();
        unit = openIntent.getParcelableExtra("unit");

        switcher = (ViewSwitcher) findViewById(R.id.switchDrawView);


        if(selectedImageUri!=null)
        {
            System.out.println("not null");
            try {
                imageDraw = (DrawingView) findViewById(R.id.draw);
                System.out.println("Adding bitmap");
                bitmap = MediaStore.Images.Media.getBitmap(this.getApplicationContext().getContentResolver(), selectedImageUri);
                imageDraw.setCanvasBitmap(bitmap);
                System.out.println("set Canvas Bitmap");
            } catch (IOException e) {
                System.err.println("Error: image URI is null");
            }
            switcher.showNext();
        }
        System.out.println(selectedImageUri);
        //ImageView selectImageView = (ImageView) findViewById(R.id.select_image_view);

        /*if (selectedImageUri == null) {
            //Should probably switch later to not be a toast
            CharSequence toastMessage = "Please return to previous screen to select an image of your unit";
            Toast toast = Toast.makeText(selectImageView.getContext(), toastMessage, Toast.LENGTH_LONG);
            toast.show();
        }
        selectImageView.setImageURI(selectedImageUri);*/

        final LinearLayout FABMenu = (LinearLayout) findViewById(R.id.fabMenu);
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
        });

        hi  = (TextView) findViewById(R.id.highlightLabel);
        FloatingActionButton highlight = (FloatingActionButton) findViewById(R.id.selectfab);
        highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fabMenuDeployed)
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
                    }
                    //TODO: change button color/animate label to describe what this FAB does
                    if(selectedImageUri!=null)
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
            }
        });
        gr = (TextView) findViewById(R.id.gridLabel);
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
                    //TODO: change button color/animate label to describe what this FAB does
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
                    //TODO: change button color/animate label to describe what this FAB does
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
        });
    }
    public static void saveLayer()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(imageDraw.getContext());
        alert.setTitle("Would you like to save your artifact?");
        EditText name = new EditText(imageDraw.getContext());
        name.setHint("Name your selection");
        alert.setView(name);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.save();
                //TODO: get info about artifact and save to server
            }

        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.undo();
            }
        });
        AlertDialog d = alert.create();
        d.show();
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

    public static void saveGrid()
    {
        AlertDialog.Builder alert1 = new AlertDialog.Builder(imageDraw.getContext());
        alert1.setTitle("Would you like to save your grid?");
        alert1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                imageDraw.saveGrid();
                //TODO: get info about artifact and save to server
            }

        });
        alert1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //imageDraw.undo();
            }
        });
        AlertDialog d = alert1.create();
        d.show();
    }
}
