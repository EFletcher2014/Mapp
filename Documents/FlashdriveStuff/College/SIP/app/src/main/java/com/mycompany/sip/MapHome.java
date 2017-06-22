package com.mycompany.sip;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MapHome extends AppCompatActivity {
    private static int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private String filemanagerstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_home);

        final ImageView unitImage = (ImageView) findViewById(R.id.unitImgView);
        unitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get Picture
                Intent pictureIntent = new Intent();
                pictureIntent.setType("image/*");
                pictureIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(pictureIntent, "Select an aerial view of your unit"), SELECT_PICTURE);
            }
        });
    }
    /**
     * FROM STACKOVERFLOW https://stackoverflow.com/questions/2169649/get-pick-an-image-from-androids-built-in-gallery-app-programmatically*****
     */
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                System.out.println("selectedImageUri is" + selectedImageUri);

                //OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();

                //MEDIA GALLERY
                ImageView unitImage = (ImageView) findViewById(R.id.unitImgView);
                unitImage.setImageURI(selectedImageUri);
            }
        }
    }
}
