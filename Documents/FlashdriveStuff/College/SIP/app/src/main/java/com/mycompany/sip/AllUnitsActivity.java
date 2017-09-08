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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AllUnitsActivity extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    JSONParser jsonParser = new JSONParser();//TODO: figure out if I need both

    ArrayList<HashMap<String, String>> unitsList;

    // url to get all sites list
    //TODO: Get real URL
    private static String url_all_units = "https://api.androidhive.info/android_connect/get_all_units.php";

    //TODO: get actual URL
    // url to create new unit
    private static String url_create_unit = "https://api.androidhive.info/android_connect/create_product.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UNITS = "units";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "datum";
    private static Site site;
    private static Unit unit;

    boolean test=true;
    Unit[] testUnits = {new Unit("N24W11", "07/21/17", "1", "2", site, "Emily Fletcher and Meghan Williams", "possible blacksmith quarters"),
            new Unit("N23E9",  "07/21/17", "1", "2", site, "Emily Fletcher and Meghan Williams", "possible blacksmith quarters"),
            new Unit("N24W6",  "07/21/17", "1", "2", site, "Emily Fletcher and Meghan Williams", "possible blacksmith quarters")};

    // sites JSONArray
    JSONArray sites = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_units);

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra("name");
        TextView siteNameText = (TextView) findViewById(R.id.siteName);
        siteNameText.setText(site.getName() + " Units");

        // Hashmap for ListView
        unitsList = new ArrayList<HashMap<String, String>>();

        if (!test) {
            // Loading sites in Background Thread
            new LoadAllUnits().execute();
        } else {
            // looping through All units
            for (int i = 0; i < 3; i++) {

                String datum = testUnits[i].getDatum();

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
                    R.layout.list_item, new String[]{TAG_PID,
                    TAG_NAME},
                    new int[]{R.id.pid, R.id.name});
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
                Intent in = new Intent(view.getContext(),
                        AllLevelsActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);
                in.putExtra("name", site);
                in.putExtra(TAG_NAME, testUnits[Integer.parseInt(pid)]);

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

                //TODO: make sure these new units are loading on list

                // Launch Add New Site Dialog
                LayoutInflater inflater = getLayoutInflater();
                final View unitLayout = inflater.inflate(R.layout.new_unit_dialog, null);
                AlertDialog.Builder alert = new AlertDialog.Builder(AllUnitsActivity.this);
                alert.setTitle("Create A New Unit");
                alert.setPositiveButton("Create Unit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText inputCoords = (EditText) unitLayout.findViewById(R.id.inputCoords);
                        EditText inputExcs = (EditText) unitLayout.findViewById(R.id.inputExcs);
                        EditText inputDate = (EditText) unitLayout.findViewById(R.id.inputDate);
                        EditText inputReas = (EditText) unitLayout.findViewById(R.id.inputReas);
                        EditText inputNSDims = (EditText) unitLayout.findViewById(R.id.inputNSDims);
                        EditText inputEWDims = (EditText) unitLayout.findViewById(R.id.inputEWDims);

                        unit = new Unit(inputCoords.getText().toString(),
                                inputDate.getText().toString(), inputNSDims.getText().toString(),
                                inputEWDims.getText().toString(), site, inputExcs.getText().toString(),
                                inputReas.getText().toString());

                        //if not testing, save to server
                        if (!test) {

                            // creating new unit in background thread
                            new CreateNewUnit().execute();
                        } else {
                            System.out.println(unit.toString());
                            // just go to next activity
                            CharSequence toastMessage = "Creating New Unit...";
                            Toast toast = Toast.makeText(unitLayout.getContext(), toastMessage, Toast.LENGTH_LONG);
                            toast.show();

                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Go back
                    }
                });
                // this is set the view from XML inside AlertDialog
                alert.setView(unitLayout);
                AlertDialog dialog = alert.create();
                dialog.show();
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
                    // Launch Add New unit Dialog
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
                pDialog = new ProgressDialog(AllUnitsActivity.this);
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
                params.put("coordinates", unit.getDatum());
                params.put("excavators", unit.getExcavators());
                params.put("dateOpened", unit.getDateOpened());
                params.put("reasonForOpening", unit.getReasonForOpening());

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
                        //TODO: Should this go to NewUnitDialog or AllUnitsActivity?
                        //TODO: make this not getApplicationContext
                        Intent i = new Intent(getApplicationContext(), AllUnitsActivity.class);
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