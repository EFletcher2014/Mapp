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
        import android.support.design.widget.FloatingActionButton;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.Button;
        import android.widget.ListAdapter;
        import android.widget.ListView;
        import android.widget.SimpleAdapter;
        import android.widget.TextView;

public class AllSitesActivity extends ListActivity {
        // Progress Dialog
        private ProgressDialog pDialog;
        boolean test=true;
        String[] testSites = {"Fort St. Joseph", "Lyne Site", "Fort Michilimackinac", "Fort Mackinac", "Site A", "Fletcher Site", "Jamestown", "White City", "Chichen Itza", "Dan", "foobar", "Copenhagen", "test site", "goldfish crackers", "Kampsville Gardens", "horrible reviews", "yahoo", "teotihuacan", "n0", "yes"};

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();

        ArrayList<HashMap<String, String>> sitesList;

        // url to get all sites list
        //TODO: Get real URL
        private static String url_all_sites = "https://api.androidhive.info/android_connect/get_all_sites.php";

        // JSON Node names
        //TODO: figure out what these do
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_SITES = "sites";
        private static final String TAG_PID = "pid";
        private static final String TAG_NAME = "name";

        // sites JSONArray
        JSONArray sites = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
            //TODO: insert layout
                setContentView(R.layout.activity_get_all_sites);

                // Hashmap for ListView
                sitesList = new ArrayList<HashMap<String, String>>();

                // Loading sites in Background Thread
            if(!test) {
                new LoadAllSites().execute();
            }
            else
            {
                // looping through All sites
                for (int i = 0; i < 20; i++) {

                    String name = testSites[i];

                    // creating new HashMap
                    HashMap<String, String> testMap = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    testMap.put(TAG_PID, i + "");
                    testMap.put(TAG_NAME, name);

                    // adding HashList to ArrayList
                    sitesList.add(testMap);
                    System.out.println(sitesList);
                }
                ListAdapter adapter = new SimpleAdapter(
                        AllSitesActivity.this, sitesList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_NAME},
                        new int[] { R.id.pid, R.id.name });
                // updating listview
                setListAdapter(adapter);
            }

                // Get listview
                ListView lv = getListView();

                // on selecting single site
                // launching Edit site Screen
                lv.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                                // getting values from selected ListItem
                            //TODO: also get site#--add another invisible TextView (like/replacePID)
                                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                                        .toString();

                                // Starting new intent
                                Intent in = new Intent(getApplicationContext(),
                                        AllUnitsActivity.class);
                                // sending pid to next activity
                                in.putExtra(TAG_PID, pid);
                                in.putExtra(TAG_NAME, name);

                                // starting new activity and expecting some response back
                                startActivityForResult(in, 100);
                        }
                });

                //on clicking new site button
                //launching new site activity
                /*FloatingAction*/Button newSite = (/*FloatingAction*/Button) findViewById(R.id.newSiteBtn);
                newSite.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // Launch Add New product Activity
                        Intent i = new Intent(getApplicationContext(),
                                NewSiteActivity.class);
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
                                                String pid = c.getString(TAG_PID);
                                                String name = c.getString(TAG_NAME);

                                                // creating new HashMap
                                                HashMap<String, String> map = new HashMap<String, String>();

                                                // adding each child node to HashMap key => value
                                                map.put(TAG_PID, pid);
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