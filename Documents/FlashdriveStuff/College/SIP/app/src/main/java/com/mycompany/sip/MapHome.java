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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;

public class MapHome extends AppCompatActivity {
    private static int SELECT_PICTURE = 1;
    private String userName = "root";
    private String password = "";
    private String dbms = "mysql";
    private String serverName = "10.0.2.2";//"192.168.1.187"; //"184.53.49.56";
    private String portNumber = "3306";
    private String dbName = "mapp";
    private String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
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

        final Button saveButton = (Button) findViewById(R.id.mainsave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                new Thread(new Runnable() {
                    public void run() {
                        //Connect to server
                        System.out.println("Connecting to server...");
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            connect();
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
                Uri selectedImageUri = data.getData();
                System.out.println("selectedImageUri is" + selectedImageUri);

                //MEDIA GALLERY
                ImageView unitImage = (ImageView) findViewById(R.id.unitImgView);
                unitImage.setImageURI(selectedImageUri);
            }
        }
    }
    public void connect() throws ClassNotFoundException, SQLException{
        //DriverManager.getDriver("jdbc:mysql://10.0.2.2:3306/mapp");
        System.out.println("Got Driver...");
        Connection conn = /*DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/mapp", "root", "");*/ getConnection();
        Statement stmt = null;
        String query = "Select * From mapp.units";
        System.out.println("Querying server...");
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            System.out.println(query);
            while (rs.next()){
                System.out.println(rs.getString("siteName"));
            }
            System.out.println(data);
            //stmt.executeUpdate("INSERT INTO mapp.units " + "VALUES(0, '21BE23', 5, 5, 'IMAGE', 'DESCRIPTION HERE', '2017-06-28 11:29:33')");
        } catch (SQLException ex) {
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
