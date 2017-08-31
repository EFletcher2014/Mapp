//All from androidhive 7/30/17
package com.mycompany.sip;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AllArtifactsActivity extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> artifactsList;

    boolean test=true;
    String[] testArtifacts = {"17-2-17 seed bead", "17-2-16 projectile point", "17-2-27 flint flake"};

    // url to get all artifacts list
    //TODO: Get real URL
    private static String url_all_artifacts = "https://api.androidhive.info/android_connect/get_all_levels.php";

    // JSON Node names
    //TODO: get correct variables (once server is running)
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UNITS = "units";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "artifact";
    private static String siteName = "";
    private static String unitNumber = "";
    private static String levelDepth ="";

    // levels JSONArray
    JSONArray levels = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_artifacts);

        // Hashmap for ListView
        artifactsList = new ArrayList<HashMap<String, String>>();

        //added by Emily Fletcher 8/29/17
        Intent openIntent = getIntent();
        siteName = openIntent.getStringExtra("name");
        unitNumber = openIntent.getStringExtra("datum");
        levelDepth = openIntent.getStringExtra("depth");
        TextView titleText = (TextView) findViewById(R.id.artifactsLabel);
        String title = siteName + " " + unitNumber + " " + levelDepth + " Artifacts";
        titleText.setText(title);

        if(!test) {
            // Loading sites in Background Thread
            new LoadAllLevels().execute();
        }
        else
        {
            // looping through All levels
            for (int i = 0; i < 3; i++) {

                String artifact = testArtifacts[i];

                // creating new HashMap
                HashMap<String, String> testMap = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                testMap.put(TAG_PID, i + "");
                testMap.put(TAG_NAME, artifact);

                // adding HashList to ArrayList
                artifactsList.add(testMap);
                System.out.println(artifactsList);
            }
            ListAdapter adapter = new SimpleAdapter(
                    AllArtifactsActivity.this, artifactsList,
                    R.layout.list_item, new String[] { TAG_PID,
                    TAG_NAME},
                    new int[] { R.id.pid, R.id.name });
            // updating listview
            setListAdapter(adapter);
        }

        // Get listview
        ListView lv = getListView();

        //TODO: make this do something else (but what??). Fix in other activities
        // on seleting single level
        // launching Edit level Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                LayoutInflater inflater = getLayoutInflater();
                View artifactLayout = inflater.inflate(R.layout.layout_custom_dialog, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(AllArtifactsActivity.this);
                alert.setTitle("Edit Artifact");
                // this is set the view from XML inside AlertDialog
                alert.setView(artifactLayout);
                AlertDialog dialog = alert.create();
                dialog.show();

                //TODO: make these fields autofill, like the other edit screens
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
                String depth = ((TextView) view.findViewById(R.id.name)).getText()
                        .toString();

            }
        });

        //on clicking new Level button
        //launching new level activity
       Button newArtifact = (Button) findViewById(R.id.newArtifactBtn);
        newArtifact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View artifactLayout = inflater.inflate(R.layout.layout_custom_dialog, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(AllArtifactsActivity.this);
                alert.setTitle("Add A New Artifact");
                // this is set the view from XML inside AlertDialog
                alert.setView(artifactLayout);
                AlertDialog dialog = alert.create();
                dialog.show();

                //TODO: When user clicks save, should save to server and add new artifact to list
            }
        });

    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted level
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllLevels extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllArtifactsActivity.this);
            pDialog.setMessage("Loading levels. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All levels from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            HashMap params = new HashMap();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All levels: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // levels found
                    // Getting Array of levels
                    levels = json.getJSONArray(TAG_UNITS);

                    // looping through All sites
                    for (int i = 0; i < levels.length(); i++) {
                        JSONObject c = levels.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);

                        // adding HashList to ArrayList
                        artifactsList.add(map);
                    }
                } else {
                    // no levels found
                    // Launch Add New level Activity
                    Intent i = new Intent(getApplicationContext(),
                            MapHome.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
            // dismiss the dialog after getting all sites
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AllArtifactsActivity.this, artifactsList,
                            R.layout.list_item, new String[] { TAG_PID,
                            TAG_NAME},
                            new int[] { R.id.pid, R.id.name });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}