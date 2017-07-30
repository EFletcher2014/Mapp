//All from androidhive 7/30/17
//TODO: document
//TODO: Figure out if I need an identical activity for all sites/units/levels and a create new for all three
//TODO: or if I can just reuse the same ones
package com.mycompany.sip;

        import java.util.ArrayList;
        import java.util.HashMap;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import android.app.ListActivity;
        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.ListAdapter;
        import android.widget.ListView;
        import android.widget.SimpleAdapter;
        import android.widget.TextView;

public class AllSitesActivity extends ListActivity {
        // Progress Dialog
        private ProgressDialog pDialog;

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();

        ArrayList<HashMap<String, String>> sitesList;

        // url to get all sites list
        //TODO: Get real URL
        private static String url_all_sites = "https://api.androidhive.info/android_connect/get_all_sites.php";

        // JSON Node names
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_SITES = "sites";
        private static final String TAG_PID = "pid";
        private static final String TAG_NAME = "name";

        // sites JSONArray
        JSONArray sites = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_get_all_sites);

                // Hashmap for ListView
                sitesList = new ArrayList<HashMap<String, String>>();

                // Loading sites in Background Thread
                new LoadAllSites().execute();

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

                                // Starting new intent
                                Intent in = new Intent(getApplicationContext(),
                                        GetAllUnits.class);
                                // sending pid to next activity
                                in.putExtra(TAG_PID, pid);

                                // starting new activity and expecting some response back
                                startActivityForResult(in, 100);
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
        class LoadAllSites extends AsyncTask<String, String, String> {

                /**
                 * Before starting background thread Show Progress Dialog
                 * */
                @Override
                protected void onPreExecute() {
                        super.onPreExecute();
                        pDialog = new ProgressDialog(AllSitesActivity.this);
                        pDialog.setMessage("Loading sites. Please wait...");
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
                        JSONObject json = jParser.makeHttpRequest(url_all_sites, "GET", params);

                        // Check your log cat for JSON reponse
                        Log.d("All sites: ", json.toString());

                        try {
                                // Checking for SUCCESS TAG
                                int success = json.getInt(TAG_SUCCESS);

                                if (success == 1) {
                                        // sites found
                                        // Getting Array of sites
                                        sites = json.getJSONArray(TAG_SITES);

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
                                                sitesList.add(map);
                                        }
                                } else {
                                        // no sites found
                                        // Launch Add New product Activity
                                        Intent i = new Intent(getApplicationContext(),
                                                NewSiteActivity.class);
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
                                                AllSitesActivity.this, sitesList,
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