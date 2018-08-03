//All from androidhive 7/30/17
package com.mycompany.sip;

import java.lang.reflect.Array;
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
import android.os.Build;
import android.os.Bundle;
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
import com.mycompany.sip.Global.*;

import static com.mycompany.sip.Global.TAG_DATEOPEN;
import static com.mycompany.sip.Global.TAG_EW;
import static com.mycompany.sip.Global.TAG_EXCS;
import static com.mycompany.sip.Global.TAG_NS;
import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_REAS;
import static com.mycompany.sip.Global.TAG_SUCCESS;
import static com.mycompany.sip.Global.TAG_UNITNAME;
import static com.mycompany.sip.Global.TAG_UNITS;
import static com.mycompany.sip.Global.url_all_units;
import static com.mycompany.sip.Global.url_create_unit;

public class AllUnitsActivity extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    LocalDatabaseHandler ldb = new LocalDatabaseHandler(this);
    RemoteDatabaseHandler rdb = new RemoteDatabaseHandler(this);

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    JSONParser jsonParser = new JSONParser();//TODO: figure out if I need both

    ArrayList<HashMap<String, String>> unitsList;

    private static Site site;
    private static Unit unit;

    private AlertDialog.Builder alert;
    private EditText inputCoords;
    private EditText inputExcs;
    private EditText inputYear;
    private EditText inputMonth;
    private EditText inputDate;
    private EditText inputReas;
    private EditText inputNSDims;
    private EditText inputEWDims;
    private int foreignKey;
    private boolean unitsExist = false;

    boolean test=false;
    ArrayList<Unit> allUnits = new ArrayList<>();
    Unit[] testUnits = {new Unit("N24W11", "07/21/17", "1", "2", site, "Emily Fletcher and Meghan Williams", "possible blacksmith quarters", 1, 1, null, null),
            new Unit("N23E9",  "07/21/17", "1", "2", site, "Emily Fletcher and Meghan Williams", "possible blacksmith quarters", 2, 2, null, null),
            new Unit("N24W6",  "07/21/17", "1", "2", site, "Emily Fletcher and Meghan Williams", "possible blacksmith quarters", 3, 3, null, null)};

    //units JSONArray
    JSONArray units = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_all_units);

        if(savedInstanceState!=null)
        {
            if(savedInstanceState.getBoolean("alert"))
            {
                final String coords = savedInstanceState.getString("Datum Coordinate");
                final String excs = savedInstanceState.getString("Excavators");
                final String date = savedInstanceState.getString("Date Opened");
                final String reas = savedInstanceState.getString("Reason");
                final String  nsd = savedInstanceState.getString("NSDim");
                final String ewd = savedInstanceState.getString("EWDim");

                showDialog(new Unit(coords, date, nsd, ewd, site, excs, reas, -1, -1, null, null));
            }
        }

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        foreignKey = openIntent.getIntExtra("PrimaryKey", -1);
        site = openIntent.getParcelableExtra("siteName");
        System.out.println("AllUnitsActivity received site: " + site + " " + site.getPk());
        TextView siteNameText = (TextView) findViewById(R.id.siteName);
        siteNameText.setText(site.getName() + " Units");

        // Hashmap for ListView
        unitsList = new ArrayList<HashMap<String, String>>();

        if (!test) {
            // Loading units in Background Thread
            new LoadAllUnits().execute();
        } else {
            // looping through All units
            for (int i = 0; i < 3; i++) {

                String datum = testUnits[i].getDatum();

                // creating new HashMap
                HashMap<String, String> testMap = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                testMap.put(TAG_PID, i + "");
                testMap.put(TAG_UNITNAME, datum);

                // adding HashList to ArrayList
                unitsList.add(testMap);
                System.out.println(unitsList);
            }
            ListAdapter adapter = new SimpleAdapter(
                    AllUnitsActivity.this, unitsList,
                    R.layout.list_item, new String[]{TAG_PID,
                    TAG_UNITNAME},
                    new int[]{R.id.pid, R.id.name});
            // updating listview
            setListAdapter(adapter);
        }

        // Get listview
        ListView lv = getListView();

        // on seleting single unit
        // launching Edit Unit Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText().toString();
                String datum = ((TextView) view.findViewById(R.id.name)).getText().toString();
                String su = ((TextView) view.findViewById(R.id.su)).getText().toString();

                // Starting new intent
                Intent in = new Intent(view.getContext(),
                        AllLevelsActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, Integer.parseInt(pid));
                in.putExtra("siteName", site);
                if(test)
                {
                    in.putExtra(TAG_UNITNAME, testUnits[Integer.parseInt(pid)]);
                }
                else
                {
                    in.putExtra(TAG_UNITNAME, allUnits.get(Integer.parseInt(su)));
                }

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
                showDialog(null);
            }

        });
    }

    // Response from Edit Unit Activity
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
     * Background Async Task to Load all units by making HTTP Request
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
            //new UpdateDBs(getApplicationContext()).execute();
        }

        /**
         * getting All units from url
         * */
        protected String doInBackground(String... args) {
            System.out.println("loading all units from site: " + site);
            allUnits = rdb.loadAllUnits(site, null);
            for(int i=0; i<allUnits.size(); i++)
            {
                Unit temp = allUnits.get(i);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put(TAG_PID, temp.getRemotePK() + "");
                System.out.println("Unit pk: " + temp.getRemotePK());
                map.put(TAG_UNITNAME, temp.getDatum());
                map.put("Site Unit", i + "");

                // adding HashList to ArrayList
                unitsList.add(map);
            }

//            // Building Parameters
//            HashMap params = new HashMap();
//
//            params.put("foreignKey", foreignKey);
//
//            // getting JSON string from URL
//            JSONObject json = jParser.makeHttpRequest(url_all_units, "GET", params);


//            try {
//                // Check your log cat for JSON reponse
//                Log.d("All units: ", json.toString());
//
//                try {
//                    // Checking for SUCCESS TAG
//                    int success = json.getInt(TAG_SUCCESS);
//
//                    if (success == 1) {
//                        // units found
//                        // Getting Array of units
//                        units = json.getJSONArray(TAG_UNITS);
//
//                        // looping through All units
//                        for (int i = 0; i < units.length(); i++) {
//                            JSONObject c = units.getJSONObject(i);
//
//                            // Storing each json item in variable
//                            String id = c.getString(TAG_PID);
//                            String name = c.getString(TAG_UNITNAME);
//                            String nsDim = c.getString(TAG_NS);
//                            String ewDim = c.getString(TAG_EW);
//                            String date = c.getString(TAG_DATEOPEN);
//                            String excs = c.getString(TAG_EXCS);
//                            String reas = c.getString(TAG_REAS);
//
//                            Unit temp = new Unit(name, date, nsDim, ewDim, site, excs, reas, Integer.parseInt(id));
//                            allUnits.add(temp);
//
//                            // creating new HashMap
//                            HashMap<String, String> map = new HashMap<String, String>();
//
//                            // adding each child node to HashMap key => value
//                            map.put(TAG_PID, id);
//                            map.put(TAG_UNITNAME, name);
//                            map.put("Site Unit", i + "");
//
//                            // adding HashList to ArrayList
//                            unitsList.add(map);
//
//                            //save to local database
//                            if (ldb.updateUnit(temp) == 0) {
//                                System.out.println("Adding new unit " + temp + " to SQLite DB");
//                                System.out.println(temp.getPk() + " " + ldb.getUnit(temp.getPk()));
//                                ldb.addUnit(temp);
//                            } else {
//                                System.out.println("Unit " + temp + " already exists and was updated");
//                            }
//                            System.out.println(ldb.getUnitsCount());
//                            System.out.println(ldb.getUnit(temp.getPk()) + " " + temp.getPk());
//                            System.out.println(ldb.getAllUnits().toString());
//                        }
//                    } else {
//                        //units don't exist
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }catch(NullPointerException e)
//            {
//                allUnits = (ArrayList) ldb.getAllUnitsFromSite(site.getPk());
//                for(int i=0; i<allUnits.size(); i++)
//                {
//                        Unit temp = allUnits.get(i);
//
//                        // creating new HashMap
//                        HashMap<String, String> map = new HashMap<String, String>();
//
//                        // adding each child node to HashMap key => value
//                        map.put(TAG_PID, temp.getPk() + "");
//                        map.put(TAG_UNITNAME, temp.getDatum());
//                        map.put("Site Unit", i + "");
//
//                        // adding HashList to ArrayList
//                        unitsList.add(map);
//                }
//            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all units
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    if(unitsList.size()!=0) {
                        ListAdapter adapter = new SimpleAdapter(
                                AllUnitsActivity.this, unitsList,
                                R.layout.list_item, new String[]{TAG_PID,
                                TAG_UNITNAME, "Site Unit"},
                                new int[]{R.id.pid, R.id.name, R.id.su});
                        // updating listview
                        setListAdapter(adapter);
                    }
                    else
                    {
                        showDialog(null);
                    }
                }
            });

        }

    }
        /**
         * Background Async Task to Create new unit
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
                //new UpdateDBs(getApplicationContext()).execute();
            }

            /**
             * Creating unit
             * */
            protected String doInBackground(String... args) {
                if (rdb.createNewUnit(unit)>-1) {
                    // successfully created unit
                    // closing this screen
                    finish();

                    //restarting activity so list will include new unit
                    startActivity(getIntent());
                } else {
                    // failed to create unit
                }
                /*// Building Parameters
                HashMap params = new HashMap();
                params.put("foreignKey", foreignKey);
                params.put("datum", unit.getDatum());
                params.put("nsDim", unit.getNsDimension());
                params.put("ewDim", unit.getEwDimension());
                params.put("excavators", unit.getExcavators());
                params.put("dateOpened", unit.getDateOpened());
                params.put("reasonForOpening", unit.getReasonForOpening());

                // getting JSON Object
                // Note that create site url accepts POST method
                JSONObject json = jsonParser.makeHttpRequest(url_create_unit,
                        "POST", params);

                // check log cat fro response
                try {
                    Log.d("Create Response", json.toString());

                    // check for success tag
                    try {
                        int success = json.getInt(TAG_SUCCESS);

                        if (success == 1) {
                            // successfully created unit
                            // closing this screen

                            finish();
                            startActivity(getIntent());
                        } else {
                            // failed to create unit
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }catch(NullPointerException e)
                {
                    ldb.addUnit(unit); //TODO: ldb's primary keys must be the same as the remote server's, but this one isn't there and won't be until the user connects
                                        //TODO: to the internet again. So what should we do? Let it default set for now and update it when we back up to remote server?
                                        //TODO: Then the ldb.update methods will have to be able to update PKs which I'm not sure is allowed...
                                        //TODO: Since both servers will have the same set of primary keys I guess we could just go with it and set the remote server's
                                        //TODO: when we're updating...But then we have to do more PHP stuff I think
                    // closing this screen
                    finish();

                    //restarting activity so list will include new site
                    startActivity(getIntent());
                }*/
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
            //TODO: replace these with unit parcelable?
            outState.putBoolean("alert", true);
            outState.putString("Datum Coordinate", inputCoords.getText().toString());
            outState.putString("Excavators", inputExcs.getText().toString());
            outState.putString("Date Opened", toDate(Integer.parseInt(inputYear.getText().toString()), Integer.parseInt(inputMonth.getText().toString()), Integer.parseInt(inputDate.getText().toString())));
            outState.putString("Reason", inputReas.getText().toString());
            outState.putString("NSDim", inputNSDims.getText().toString());
            outState.putString("EWDim", inputEWDims.getText().toString());

        }
        else
        {
            outState.putBoolean("alert", false);
        }
    }

    private void showDialog(Unit un)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View unitLayout = inflater.inflate(R.layout.new_unit_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(AllUnitsActivity.this);
        }
        inputCoords = (EditText) unitLayout.findViewById(R.id.inputCoords);
        inputExcs = (EditText) unitLayout.findViewById(R.id.inputExcs);
        inputYear = (EditText) unitLayout.findViewById(R.id.inputYear);
        inputMonth = (EditText) unitLayout.findViewById(R.id.inputMonth);
        inputDate = (EditText) unitLayout.findViewById(R.id.inputDate);
        inputReas = (EditText) unitLayout.findViewById(R.id.inputReas);
        inputNSDims = (EditText) unitLayout.findViewById(R.id.inputNSDims);
        inputEWDims = (EditText) unitLayout.findViewById(R.id.inputEWDims);

        if(un!=null)
        {
            inputCoords.setText(un.getDatum());
            inputExcs.setText(un.getExcavators());

            //TODO: figure out if I want to change so that user can enter a partial date
            int[] date = fromDate(un.getDateOpened());
            String y=date[0]+"", m=date[1]+"", d=date[2]+"";
            if(date[0]!=0)
                inputYear.setText(y);
            if(date[1]!=0)
                inputMonth.setText(m);
            if(date[2]!=0)
                inputDate.setText(d);
            inputReas.setText(un.getReasonForOpening());
            inputNSDims.setText(un.getNsDimension());
            inputEWDims.setText(un.getEwDimension());
        }
        alert.setTitle("Create A New Unit");
        alert.setPositiveButton("Create Unit", new DialogInterface.OnClickListener() {
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

                unit = new Unit(inputCoords.getText().toString(),
                        toDate(y, m, d), inputNSDims.getText().toString(),
                        inputEWDims.getText().toString(), site, inputExcs.getText().toString(),
                        inputReas.getText().toString(), -1, -1, null, null);

                if(!(inputCoords.getText().toString().equals("")) && !(inputYear.getText().toString().equals("")) && !(inputMonth.getText().toString().equals(""))
                        && !(inputDate.getText().toString().equals("")) && !(inputNSDims.getText().toString().equals(""))
                        && !(inputEWDims.getText().toString().equals("")) && !(inputExcs.getText().toString().equals("")) && !(inputReas.getText().toString().equals(""))) {

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
                    alert = null;
                }
                else
                {
                    Toast.makeText(unitLayout.getContext(), "You must fill out all fields before saving", Toast.LENGTH_SHORT).show();
                    showDialog(unit);
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
        alert.setView(unitLayout);
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