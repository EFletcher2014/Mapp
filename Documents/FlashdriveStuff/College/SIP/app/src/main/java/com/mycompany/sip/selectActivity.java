package com.mycompany.sip;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class selectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Intent openIntent = getIntent();
        Uri selectedImageUri = openIntent.getData();
        System.out.println(selectedImageUri);
        ImageView selectImageView = (ImageView) findViewById(R.id.select_image_view);
        if (selectedImageUri==null)
        {
            //Should probably switch later to not be a toast
            CharSequence toastMessage = "Please return to previous screen to select an image of your unit";
            Toast toast = Toast.makeText(selectImageView.getContext(), toastMessage, Toast.LENGTH_LONG);
            toast.show();
        }
        selectImageView.setImageURI(selectedImageUri);
    }

}
