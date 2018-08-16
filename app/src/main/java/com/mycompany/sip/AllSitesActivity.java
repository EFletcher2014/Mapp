//A class to pull all sites from a MySQL server and display them in a listview
//From tutorial on androidhive accessed 7/30/17
//Author: Emily Fletcher
//TODO: Figure out if I need an identical activity for all sites/units/levels and a create new for all three
//TODO: or if I can just reuse the same ones
package com.mycompany.sip;

        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;

        import org.json.JSONArray;

        import android.app.AlertDialog;
        import android.app.ListActivity;
        import android.app.ProgressDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.util.Log;
        import android.view.ContextThemeWrapper;
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

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.firestore.*;

        import static android.content.ContentValues.TAG;
        import static com.mycompany.sip.Global.*;

public class AllSitesActivity extends ListActivity {
        // Progress Dialog--from AndroidHive -EF
        private ProgressDialog pDialog;

        //Firebase
        FirebaseFirestore mappDB = FirebaseFirestore.getInstance();
        DatabaseHandler handler = new DatabaseHandler();

        //Handler for local SQLite database to backup data to device
        LocalDatabaseHandler ldb = new LocalDatabaseHandler(this);

        //Handler for remote database
        RemoteDatabaseHandler rdb = new RemoteDatabaseHandler(this);

        //ArrayList to store all sites on the server for display
        ArrayList<Site> allSites = new ArrayList<>();

        //Boolean for testing
        boolean test=false;

        //Array of fake test sites, to be used if test==true
        //TODO: Make this an arraylist so that even in testing sites can be added and deleted [FIGURE OUT HOW TO DELETE SITES]
        Site[] testSites = {new Site("Fort St. Joseph", "20BE23", "11/03/1996", "location", "a site", 0, 0, null, null),
                new Site("Lyne Site", "20BE10", "11/1/1111", "location", "another site", 1, 1, null, null),
                new Site("Fort Michilimackinac", "22MA23", "11/11/1010", "location", "yet another freaking site", 2, 2, null, null),
                new Site("Fort Mackinac", "23MA23", "11/11/1011", "location", "yet another freaking site", 3, 3, null, null),
                new Site("Site A", "22ZZ23", "11/11/1010", "location", "yet another freaking site", 4, 4, null, null),
                new Site("Fletcher Site", "3FL3", "11/11/1010", "location", "yet another freaking site", 5, 5, null, null),
                new Site("Jamestown", "22MA23", "11/11/1010", "location", "yet another freaking site", 6, 6, null, null),
                new Site("White City", "22MA23", "11/11/1010", "location", "yet another freaking site", 7, 7, null, null),
                new Site("Chichen Itza", "22MA23", "11/11/1010", "location", "yet another freaking site", 8, 8, null, null),
                new Site("Dan", "22MA23", "11/11/1010", "location", "yet another freaking site", 9, 9, null, null),
                new Site("foobar", "22MA23", "11/11/1010", "location", "yet another freaking site", 10, 10, null, null),
                new Site("Copenhagen", "22MA23", "11/11/1010", "location", "yet another freaking site", 11, 11,null, null),
                new Site("test site", "22MA23", "11/11/1010", "location", "yet another freaking site", 12, 12, null, null),
                new Site("goldfish crackers","22MA23", "11/11/1010", "location", "yet another freaking site", 13, 13, null, null),
                new Site("Kampsville Gardens", "22MA23", "11/11/1010", "location", "yet another freaking site", 14, 14, null, null),
                new Site("horrible reviews", "22MA23", "11/11/1010", "location", "yet another freaking site", 15, 15, null, null),
                new Site("yahoo", "22MA23", "11/11/1010", "location", "yet another freaking site", 16, 16, null, null),
                new Site("teotihuacan", "22MA23", "11/11/1010", "location", "yet another freaking site", 17, 17, null, null),
                new Site("n0", "22MA23", "11/11/1010", "location", "yet another freaking site", 18, 18, null, null),
                new Site("yes", "22MA23", "11/11/1010", "location", "yet another freaking site", 19, 19, null, null),};

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();
        JSONParser jsonParser = new JSONParser();//TODO: is it necessary to have two?

        //HashMap to be passed to ListView, contains site's name and primary key
        ArrayList<HashMap<String, String>> sitesList;

    //TODO: HANDLE BEING OFFLINE

        //Format needed to save a date to MySQL
        private static SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

