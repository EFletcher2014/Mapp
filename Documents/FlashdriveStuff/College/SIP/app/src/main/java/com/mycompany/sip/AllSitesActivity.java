//All from androidhive 7/30/17
//TODO: document
//TODO: Figure out if I need an identical activity for all sites/units/levels and a create new for all three
//TODO: or if I can just reuse the same ones
package com.mycompany.sip;

        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.StringTokenizer;

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
        import android.widget.DatePicker;
        import android.widget.EditText;
        import android.widget.ListAdapter;
        import android.widget.ListView;
        import android.widget.SimpleAdapter;
        import android.widget.TextView;
        import android.widget.Toast;

public class AllSitesActivity extends ListActivity {
        // Progress Dialog
        private ProgressDialog pDialog;
        SiteDatabaseHandler sdb = new SiteDatabaseHandler(this);
        ArrayList<Site> allSites = new ArrayList<>();
        boolean test=false;
        //TODO: Make this an arraylist so that even in testing sites can be added and deleted [FIGURE OUT HOW TO DELETE SITES]
        Site[] testSites = {new Site("Fort St. Joseph", "20BE23", "11/03/1996", "location", "a site", 0),
                new Site("Lyne Site", "20BE10", "11/1/1111", "location", "another site", 1),
                new Site("Fort Michilimackinac", "22MA23", "11/11/1010", "location", "yet another freaking site", 2),
                new Site("Fort Mackinac", "23MA23", "11/11/1011", "location", "yet another freaking site", 3),
                new Site("Site A", "22ZZ23", "11/11/1010", "location", "yet another freaking site", 4),
                new Site("Fletcher Site", "3FL3", "11/11/1010", "location", "yet another freaking site", 5),
                new Site("Jamestown", "22MA23", "11/11/1010", "location", "yet another freaking site", 6),
                new Site("White City", "22MA23", "11/11/1010", "location", "yet another freaking site", 7),
                new Site("Chichen Itza", "22MA23", "11/11/1010", "location", "yet another freaking site", 6),
                new Site("Dan", "22MA23", "11/11/1010", "location", "yet another freaking site", 9),
                new Site("foobar", "22MA23", "11/11/1010", "location", "yet another freaking site", 10),
                new Site("Copenhagen", "22MA23", "11/11/1010", "location", "yet another freaking site", 11),
                new Site("test site", "22MA23", "11/11/1010", "location", "yet another freaking site", 12),
                new Site("goldfish crackers","22MA23", "11/11/1010", "location", "yet another freaking site", 13),
                new Site("Kampsville Gardens", "22MA23", "11/11/1010", "location", "yet another freaking site", 14),
                new Site("horrible reviews", "22MA23", "11/11/1010", "location", "yet another freaking site", 15),
                new Site("yahoo", "22MA23", "11/11/1010", "location", "yet another freaking site", 16),
                new Site("teotihuacan", "22MA23", "11/11/1010", "location", "yet another freaking site", 17),
                new Site("n0", "22MA23", "11/11/1010", "location", "yet another freaking site", 18),
                new Site("yes", "22MA23", "11/11/1010", "location", "yet another freaking site", 19),};

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();
        JSONParser jsonParser = new JSONParser();//TODO: is it necessary to have two?

        ArrayList<HashMap<String, String>> sitesList;

    //TODO: HANDLE BEING OFFLINE

        // url to get all sites list
        private static String url_all_sites = "http://75.134.106.101:80/mapp/get_all_sites.php";

        // url to create new site
        private static String url_create_site = "http://75.134.106.101:80/mapp/create_new_site.php";

        // JSON Node names
        private static final String TAG_PID = "PrimaryKey";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_SITES = "sites";
        private static final String TAG_NUM = "siteNumber";
        private static final String TAG_NAME = "siteName";
        private static final String TAG_LOC = "location";
        private static final String TAG_DESC = "description";
        private static final String TAG_DATE = "dateDiscovered";

