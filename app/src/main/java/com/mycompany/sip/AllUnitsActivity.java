//All from androidhive 7/30/17
package com.mycompany.sip;

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
import com.google.firebase.firestore.FirebaseFirestore;
import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_UNITNAME;

public class AllUnitsActivity extends ListActivity {

    public static boolean isActive;

    // Progress Dialog
    private ProgressDialog pDialog;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

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
    private String siteID;
    private boolean unitsExist = false;

    boolean test=false;
    ArrayList<Unit> allUnits = new ArrayList<>();
    JSONArray units = null;


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
        fbh.updateUnitActivity(this);
        setContentView(R.layout.activity_get_all_units);

        if(savedInstanceState!=null && savedInstanceState.getBoolean("alert"))
        {
            final String coords = savedInstanceState.getString("Datum Coordinate");
            final String excs = savedInstanceState.getString("Excavators");
            final String date = savedInstanceState.getString("Date Opened");
            final String reas = savedInstanceState.getString("Reason");
            final String  nsd = savedInstanceState.getString("NSDim");
            final String ewd = savedInstanceState.getString("EWDim");

            //TODO: get datum in a different way
            //TODO: get excavators
            showDialog(new Unit(site, "", Integer.parseInt(coords.substring(1, 2)),
                    Integer.parseInt(coords.substring(4, 5)), Integer.parseInt(nsd), Integer.parseInt(ewd), date, reas));
        }

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        siteID = openIntent.getStringExtra("PrimaryKey");
        site = openIntent.getParcelableExtra("siteName");

        //notify firebase that a site has been selected so it can save all the data for it
        fbh.siteSelected(site);

        //for title bar
        TextView siteNameText = (TextView) findViewById(R.id.siteName);
        siteNameText.setText(site.getName() + " Units");

        // Hashmap for ListView
        unitsList = new ArrayList<HashMap<String, String>>();

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

                // sending id, sitename, and unit to next activity
                in.putExtra(TAG_PID, pid);
                in.putExtra("siteName", site);
                in.putExtra(TAG_UNITNAME, allUnits.get(allUnits.indexOf(new Unit(null, pid, 0, 0, 0, 0, "", ""))));

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

        //on clicking new unit button
        //launching new unit dialog
        Button newUnit = (Button) findViewById(R.id.newUnitBtn);

        if(!fbh.userHasWritePermission(site))
        {
            newUnit.setVisibility(View.INVISIBLE);
        }
        else
        {
            newUnit.setVisibility(View.VISIBLE);

            newUnit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showDialog(null);
                }

            });
        }
    }

    //method called by FirebaseHandler to populate listview
    public void loadUnits(ArrayList<Unit> newUnits)
    {
        //adding new units passed from FirebaseHandler
        for (int i = 0; i < newUnits.size(); i++) {
            Unit temp = newUnits.get(i);
            int index = allUnits.indexOf(temp);
            if (index < 0) {
                allUnits.add(temp);
            } else {
                allUnits.set(index, temp);
            }
        }

        //ArrayList containing unit datum and id to populate listview
        unitsList = new ArrayList<HashMap<String, String>>();

        //Looping through all units to add them to listview
        for(int i = 0; i < allUnits.size(); i++)
        {
            Unit temp = allUnits.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_UNITNAME, temp.getDatum());

            // adding HashList to ArrayList
            unitsList.add(map);
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        AllUnitsActivity.this, unitsList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_UNITNAME},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain unit's id and name

                // updating listview
                setListAdapter(adapter);
            }
        });
    }

    //TODO: is this necessary? Never get response from any activities
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

    //TODO: fix layout to show data needed for firesource
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
            //TODO: get coordinates in a better way
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

            //TODO: get excavators once users are added
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

                //TODO: add excavators
                unit = new Unit(site, "", Integer.parseInt(inputCoords.getText().toString().substring(1, 3)),
                        Integer.parseInt(inputCoords.getText().toString().substring(4)),
                        Integer.parseInt(inputNSDims.getText().toString()),
                        Integer.parseInt(inputEWDims.getText().toString()),
                        toDate(y, m, d), inputReas.getText().toString());

                if(!(inputCoords.getText().toString().equals("")) && !(inputYear.getText().toString().equals("")) && !(inputMonth.getText().toString().equals(""))
                        && !(inputDate.getText().toString().equals("")) && !(inputNSDims.getText().toString().equals(""))
                        && !(inputEWDims.getText().toString().equals("")) /*&& !(inputExcs.getText().toString().equals(""))*/ && !(inputReas.getText().toString().equals(""))) {

                    fbh.createUnit(unit);
                    alert = null;
                }
                else
                {
                    //user must fill out all necessary fields
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
            ymd[0]=0;
            ymd[1]=0;
            ymd[2]=0;
        }

        return ymd;
    }
}