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
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;

public class selectActivity extends AppCompatActivity {

    private static boolean fabMenuDeployed=false;
    private static DrawingView imageDraw;
    public Bitmap bitmap;
    private static ViewSwitcher switcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Intent openIntent = getIntent();
        Uri selectedImageUri = openIntent.getData();
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
                    //FABMenu.setVisibility(View.GONE);
                }
            }
        });

        FloatingActionButton highlight = (FloatingActionButton) findViewById(R.id.selectfab);
        highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fabMenuDeployed)
                {

                }
            }
        });
    }

    public void onClickArtifact(View view)
    {
        LayoutInflater inflater = getLayoutInflater();
        View artifactLayout = inflater.inflate(R.layout.new_artifact_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Create New Artifact");
        // this is set the view from XML inside AlertDialog
        alert.setView(artifactLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

}
