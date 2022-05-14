//A class to pull all sites from a MySQL server and display them in a listview
//From tutorial on androidhive accessed 7/30/17
//Author: Emily Fletcher
//TODO: Figure out if I need an identical activity for all sites/units/levels and a create new for all three
//TODO: or if I can just reuse the same ones
package com.mycompany.sip;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;

import static com.mycompany.sip.Global.*;

public class AllSitesActivity extends ListActivity {

    public static boolean isActive = true;

    FirebaseHandler fbh = FirebaseHandler.getInstance();

    //ArrayList to store all sites on the server for display
    ArrayList<Site> allSites = new ArrayList<Site>();

    //Boolean for testing
    boolean test=false;

    //Array of fake test sites, to be used if test==true
    //TODO: Make this an arraylist so that even in testing sites can be added and deleted [FIGURE OUT HOW TO DELETE SITES]

    //HashMap to be passed to ListView, contains site's name and id
    ArrayList<HashMap<String, String>> sitesList;

    private static Site site;

    //EditTexts for the dialog to create a new site
    private EditText inputName;
    private EditText inputDesc;
    //private DatePicker inputDate;
    private EditText inputDate;
    private EditText inputMonth;
    private EditText inputYear;
    private EditText inputNumb;
    private EditText inputLo;
    private EditText inputLa;



    // sites JSONArray
    JSONArray sites = null;

    //create alert to create a new site
    AlertDialog.Builder alert;
    AlertDialog.Builder requestAlert;

    private EditText siteCode;

    @Override
    public void onStart()
    {
        super.onStart();
        isActive = true;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        isActive = false;
    }

    public boolean isActive()
    {
        return isActive;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbh.updateAllSitesActivity(this);

            //This statement is true if the user has rotated the screen during this activity
            if(savedInstanceState!=null)
            {
                //Determines if the dialog to create a new site was open before the user rotated the screen
                if(savedInstanceState.getBoolean("alert"))
                {
                    final String sName = savedInstanceState.getString("Site Name");
                    final String sDesc = savedInstanceState.getString("Description");
                    final String sDay = (!savedInstanceState.getString("Day").equals("") ? savedInstanceState.getString("Day") : "0");
                    final String sMonth = (!savedInstanceState.getString("Month").equals("") ? savedInstanceState.getString("Month") : "0");
                    final String sYear = (!savedInstanceState.getString("Year").equals("") ? savedInstanceState.getString("Year") : "0");
                    final String sNumb = savedInstanceState.getString("Site Number");
                    final String sLo = (!savedInstanceState.getString("Longitude").equals("") ? savedInstanceState.getString("Longitude") : "0");
                    final String sLa = (!savedInstanceState.getString("Latitude").equals("") ? savedInstanceState.getString("Latitude") : "0");



                    //Reopens dialog to create a site with existing inputs
                    showDialog(new Site("", sName, sNumb, sDesc, toDate(Integer.parseInt(sYear), Integer.parseInt(sMonth), Integer.parseInt(sDay)), Double.parseDouble(sLa), Double.parseDouble(sLo), null));
                }

                if(savedInstanceState.getBoolean("requestAlert"))
                {
                    showRequestDialog(savedInstanceState.getString("siteCode"));
                }
            }
            setContentView(R.layout.activity_get_all_sites);


        fbh.getSitesListener();

            // Hashmap for ListView
            sitesList = new ArrayList<HashMap<String, String>>();

            // Get listview
            ListView lv = getListView();

            // on selecting single site
            // launching Edit site Screen
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // getting values from selected ListItem
                    String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                    String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                            .toString();//Gets id

                    Intent in;
                    fbh.siteSelected(allSites.get(allSites.indexOf(new Site(pid, "", "", "", "", 0.0, 0.0, null))));
                    if(fbh.userHasWritePermission(allSites.get(allSites.indexOf(new Site(pid, "", "", "", "", 0.0, 0.0, null)))))
                    {
                        in = new Intent(view.getContext(), SiteActivity.class);
                    }
                    else
                    {
                        // Starting new intent
                        in = new Intent(view.getContext(), AllUnitsActivity.class);
                    }

                    // sending id and site to list units activity
                    in.putExtra(TAG_PID, pid);
                    in.putExtra(TAG_SITENAME, allSites.get(allSites.indexOf(new Site(pid, "", "", "", "", 0.0, 0.0, null))));

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

    public void logout(View view)
    {
        FirebaseAuth.getInstance().signOut();

        Intent in = new Intent(AllSitesActivity.this, LoginActivity.class);
        startActivity(in);
    }

    //Called by FirebaseHandler to update sites list
    public void loadSites(ArrayList<Site> newSites) {

        //Adds sites passed from FirebaseHandler
        for (int i = 0; i < newSites.size(); i++) {
            Site temp = newSites.get(i);
            int index = allSites.indexOf(temp);
            if (index < 0) {
                allSites.add(temp);
            } else {
                allSites.set(index, temp);
            }
        }

        //ArrayList to hold hash maps of site names and ids
        sitesList = new ArrayList<HashMap<String, String>>();

        //Loops through all sites to add them to the arraylist
        for(int i = 0; i<allSites.size(); i++)
        {
            Site temp = allSites.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_SITENAME, temp.getName());

            // adding HashList to ArrayList
            sitesList.add(map);
        }

        //Adding arraylist to list adapter
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        AllSitesActivity.this, sitesList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_SITENAME},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain site's id and name

