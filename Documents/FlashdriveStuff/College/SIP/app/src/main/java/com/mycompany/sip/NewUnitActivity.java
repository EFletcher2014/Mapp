package com.mycompany.sip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewUnitActivity extends Activity {

    //test unit
    boolean test=true;
    SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
    String coordinates;
    String excavators;
    String dateOpened;
    String reasonForOpening;
    String dimensions;
    //TODO: make sure they're formatted correctly


    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    EditText inputCoords;
    EditText inputExcs;
    EditText inputDate;
    EditText inputReas;
    EditText inputDims;
    //TODO: might be better to have a layout where the user is forced to input all four coordinates manually

    //TODO: get actual URL
    // url to create new unit
    private static String url_create_unit = "https://api.androidhive.info/android_connect/create_product.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_unit);

        // Edit Text
        //TODO: will inputDate in two different activities be alright?
        inputCoords = (EditText) findViewById(R.id.inputCoords);
        inputExcs = (EditText) findViewById(R.id.inputExcs);
        inputDate = (EditText) findViewById(R.id.inputDate);
        inputReas = (EditText) findViewById(R.id.inputReas);
        inputDims = (EditText) findViewById(R.id.inputDims);

        // Create button
        Button btnCreateUnit = (Button) findViewById(R.id.btnCreateUnit);

        // button click event
        btnCreateUnit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                coordinates = inputCoords.getText().toString();
                excavators = inputExcs.getText().toString();
                dateOpened = format.format(inputDate.getText().toString());
                reasonForOpening = inputReas.getText().toString();

                //if not testing, save to server
                if(!test) {

                    // creating new unit in background thread
                    new CreateNewUnit().execute();
                }
                else
                {
                    System.out.println("coordinates: " + coordinates + "/nexcavators: " + excavators + "/ndateOpened: " + dateOpened + "/nreason: " + reasonForOpening);
                    // just go to next activity
                    //TODO: Should this go to NewUnitActivity or AllUnitsActivity?
                    Intent i = new Intent(getApplicationContext(), AllUnitsActivity.class);
                    startActivity(i);

                }
            }
        });
    }

    /**
     * Background Async Task to Create new product
     * */
    class CreateNewUnit extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewUnitActivity.this);
            pDialog.setMessage("Creating Unit..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating unit
         * */
        protected String doInBackground(String... args) {

            // Building Parameters
            HashMap params = new HashMap();
            params.put("coordinates", coordinates);
            params.put("excavators", excavators);
            params.put("dateOpened", dateOpened);
            params.put("reasonForOpening", reasonForOpening);

            // getting JSON Object
            // Note that create site url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_unit,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    //TODO: Should this go to NewUnitActivity or AllUnitsActivity?
                    Intent i = new Intent(getApplicationContext(), NewUnitActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }
}