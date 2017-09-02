package com.mycompany.sip;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import org.json.*;

public class MapHome extends AppCompatActivity {
    private static int SELECT_PICTURE = 1;
    JSONParser jsonParser = new JSONParser();

    // url to create new product
    private static String url_update_product = "http://192.168.2.7:3306/mapp/android_connect/update_unit.php"; //http://192.168.2.4:3306/mapp/android_connect/update_unit.php"; //"http://192.168.2.4:3306/api.mapp.info/mapp/android_connect/update_unit.php"; //

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
  
    private Uri selectedImageUri = null;
    private String userName = "root";
    private String password = "";
    private String dbms = "mysql";
    private String serverName = "192.168.2.7";//"192.168.1.187"; //"184.53.49.56";
    private String portNumber = "3306";
    private String dbName = "mapp";
    private String siteName;
    private String siteNumber;
    private int pk = 1;
    private String unitNumber = "";
    private String levelNumber;
    private String levelDepth;
    private String imageReference = "IMAGE";
    private String description = "DESCRIPTION HERE2";
    private String dateTime;
    private Site site;
    private Unit unit;
    private Level level;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //tried to make a new titlebar, didn't work, said I couldn't have two
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mytitle);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_home);

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra("siteName");
        siteName=site.getName();
        siteNumber=site.getNumber();
        unit = openIntent.getParcelableExtra("unitNumber");
        unitNumber=unit.getDatum();
        level = openIntent.getParcelableExtra("depth");
        if(level!=null) {
            levelNumber = level.getNumber() + "";
            levelDepth = level.getDepth();
        }
        else
        {
            //TODO: these vvvvvvv
            //Query server for new level number
            //make edittext for depth
        }
        TextView siteNameText = (TextView) findViewById(R.id.SiteNameLevel);
        siteNameText.setText("Site: " + siteName);
        TextView siteNumberText = (TextView) findViewById(R.id.siteNumLevel);
        siteNumberText.setText("Site Number: " + siteNumber);
        TextView unitNumberText = (TextView) findViewById(R.id.UnitNumberLevel);
        unitNumberText.setText("Unit: " + unitNumber);
        TextView levelNumberText = (TextView) findViewById(R.id.levelNumber);
        levelNumberText.setText("Level " + levelNumber);
        TextView levelDepthText = (TextView) findViewById(R.id.levelDepth);
        levelDepthText.setText("Depth: " + levelDepth);

        //TODO: Figure out what to do if no depth is given, i.e. a new level is created and the user has to input depth
        //Should that be an edittext? can I dynamically make it one?

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

        //TODO: Throw error if user tries to click these buttons before saving the level
        final Button toAddArtifactActivity = (Button) findViewById(R.id.toAddArtifactsActivity);
        toAddArtifactActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Move to select on image activity
                Intent artifactActivityIntent = new Intent(Intent.ACTION_ATTACH_DATA, selectedImageUri, view.getContext(), AllArtifactsActivity.class);
                artifactActivityIntent.putExtra("name", site);
                artifactActivityIntent.putExtra("datum", unit);
                artifactActivityIntent.putExtra("depth", level);
                startActivity(artifactActivityIntent);
            }
        });

        final Button toSelectOnImageActivity = (Button) findViewById(R.id.select_on_image_button);
        toSelectOnImageActivity.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  //Move to select on image activity
                  Intent selectActivityIntent = new Intent(Intent.ACTION_ATTACH_DATA, selectedImageUri, view.getContext(), selectActivity.class);
                  startActivity(selectActivityIntent);
              }
       });
        final Button saveButton = (Button) findViewById(R.id.mainsave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                new Thread(new Runnable() {
                    public void run() {
                        //Connect to server
                        System.out.println("Connecting to server...");
                        try {
                            queryServer();

                            //Class.forName("com.mysql.jdbc.Driver");
                            //connect();
                        }
                        catch (ClassNotFoundException | SQLException ex)
                        {
                            System.out.println("Nope: " + ex);
                        }
                    }
                }).start();
            }
        });
    }
    /**
     * FROM STACKOVERFLOW https://stackoverflow.com/questions/2169649/get-pick-an-image-from-androids-built-in-gallery-app-programmatically*****
     */
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                imageReference = selectedImageUri.toString();
                System.out.println("selectedImageUri is" + selectedImageUri);

                //MEDIA GALLERY
                ImageView unitImage = (ImageView) findViewById(R.id.unitImgView);
                unitImage.setImageURI(selectedImageUri);
            }
        }
    }
    public void queryServer() throws ClassNotFoundException, SQLException{
        //get info from app
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        dateTime = format.format(ts);
        EditText desc = (EditText) findViewById(R.id.unit_description);
        description = desc.getText().toString();

        //update server
        HashMap params = new HashMap();
        params.put("primaryKey", pk);
        params.put("imageReference", imageReference);
        params.put("description", description);
        params.put("dateTime", dateTime);

        JSONObject json = jsonParser.makeHttpRequest(url_update_product,
                "POST", params);
        System.out.println("posted");

        // check json success tag
        try {
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1) {
                // successfully updated
                System.out.println("success!");
            } else {
                // failed to update product
                System.out.println("fail");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws ClassNotFoundException, SQLException{
        Connection conn = /*DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/mapp", "root", "");*/ getConnection();
        Statement stmt = null;
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        dateTime = format.format(ts);
        EditText desc = (EditText) findViewById(R.id.unit_description);
        description = desc.getText().toString();

        //also get picture link from user
        String query = "SELECT PrimaryKey FROM units WHERE siteName='" + siteName + "' AND unitNumber=" + unitNumber + " AND levelNumber=" + levelNumber;
        System.out.println("Querying server..." + query);
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            System.out.println(query);
            if(rs.next()){
                System.out.println("worked to here");
                int pk=rs.getInt(1);
                stmt.executeUpdate("UPDATE mapp.units SET imageReference ='" + imageReference + "', description='" + description + "', dateTime='" + dateTime + "' WHERE PrimaryKey=" + pk );//Still need to find out how to actually update it
            }
            else {
                stmt.executeUpdate("INSERT INTO mapp.units (siteName, unitNumber, levelNumber, imageReference, description, dateTime) VALUES ('" + siteName + "', " + unitNumber + ", " + levelNumber + ", '" + imageReference + "', '" + description + "', '" + dateTime + "')");
            }
        } catch (SQLException ex) {
            System.err.println(ex);
            System.out.println("**********no luck");
        } finally {
            if (stmt != null) {stmt.close();}
        }
        conn.close();
    }
    /*********FROM ERIK HARTIG https://github.com/erikhartig/ValidationCreationSqlInterface/blob/master/src/Test.java
	 * Note large parts of this code are either directly taken from
	 * or are at least based off of code provided by java and sql tutorials
	 * on connecting to databases.
	 */
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        System.out.println("getting connection...");
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);

        if (this.dbms.equals("mysql")) {
            System.out.println("Creating driver manager...");
            conn = DriverManager.getConnection(
                    "jdbc:" + this.dbms + "://" +
                            this.serverName +
                            ":" + this.portNumber + "/" + this.dbName,
                    connectionProps);
        } else if (this.dbms.equals("derby")) {
            conn = DriverManager.getConnection(
                    "jdbc:" + this.dbms + ":" +
                            this.dbName +
                            ";create=true",
                    connectionProps);
        }
        System.out.println("Connected to database");
        return conn;
    }
}