                // updating listview
                setListAdapter(adapter);
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
            outState.putString("Latitude", inputLa.getText().toString());
            outState.putString("Longitude", inputLo.getText().toString());

        }
        else
        {
            if(requestAlert != null)
            {
                outState.putBoolean("requestAlert", true);
                outState.putString("siteCode", siteCode.getText().toString());
            }
            else {
                //tells the new activity that there isn't a dialog active. Might not be necessary
                outState.putBoolean("alert", false);
            }
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
        inputLa = (EditText) siteLayout.findViewById(R.id.inputLat);
        inputLo = (EditText) siteLayout.findViewById(R.id.inputLong);

        //If screen was rotated and recreating an existing dialog
        if(st!=null)
        {
            //Populate with user's previous input
            inputName.setText(st.getName());
            inputDesc.setText(st.getDescription());

            //TODO: figure out if I want to change so that user can enter a partial date
            //Parsing the date from the saved format to the displayed format
            if(st.getDateOpened() != null) {
                int[] date = fromDate(st.getDateOpened());
                String y = date[0] + "", m = date[1] + "", d = date[2] + "";
                if (date[0] != 0)
                    inputYear.setText(y);
                if (date[1] != 0)
                    inputMonth.setText(m);
                if (date[2] != 0)
                    inputDate.setText(d);
            }

            inputNumb.setText(st.getNumber());
            inputLa.setText(st.getDatum().latitude + "");
            inputLo.setText(st.getDatum().longitude + "");
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
            site = new Site("", inputName.getText().toString(), inputNumb.getText().toString(),
                    inputDesc.getText().toString(), toDate(y, m, d), Double.parseDouble(inputLa.getText().toString()), Double.parseDouble(inputLo.getText().toString()), null);

            //If all fields are filled out
            if(!(inputName.getText().toString().equals("")) && !(inputDesc.getText().toString().equals(""))
                    && !(inputNumb.getText().toString().equals("")) && !(inputLa.getText().toString().equals(""))
                    && !(inputLo.getText().toString().equals("")) && !(inputDate.getText().toString().equals(""))
                    && !(inputDate.getText().toString().equals("")) && !(inputDate.getText().toString().equals(""))
                    && !(inputDesc.getText().toString().equals("")))
            {
                //save to Firebase
                fbh.createSite(site);

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
        int[] ymd = new int[3];
        date.replace(" 00:00:00", ""); //get rid of this part, don't need it
        int i = 0;
        try {
            while (i < 3) {
                ymd[i] = Integer.parseInt(date.split("-")[i]); //split the date by the dashes to get year month and date
                i++;
            }
        } catch (NumberFormatException e) {
            ymd[0] = 0;
            ymd[1] = 0;
            ymd[2] = 0;
        }
        return ymd;
    }

    public void generateRequestDialog(View view)
    {
        showRequestDialog("");
    }

    public void showRequestDialog(String code)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View requestLayout = inflater.inflate(R.layout.request_site_dialog, null);

        siteCode = requestLayout.findViewById(R.id.siteCode);
        siteCode.setText(code);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            requestAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            requestAlert = new AlertDialog.Builder(AllSitesActivity.this);
        }

        requestAlert.setTitle("Request access to a site");
        requestAlert.setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                fbh.createRequest(siteCode.getText().toString());
            }
        });
        requestAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Go back
                requestAlert=null;
            }
        });
        // this is set the view from XML inside AlertDialog
        requestAlert.setView(requestLayout);
        AlertDialog dialog = requestAlert.create();
        dialog.show();
    }
}