        //TODO: what does this do?
        private static Site site;

        //EditTexts for the dialog to create a new site
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

        //TODO: what does this do?
        AlertDialog.Builder alert;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            System.out.println("created");
            //this.getApplicationContext().deleteDatabase("mapp");
                super.onCreate(savedInstanceState);

                //This statement is true if the user has rotated the screen during this activity
                if(savedInstanceState!=null)
                {
                    //Determines if the dialog to create a new site was open before the user rotated the screen
                    if(savedInstanceState.getBoolean("alert"))
                    {
                        final String sName = savedInstanceState.getString("Site Name");
                        final String sDesc = savedInstanceState.getString("Description");
                        final String sDate = savedInstanceState.getString("Date Discovered");
                        final String sNumb = savedInstanceState.getString("Site Number");
                        final String sLoca = savedInstanceState.getString("Location");

                        //Reopens dialog to create a site with existing inputs
                        showDialog(new Site(sName, sNumb, sDate, sLoca, sDesc, -1, -1, null, null));//TODO: can this be -1?
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
                    testMap.put(TAG_SITENAME, name);

                    // adding HashList to ArrayList
                    sitesList.add(testMap);
                }

                //From androidhive tutorial
                ListAdapter adapter = new SimpleAdapter(
                        AllSitesActivity.this, sitesList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_SITENAME},
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
                                        .toString();//Gets pk

                                // Starting new intent
                                Intent in = new Intent(view.getContext(),
                                        AllUnitsActivity.class);
                                // sending pk to list units activity
                                in.putExtra(TAG_PID, pid);

                                if(test)
                                {
                                    in.putExtra(TAG_SITENAME, testSites[Integer.parseInt(pid)]);//Get from test sites
                                }
                                else
                                {
                                    in.putExtra(TAG_SITENAME, handler.getSite(pid));
                                }

                                // starting unit activity
                                //TODO: why ForResult?
                                startActivityForResult(in, 100);
                        }
                });

                //on clicking new site button
                //launching new site dialog
                Button newSite = (Button) findViewById(R.id.newSiteBtn);
                newSite.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        // Launch Add New Site Dialog

                        showDialog(null);

                    }
            });

        }

        //TODO: I don't think I even use this--delete?
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
         * Background Async Task to Load all sites by making HTTP Request to server
         * From AndroidHive tutorial
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
                    allSites = new ArrayList<>();
                    mappDB.collection("sites")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Site site = new Site(document.getId(), document.get("Name").toString(), document.get("Number").toString(), document.get("Description").toString());
                                            allSites.add(site);
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                        }

                                        System.out.println("Sites recovered: " + allSites);
                                        for(int i = 0; i<allSites.size(); i++)
                                        {
                                            Site temp = allSites.get(i);
                                            // creating new HashMap
                                            HashMap<String, String> map = new HashMap<String, String>();
                                            map.put(TAG_PID, temp.getNumber());
                                            map.put(TAG_SITENAME, temp.getName());

                                            // adding HashList to ArrayList
                                            sitesList.add(map);
                                        }

                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                /**
                                                 * Updating parsed JSON data into ListView
                                                 * */
                                                ListAdapter adapter = new SimpleAdapter(
                                                        AllSitesActivity.this, sitesList,
                                                        R.layout.list_item, new String[] { TAG_PID,
                                                        TAG_SITENAME},
                                                        new int[] { R.id.pid, R.id.name }); //listview entries will contain site's pk and name

                                                // updating listview
                                                setListAdapter(adapter);
                                            }
                                        });
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });
                    return null;
                }

                /**
                 * After completing background task Dismiss the progress dialog
                 * and add data to listview
                 * **/
                protected void onPostExecute(String file_url) {
                        // dismiss the dialog after getting all sites
                        pDialog.dismiss();

                }

        }

    /**
     * Background Async Task to Create new site
     * From Androidhive tutorial
     * */
    class CreateNewSite extends AsyncTask<String, String, String> {
        boolean success = false;
        /**
         * Before starting background thread Show Progress Dialog
         * */
        //TODO: is this necessary?
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
            Map<String, Object> temp = new HashMap<>();
            temp.put("Description", site.getDescription());
            temp.put("Name", site.getName());
            temp.put("Number", site.getNumber());

            mappDB.collection("sites").add(temp);
            success = true;
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();

            if(success) {
                // closing this screen
                finish();

                //restarting activity so list will include new site
                startActivity(getIntent());
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Ensures that the create new site dialog will be preserved if the user rotates the screen
        // From where?
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        if(alert!=null)//If the dialog is pulled up
        {
            //Get user inputs from the dialog, saves them
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
            //tells the new activity that there isn't a dialog active. Might not be necessary
            outState.putBoolean("alert", false);
        }
    }

    //Displays the dialog which allows a user to create a new site
    private void showDialog(Site st)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View siteLayout = inflater.inflate(R.layout.new_site_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(AllSitesActivity.this);
        }
        inputName = (EditText) siteLayout.findViewById(R.id.inputName);
        inputDesc = (EditText) siteLayout.findViewById(R.id.inputDesc);
        //inputDate = (DatePicker) siteLayout.findViewById(R.id.inputDate);
        inputDate = (EditText) siteLayout.findViewById(R.id.inputDate);
        inputMonth = (EditText) siteLayout.findViewById(R.id.inputMonth);
        inputYear = (EditText) siteLayout.findViewById(R.id.inputYear);
        inputNumb = (EditText) siteLayout.findViewById(R.id.inputNumb);
        inputLoca = (EditText) siteLayout.findViewById(R.id.inputLoca);

        //If screen was rotated and recreating an existing dialog
        if(st!=null)
        {
            //Populate with user's previous input
            inputName.setText(st.getName());
            inputDesc.setText(st.getDescription());

            //TODO: figure out if I want to change so that user can enter a partial date
            //Parsing the date from the saved format to the displayed format
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
                //When user saves site, must parse displayed date into correct format
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

                //Creating site from user's inputted data. CreateNewSite will use this later
                site = new Site(inputName.getText().toString(), inputNumb.getText().toString(),
                        toDate(y, m, d), inputLoca.getText().toString(), inputDesc.getText().toString(), -1, -1, null, null);
                //TODO: have CreateNewSite() return pk???

                System.out.println("text: " + inputName.getText());

                //If all fields are filled out
                if(!(inputName.getText().toString().equals("")) && !(inputDesc.getText().toString().equals(""))
                        && !(inputNumb.getText().toString().equals("")) && !(inputLoca.getText().toString().equals(""))
                        && !(inputDate.getText().toString().equals("")) && !(inputDate.getText().toString().equals("")) && !(inputDate.getText().toString().equals("")) && !(inputDesc.getText().toString().equals("")))
                {

                    //if not testing, save to server
                    if (!test) {

                        new CreateNewSite().execute();

                    } else {
                        System.out.println("Site: " + site.toString());
                        // just go to next activity
                        CharSequence toastMessage = "Creating New Site...";
                        Toast toast = Toast.makeText(siteLayout.getContext(), toastMessage, Toast.LENGTH_LONG);
                        toast.show();

                    }

                    //no longer need alert, getting rid of
                    alert = null;
                }
                else //If user didn't fill out all fields, tell them they have to
                {
                    Toast.makeText(siteLayout.getContext(), "You must fill out every field before saving", Toast.LENGTH_SHORT).show();
                    showDialog(site); //bring up dialog again
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

    //Will format a year/month/day combination (from create site) to correct format
    private String toDate(int year, int month, int day)
    {
        String m=month + "", d=day + "";
        System.out.println("Converting to date! " + year + " " + m + " " + d);

        //If an incorrect date
        if(year<1 || month<1 || day<1)
        {
            return "0000-00-00 00:00:00";
        }
        else {
            if (month < 10)
            {
                m = "0" + month; //all must be two digits
            }
            if (day < 10)
            {
                d = "0" + day; //all must be two digits
            }
            return year + "-" + m + "-" + d + " 00:00:00";
        }
    }

    //parsing from correct format to displayed format
    private int[] fromDate(String date)
    {
        System.out.println("Converting " + date + " from date");
        int[] ymd = new int[3];
        date.replace(" 00:00:00", ""); //get rid of this part, don't need it
        int i=0;
        try {
            while (i < 3) {
                ymd[i] = Integer.parseInt(date.split("-")[i]); //split the date by the dashes to get year month and date
                i++;
            }
        }catch(NumberFormatException e)
        {
            System.out.println("Error: date not valid");//If an error, everything is zero
            ymd[0]=0;
            ymd[1]=0;
            ymd[2]=0;
        }
        System.out.println(ymd[0] + " " + ymd[1] + " " + ymd[2]);
        return ymd;
    }
}