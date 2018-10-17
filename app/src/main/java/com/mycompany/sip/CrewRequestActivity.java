package com.mycompany.sip;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_SITENAME;
import static com.mycompany.sip.Global.TAG_UNITNAME;

public class CrewRequestActivity extends ListActivity {

    boolean isActive = true;

    ArrayList<HashMap<String, String>> requests = new ArrayList<>();
    ArrayList<Unit> units = new ArrayList<>();

    AlertDialog.Builder alert;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

    Spinner availableUnits;
    SpinnerAdapter unitsAdapter;

    ListView requestList;

    Site site;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_request);
        fbh.updateCrewRequestActivity(this);

        TextView title = findViewById(R.id.requestTitle);
        title.setText("Crew Requests");

        requestList = getListView();

        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra(TAG_SITENAME);
        fbh.getRequests(site);
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void loadRequests(ArrayList<HashMap<String, String>> r)
    {
        for(int i = 0; i<r.size(); i++)
        {
            if(requests.indexOf(r.get(i)) < 0)
            {
                requests.add(r.get(i));
            }
            else
            {
                requests.set(requests.indexOf(r.get(i)), r.get(i));
            }
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        CrewRequestActivity.this, requests,
                        R.layout.list_item_buttons, new String[] { "Site",
                        "Name", "Uid", "Email"},
                        new int[] { R.id.pid, R.id.name, R.id.su, R.id.email }); //listview entries will contain unit's id and name

                // updating listview
                setListAdapter(adapter);
            }
        });
    }

    public void showDialog(View view)
    {
        TextView name = findViewById(R.id.name);
        TextView uid = findViewById(R.id.su);
        TextView email = findViewById(R.id.email);

        final String requestName = name.getText().toString();
        final String requestEmail = email.getText().toString();
        final String requestUid = uid.getText().toString();

        LayoutInflater inflater = getLayoutInflater();
        final View acceptRequest = inflater.inflate(R.layout.accept_request_dialog, null);

        TextView approveTitle = acceptRequest.findViewById(R.id.acceptRequestTitle);
        approveTitle.setText("Assign " + requestName + " a permission on site " + site + " ?");

        final Spinner choosePermission = acceptRequest.findViewById(R.id.permissionChoose);
        final ArrayList<String> permissions = new ArrayList<>();
        permissions.add("excavator");
        permissions.add("director");

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        permissions); //selected item will look like a spinner set from XML
        choosePermission.setAdapter(spinnerArrayAdapter);

        availableUnits = acceptRequest.findViewById(R.id.unitChoose);

        choosePermission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(permissions.get(position).equals("excavator"))
                {
                    availableUnits.setVisibility(View.VISIBLE);
                    fbh.getUnitsFromSite(site);
                }
                else
                {
                    availableUnits.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                availableUnits.setVisibility(View.INVISIBLE);
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(CrewRequestActivity.this);
        }

        alert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fbh.addPermission(requestUid, availableUnits.getVisibility() == View.VISIBLE && availableUnits.getSelectedItem() != null ? ((HashMap<String, String>) availableUnits.getSelectedItem()).get("UnitID") : "", requestName, requestEmail);
                deleteRequest(requestUid);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert = null;
            }
        });

        alert.setView(acceptRequest);
        AlertDialog dialog = alert.create();
        dialog.show();
    }
    //create pop up to accept or deny a request

    public void denyRequest(View view)
    {
        TextView uid = findViewById(R.id.su);
        final String requestUid = uid.getText().toString();

        LayoutInflater inflater = getLayoutInflater();
        final View cancelLayout = inflater.inflate(R.layout.deny_request_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(CrewRequestActivity.this);
        }
        alert.setTitle("Deny Request?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteRequest(requestUid);
                alert=null;
            }

        });
        alert.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Go back
                alert=null;
            }
        });
        alert.setView(cancelLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void deleteRequest(String requestUid)
    {
        fbh.deleteRequest(requestUid);
        for(HashMap<String, String> oldReq : requests) {
            if(oldReq.get("Uid").equals(requestUid)) {
                requests.remove(requests.indexOf(oldReq));
            }
        }
        ListAdapter adapter = new SimpleAdapter(
                CrewRequestActivity.this, requests,
                R.layout.list_item_buttons, new String[] { "Site",
                "Name", "Uid", "Email"},
                new int[] { R.id.pid, R.id.name, R.id.su, R.id.email }); //listview entries will contain unit's id and name

        // updating listview
        setListAdapter(adapter);
        ((BaseAdapter) adapter).notifyDataSetChanged();
    }

    public void loadUnits(ArrayList<Unit> u)
    {
        for(int i = 0; i<u.size(); i++)
        {
            if(units.indexOf(u.get(i)) < 0)
            {
                units.add(u.get(i));
            }
            else
            {
                units.set(units.indexOf(u.get(i)), u.get(i));
            }
        }

        final ArrayList<HashMap<String, String>> unitsList = new ArrayList<>();

        for(int i = 0; i<units.size(); i++)
        {
            HashMap<String, String> temp = new HashMap<>();
            temp.put("UnitID", units.get(i).getID());
            temp.put("Datum", units.get(i).getDatum());

            unitsList.add(temp);
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                unitsAdapter = new SimpleAdapter(
                        CrewRequestActivity.this, unitsList,
                        R.layout.list_item, new String[] { "UnitID",
                        "Datum"},
                        new int[] { R.id.pid, R.id.name}); //listview entries will contain unit's id and name

                // updating listview
                availableUnits.setAdapter(unitsAdapter);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_OK);
        finish();
    }
}