        private static SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        private static Site site;

        private EditText inputName;
        private EditText inputDesc;
        //private DatePicker inputDate;
        private EditText inputDate;
        private EditText inputMonth;
        private EditText inputYear;
        private EditText inputNumb;
        private EditText inputLoca;


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

                if(savedInstanceState!=null)
                {
                    if(savedInstanceState.getBoolean("alert"))
                    {
                        final String sName = savedInstanceState.getString("Site Name");
                        final String sDesc = savedInstanceState.getString("Description");
                        final String sDate = savedInstanceState.getString("Date Discovered");
                        final String sNumb = savedInstanceState.getString("Site Number");
                        final String sLoca = savedInstanceState.getString("Location");

                        showDialog(new Site(sName, sNumb, sDate, sLoca, sDesc, -1));//TODO: can this be -1?
                    }
                }
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
                                in.putExtra(TAG_PID, Integer.parseInt(pid));
                                if(test)
                                {
                                    in.putExtra(TAG_NAME, testSites[Integer.parseInt(pid)]);
                                }
                                else
                                {
                                    in.putExtra(TAG_NAME, allSites.get(Integer.parseInt(pid)-1));
                                }

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

                        showDialog(null);

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

                                            //TODO: Incorporate model objects

                                                // Storing each json item in variable
                                                String pk = c.getString(TAG_PID);
                                                String name = c.getString(TAG_NAME);
                                                String siteNumber = c.getString(TAG_NUM);
                                                String location = c.getString(TAG_LOC);
                                                String description = c.getString(TAG_DESC);
                                                String date = c.getString(TAG_DATE);

                                                Site temp = new Site(name, siteNumber, date, location, description, Integer.parseInt(pk));
                                                allSites.add(temp);

                                                // creating new HashMap
                                                HashMap<String, String> map = new HashMap<String, String>();

                                                // adding each child node to HashMap key => value
                                                map.put(TAG_PID, pk);
                                                map.put(TAG_NAME, name);

                                                // adding HashList to ArrayList
                                                sitesList.add(map);

                                            //save to local database
                                            if(sdb.updateSite(temp)==0)
                                            {
                                                System.out.println("Adding new site to SQLite DB");
                                                sdb.addSite(temp);
                                            }
                                            else
                                            {
                                                System.out.println();
                                            }
                                            System.out.println(sdb.getSitesCount());
                                            System.out.println(sdb.getSite(i));
                                            System.out.println(sdb.getAllSites().toString());
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
            params.put("siteNumber", site.getNumber());
            params.put("location", site.getLocation());
            params.put("description", site.getDescription());
            params.put("dateDiscovered", site.getDateOpened());

