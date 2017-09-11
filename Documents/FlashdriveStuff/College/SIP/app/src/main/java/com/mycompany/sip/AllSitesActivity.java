//All from androidhive 7/30/17
//TODO: document
//TODO: Figure out if I need an identical activity for all sites/units/levels and a create new for all three
//TODO: or if I can just reuse the same ones
package com.mycompany.sip;

        import java.text.SimpleDateFormat;
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

public class AllSitesActivity extends ListActivity {
        // Progress Dialog
        private ProgressDialog pDialog;
        boolean test=true;
        //TODO: Make this an arraylist so that even in testing sites can be added and deleted [FIGURE OUT HOW TO DELETE SITES]
        Site[] testSites = {new Site("Fort St. Joseph", "20BE23", "11/03/1996", "location", "a site"),
                new Site("Lyne Site", "20BE10", "11/1/1111", "location", "another site"),
                new Site("Fort Michilimackinac", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("Fort Mackinac", "23MA23", "11/11/1011", "location", "yet another freaking site"),
                new Site("Site A", "22ZZ23", "11/11/1010", "location", "yet another freaking site"),
                new Site("Fletcher Site", "3FL3", "11/11/1010", "location", "yet another freaking site"),
                new Site("Jamestown", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("White City", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("Chichen Itza", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("Dan", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("foobar", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("Copenhagen", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("test site", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("goldfish crackers","22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("Kampsville Gardens", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("horrible reviews", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("yahoo", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("teotihuacan", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("n0", "22MA23", "11/11/1010", "location", "yet another freaking site"),
                new Site("yes", "22MA23", "11/11/1010", "location", "yet another freaking site"),};

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();
        JSONParser jsonParser = new JSONParser();//TODO: is it necessary to have two?

        ArrayList<HashMap<String, String>> sitesList;

        // url to get all sites list
        //TODO: Get real URL
        private static String url_all_sites = "https://api.androidhive.info/android_connect/get_all_sites.php";

        //TODO: get actual URL
        // url to create new site
        private static String url_create_site = "https://api.androidhive.info/android_connect/create_product.php";

        // JSON Node names
        //TODO: figure out what these do
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_SITES = "sites";
        private static final String TAG_PID = "pid";
        private static final String TAG_NAME = "name";

        private static SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        private static Site site;

        // sites JSONArray
        JSONArray sites = null;

        private String siteName;
        private String siteDesc;
        private String dateFound;
        private String siteNumber;
        private String siteLoca;
        AlertDialog.Builder alert;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
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

                    String name = testSites[i].getName();

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
                                Intent in = new Intent(view.getContext(),
                                        AllUnitsActivity.class);
                                // sending pid to next activity
                                in.putExtra(TAG_PID, pid);
                                in.putExtra(TAG_NAME, testSites[Integer.parseInt(pid)]);

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
                        // Launch Add New Site Dialog
                        LayoutInflater inflater = getLayoutInflater();
                        final View siteLayout = inflater.inflate(R.layout.new_site_dialog, null);
                        alert = new AlertDialog.Builder(AllSitesActivity.this);
                        alert.setTitle("Create A New Site");
                        alert.setPositiveButton("Create Site", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                EditText inputName = (EditText) siteLayout.findViewById(R.id.inputName);
                                EditText inputDesc = (EditText) siteLayout.findViewById(R.id.inputDesc);
                                EditText inputDate = (EditText) siteLayout.findViewById(R.id.inputDate);
                                EditText inputNumb = (EditText) siteLayout.findViewById(R.id.inputNumb);
                                EditText inputLoca = (EditText) siteLayout.findViewById(R.id.inputLoca);

                                    site = new Site(inputName.getText().toString(), inputNumb.getText().toString(), inputDate.getText().toString(), inputLoca.getText().toString(), inputDesc.getText().toString());



                                //TODO: Make sure this new site shows up on all sites
                                //if not testing, save to server
                                if(!test) {

                                    // creating new site in background thread
                                    new CreateNewSite().execute();
                                }
                                else
                                {
                                    System.out.println(site.toString());
                                    // just go to next activity
                                    CharSequence toastMessage = "Creating New Site...";
                                    Toast toast = Toast.makeText(siteLayout.getContext(), toastMessage, Toast.LENGTH_LONG);
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
                        alert.setView(siteLayout);
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
                                        // Launch Add New product Dialog
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

    /**
     * Background Async Task to Create new product
     * */
    class CreateNewSite extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllSitesActivity.this);
            pDialog.setMessage("Creating Site..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating site
         * */
        protected String doInBackground(String... args) {

            // Building Parameters
            HashMap params = new HashMap();
            params.put("siteName", site.getName());
            params.put("description", site.getDescription());
            params.put("dateFound", site.getDateOpened());
            params.put("siteNumber", site.getNumber());
            params.put("location", site.getLocation());

            // getting JSON Object
            // Note that create site url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_site,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    //TODO: Should this go to a new dialog or the current activity

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        if(alert!=null)
        {
            //TODO: Figure out how to save alert and all of its corresponding strings--UGH
        }
    }
}