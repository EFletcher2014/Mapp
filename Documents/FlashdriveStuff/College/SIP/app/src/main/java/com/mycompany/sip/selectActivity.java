package com.mycompany.sip;

import android.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Intent openIntent = getIntent();
        final Uri selectedImageUri = openIntent.getData();
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

        final FloatingActionButton undo = (FloatingActionButton) findViewById(R.id.undofab);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fabMenuDeployed)
                {
                    //TODO: change button color/animate label to describe what this FAB does
                    if(selectedImageUri!=null)
                    {
                        imageDraw.undo();
                    }
                }
            }
        });
    }

}