            // getting JSON Object
            // Note that create site url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_site,
                    "POST", params);

            System.out.println(json);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    //new LoadAllSites().execute();
                    // closing this screen
                    finish();
                    startActivity(getIntent());
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //TODO: figure out where this should go
            sdb.addSite(site);

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
            outState.putBoolean("alert", true);
            outState.putString("Site Name", inputName.getText().toString());
            outState.putString("Description", inputDesc.getText().toString());
            outState.putString("Year", (inputYear.getText().toString()));
            outState.putString("Month", (inputMonth.getText().toString()));
            outState.putString("Day", (inputDate.getText().toString()));
            outState.putString("Site Number", inputNumb.getText().toString());
            outState.putString("Location", inputLoca.getText().toString());

        }
        else
        {
            outState.putBoolean("alert", false);
        }
    }

    private void showDialog(Site st)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View siteLayout = inflater.inflate(R.layout.new_site_dialog, null);
        alert = new AlertDialog.Builder(AllSitesActivity.this);
        inputName = (EditText) siteLayout.findViewById(R.id.inputName);
        inputDesc = (EditText) siteLayout.findViewById(R.id.inputDesc);
        //inputDate = (DatePicker) siteLayout.findViewById(R.id.inputDate);
        inputDate = (EditText) siteLayout.findViewById(R.id.inputDate);
        inputMonth = (EditText) siteLayout.findViewById(R.id.inputMonth);
        inputYear = (EditText) siteLayout.findViewById(R.id.inputYear);
        inputNumb = (EditText) siteLayout.findViewById(R.id.inputNumb);
        inputLoca = (EditText) siteLayout.findViewById(R.id.inputLoca);

        if(st!=null)
        {
            inputName.setText(st.getName());
            inputDesc.setText(st.getDescription());

            //TODO: figure out if I want to change so that user can enter a partial date
            int[] date = fromDate(st.getDateOpened());
            String y=date[0]+"", m=date[1]+"", d=date[2]+"";
            if(date[0]!=0)
                inputYear.setText(y);
            if(date[1]!=0)
                inputMonth.setText(m);
            if(date[2]!=0)
                inputDate.setText(d);
            inputNumb.setText(st.getNumber());
            inputLoca.setText(st.getLocation());
        }
        alert.setTitle("Create A New Site");
        alert.setPositiveButton("Create Site", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int y, m, d;
                try
                {
                    y=Integer.parseInt(inputYear.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    y=0;
                }
                try
                {
                    m=Integer.parseInt(inputMonth.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    m=0;
                }
                try
                {
                    d=Integer.parseInt(inputDate.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    d=0;
                }

                site = new Site(inputName.getText().toString(), inputNumb.getText().toString(),
                        toDate(y, m, d), inputLoca.getText().toString(), inputDesc.getText().toString(), -1);
                //TODO: have CreateNewSite() return pk???

                System.out.println("text: " + inputName.getText());
                if(!(inputName.getText().toString().equals("")) && !(inputDesc.getText().toString().equals(""))
                        && !(inputNumb.getText().toString().equals("")) && !(inputLoca.getText().toString().equals(""))
                        && !(inputDate.getText().toString().equals("")) && !(inputDate.getText().toString().equals("")) && !(inputDate.getText().toString().equals("")) && !(inputDesc.getText().toString().equals(""))) {

                    //if not testing, save to server
                    if (!test) {

                        // creating new site in background thread
                        new CreateNewSite().execute();
                    } else {
                        System.out.println(site.toString());
                        // just go to next activity
                        CharSequence toastMessage = "Creating New Site...";
                        Toast toast = Toast.makeText(siteLayout.getContext(), toastMessage, Toast.LENGTH_LONG);
                        toast.show();

                    }
                    alert = null;
                }
                else
                {
                    Toast.makeText(siteLayout.getContext(), "You must fill out every field before saving", Toast.LENGTH_SHORT).show();
                    showDialog(site);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Go back
                alert=null;
            }
        });
        // this is set the view from XML inside AlertDialog
        alert.setView(siteLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private String toDate(int year, int month, int day)
    {
        String m=month + "", d=day + "";
        System.out.println("Converting to date! " + year + " " + m + " " + d);
        if(year<1 || month<1 || day<1)
        {
            return "0000-00-00 00:00:00";
        }
        else {
            if (month < 10) {
                m = "0" + month;
            }
            if (day < 10) {
                d = "0" + day;
            }
            return year + "-" + m + "-" + d + " 00:00:00";
        }
    }

    private int[] fromDate(String date)
    {
        System.out.println("Converting " + date + " from date");
        int[] ymd = new int[3];
        date.replace(" 00:00:00", "");
        int i=0;
        try {
            while (i < 3) {
                ymd[i] = Integer.parseInt(date.split("-")[i]);
                i++;
            }
        }catch(NumberFormatException e)
        {
            System.out.println("Error: date not valid");
            ymd[0]=0;
            ymd[1]=0;
            ymd[2]=0;
        }
        System.out.println(ymd[0] + " " + ymd[1] + " " + ymd[2]);
        return ymd;
    }
}