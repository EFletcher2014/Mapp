package com.mycompany.sip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LevelDocument extends AppCompatActivity {

    public static boolean isActive;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

    private static int SELECT_PICTURE = 1;
  
    private Uri selectedImageUri = null;
    private String siteName;
    private String siteNumber;
    private int pk = -1, fk = -1, lvlNum = -1;
    private String unitNumber = "";
    private String levelNumber;
    private Site site;
    private Unit unit;
    private Level level;
    private EditText begDepth;
    private EditText endDepth;
    private EditText excMeth, notes;
    private ImageView unitImage;
    private AlertDialog.Builder alert, alert1;
    private ViewSwitcher switcher;
    private static final int CAMERA_REQUEST = 1888;
    private int rotation = 0;
    private File localFile;

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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.isActive = true;
        fbh.updateLevelDocActivity(this);

        setContentView(R.layout.activity_level_document);
        switcher= (ViewSwitcher) findViewById(R.id.imageSwitch);
        begDepth = (EditText) findViewById(R.id.enterBegDepth);
        endDepth = (EditText) findViewById(R.id.enterEndDepth);
        excMeth = (EditText) findViewById(R.id.techniques);
        notes = (EditText) findViewById(R.id.level_notes);
        unitImage = (ImageView) findViewById(R.id.unitImgView);

        if(savedInstanceState!=null)
        {
            selectedImageUri=savedInstanceState.getParcelable("URI");
            if(selectedImageUri!=null)
            {
                switcher.showNext();
                unitImage.setImageURI(selectedImageUri);
                Bitmap bm=((BitmapDrawable)unitImage.getDrawable()).getBitmap();
                rotation=savedInstanceState.getInt("rotation");
                Bitmap bitmap = rotateBitmap(bm, rotation);
                unitImage.setImageBitmap(bitmap);
            }

            if(savedInstanceState.getBoolean("alert"))
            {
                showCancelDialog();
            }
            if(savedInstanceState.getBoolean("alert1"))
            {
                showChooserDialog();
            }
        }

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        level = openIntent.getParcelableExtra("depth");

        if(level!=null) {
            lvlNum = level.getNumber();
            site = level.getSite();
            siteName = site.getName();
            siteNumber = site.getNumber();
            unit = level.getUnit();
            unitNumber = unit.getDatum();
            levelNumber = level.getNumber() + "";
            if(level.getBegDepth()!=-1)
            {
                begDepth.setText(level.getBegDepth() + "");
            }
            if(level.getEndDepth()!=-1)
            {
                endDepth.setText(level.getEndDepth() + "");
            }
            excMeth.setText(level.getExcavationMethod() + "");
            notes.setText(level.getNotes() + "");

            fbh.getImage(site.getID() + "/" , level.getID() + "map", this.getCacheDir(), "");
        }
        TextView siteNameText = (TextView) findViewById(R.id.SiteNameLevel);
        siteNameText.setText("Site: " + siteName);
        TextView siteNumberText = (TextView) findViewById(R.id.siteNumLevel);
        siteNumberText.setText("Site Number: " + siteNumber);
        TextView unitNumberText = (TextView) findViewById(R.id.UnitNumberLevel);
        unitNumberText.setText("Unit: " + unitNumber);
        TextView levelNumberText = (TextView) findViewById(R.id.levelNumber);
        levelNumberText.setText("Level " + levelNumber);


        if(fbh.userIsExcavator(unit)) {
            switcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                if (selectedImageUri == null) {
                    showChooserDialog();
                }
                }
            });
        }
        else
        {
            TextView noImage = findViewById(R.id.selectImage);
            noImage.setText("No image selected");
            begDepth.setEnabled(false);
            endDepth.setEnabled(false);
            excMeth.setEnabled(false);
            notes.setEnabled(false);
        }

        final FloatingActionButton rotate = (FloatingActionButton) findViewById(R.id.rotateFab);

        if(selectedImageUri != null || !fbh.userIsExcavator(unit))
        {
            rotate.setVisibility(View.INVISIBLE);
        }
        else {
            rotate.setVisibility(View.VISIBLE);

            rotate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Bitmap bm = ((BitmapDrawable) unitImage.getDrawable()).getBitmap();
                Bitmap bmRotated = rotateBitmap(bm, 90);
                unitImage.setImageBitmap(bmRotated);
                rotation += 90;
                rotation %= 360;
                }
            });
        }

        final Button toAddArtifactActivity = (Button) findViewById(R.id.toAddArtifactsActivity);
        toAddArtifactActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(level!=null) { //TODO: this isn't working
                //Move to select on image activity
                Intent artifactActivityIntent = new Intent(view.getContext(), AllArtifactBagsActivity.class);
                artifactActivityIntent.putExtra("name", site);
                artifactActivityIntent.putExtra("datum", unit);
                artifactActivityIntent.putExtra("depth", level);
                artifactActivityIntent.putExtra("PrimaryKey", pk);
                startActivity(artifactActivityIntent);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "You must save your level before creating artifacts", Toast.LENGTH_SHORT).show();
            }
            }
        });

        final Button toSelectOnImageActivity = (Button) findViewById(R.id.select_on_image_button);
        toSelectOnImageActivity.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
            if(level!=null) {
                if(selectedImageUri!=null) {

                    //check if image has been saved
                    File levelMap = new File(toSelectOnImageActivity.getContext().getCacheDir(),  site.getID() + "/" + level.getID() + "map.jpg");

                    if(levelMap.exists()) {
                        //Move to select on image activity
                        Intent selectActivityIntent = new Intent(Intent.ACTION_ATTACH_DATA, selectedImageUri, view.getContext(), LevelMap.class);
                        selectActivityIntent.putExtra("level", level);
                        selectActivityIntent.putExtra("rotation", rotation);
                        startActivity(selectActivityIntent);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "You must save your level before using this feature", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "You must add a picture of your unit before using this feature", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "You must save your level before mapping", Toast.LENGTH_SHORT).show();
            }
              }
        });

        //Button to save the level
        final Button saveButton = (Button) findViewById(R.id.mainsave);

        if(!fbh.userIsExcavator(unit))
        {
            saveButton.setVisibility(View.INVISIBLE);
        }
        else {
            saveButton.setVisibility(View.VISIBLE);


            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                Double bd = Double.parseDouble(begDepth.getText().toString());
                Double ed = Double.parseDouble(endDepth.getText().toString());

                //TODO: add date started input or remove
                //String date = dateTime.getText().toString();
                String em = excMeth.getText().toString();
                String n = notes.getText().toString();

                level.setBegDepth(bd);
                level.setEndDepth(ed);
                level.setExcavationMethod(em);
                level.setNotes(n);

                fbh.createLevel(level);

                if (selectedImageUri != null) {
                    fbh.setImage(site.getID() + "/" + level.getID(), "map", ".jpg", selectedImageUri);
                }
                Toast.makeText(getApplicationContext(), "Level saved", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
                }
            });
        }

        //Button to cancel all work
        final Button cancelButton = (Button) findViewById(R.id.maincancel);

        if(!fbh.userIsExcavator(unit))
        {
            cancelButton.setVisibility(View.INVISIBLE);
        }
        else {
            cancelButton.setVisibility(View.VISIBLE);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                // Throws dialog asking if user wants to cancel without saving
                showCancelDialog();
                }
            });
        }

    }

    public void setURI(Uri u)
    {
        if(u != null) {
            selectedImageUri = u;
            unitImage.setImageURI(selectedImageUri);
            if (switcher.getNextView().equals(findViewById(R.id.pictures))) {
                switcher.showNext();
            }
        }
    }

    public String getLevelInfo()
    {
        return level != null ? level.getID() : "";
    }

    /**Displays cancel dialog ("Are you sure you want to go back? You will lose your progress")
     *when the user clicks the back button*/
    @Override
    public void onBackPressed()
    {
        if(!fbh.userIsExcavator(unit)) {
            super.onBackPressed();
        }
        else
        {
            showCancelDialog();
        }
    }

    /**
     * FROM STACKOVERFLOW https://stackoverflow.com/questions/2169649/get-pick-an-image-from-androids-built-in-gallery-app-programmatically*****
     */
     public void onActivityResult(int requestCode, int resultCode, Intent data) {

         if (resultCode == RESULT_OK) {

             if (switcher.getNextView().equals(findViewById(R.id.pictures))) {
                 switcher.showNext();
             }

             if(data != null) {
                 selectedImageUri = data.getData();
             }
             else
             {
                 selectedImageUri = Uri.fromFile(localFile);
             }
             unitImage.setImageURI(selectedImageUri);
             rotation = 0;
         }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);

        // Save our own state now
        outState.putParcelable("URI", selectedImageUri);
        outState.putBoolean("alert", (alert!=null));
        outState.putBoolean("alert1", (alert1!=null));
        outState.putInt("rotation", rotation);
    }

    private void showCancelDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        final View cancelLayout = inflater.inflate(R.layout.cancel_level_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(LevelDocument.this);
        }
        alert.setTitle("Cancel?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            alert=null;
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
            }

        });
        alert.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            //Go back
            alert=null;
            }
        });
        alert.setView(cancelLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void showChooserDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        final View chooserLayout = inflater.inflate(R.layout.chooser_dialog, null);
        alert1 = new AlertDialog.Builder(LevelDocument.this);
        alert1.setView(chooserLayout);
        alert1.setTitle("Add A Picture:");
        alert1.setIcon(R.drawable.ic_add_a_photo_white_24dp);
        final AlertDialog dialog = alert1.create();
        dialog.show();
        ImageButton findPic = (ImageButton) chooserLayout.findViewById(R.id.goToGallery);
        findPic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            //Get Picture
            Intent pictureIntent = new Intent();
            pictureIntent.setType("image/*");
            pictureIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pictureIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(pictureIntent, "Select an aerial view of your unit"), SELECT_PICTURE);
            alert1=null;
            dialog.cancel();
            }

        });

        ImageButton takePic = (ImageButton) chooserLayout.findViewById(R.id.takeNewPicture);
        takePic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

            try {
                localFile = File.createTempFile(level.getID(), ".jpg");
                //Take picture from camera
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(LevelDocument.this, "com.mycompany.sip.fileprovider", localFile));
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } catch (IOException e) {
            }


            alert1=null;
            dialog.cancel();
            }
        });
    }

    //From https://stackoverflow.com/questions/31781150/auto-image-rotated-from-portrait-to-landscape
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        matrix.setRotate(orientation);

        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            return null;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor=null;
        String[] proj = {MediaStore.Images.Media.DATA};
        cursor = this.getBaseContext().getContentResolver().query(contentURI, proj, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
