//All from androidhive 7/30/17
package com.mycompany.sip;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AllUnitsActivity extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> unitsList;
    boolean test=true;
    String[] testUnits = {"N24W11", "N23E9", "N24W6"};

    // url to get all sites list
    //TODO: Get real URL
    private static String url_all_units = "https://api.androidhive.info/android_connect/get_all_units.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UNITS = "units";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "datum";
    private static String siteName="";

    // sites JSONArray
    JSONArray sites = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_units);

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        siteName = openIntent.getStringExtra("name");
        TextView siteNameText = (TextView) findViewById(R.id.siteName);
        siteNameText.setText(siteName + " Units");

        // Hashmap for ListView
        unitsList = new ArrayList<HashMap<String, String>>();

        if(!test) {
            // Loading sites in Background Thread
            new LoadAllUnits().execute();
        }
        else {
            // looping through All units
            for (int i = 0; i < 3; i++) {

                String datum = testUnits[i];

                // creating new HashMap
                HashMap<String, String> testMap = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                testMap.put(TAG_PID, i + "");
                testMap.put(TAG_NAME, datum);

                // adding HashList to ArrayList
                unitsList.add(testMap);
                System.out.println(unitsList);
            }
            ListAdapter adapter = new SimpleAdapter(
                    AllUnitsActivity.this, unitsList,
                    R.layout.list_item, new String[] { TAG_PID,
                    TAG_NAME},
                    new int[] { R.id.pid, R.id.name });
            // updating listview
            setListAdapter(adapter);
        }

        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();
                String datum = ((TextView) view.findViewById(R.id.name)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        AllLevelsActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);
                in.putExtra("name", siteName);
                in.putExtra(TAG_NAME, datum);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

        //on clicking new unit button
        //launching new unit activity
        Button newUnit = (Button) findViewById(R.id.newUnitBtn);
        newUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch Add New product Activity
                Intent i = new Intent(getApplicationContext(),
                        NewUnitActivity.class);
                // Closing all previous activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
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
            // means user edited/deleted site
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllUnits extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllUnitsActivity.this);
            pDialog.setMessage("Loading units. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All sites from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            HashMap params = new HashMap();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_units, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All sites: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // sites found
                    // Getting Array of sites
                    sites = json.getJSONArray(TAG_UNITS);

                    // looping through All sites
                    for (int i = 0; i < sites.length(); i++) {
                        JSONObject c = sites.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);

                        // adding HashList to ArrayList
                        unitsList.add(map);
                    }
                } else {
                    // no sites found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            NewUnitActivity.class);
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
                            AllUnitsActivity.this, unitsList,
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