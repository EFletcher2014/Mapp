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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_SITENAME;
import static com.mycompany.sip.Global.TAG_UNITNAME;

public class CrewActivity extends AppCompatActivity {

    public static boolean isActive = true;

    FirebaseHandler fbh = FirebaseHandler.getInstance();
    FirebaseAuth fbA = FirebaseAuth.getInstance();

    ArrayList<Unit> units = new ArrayList<>();

    ListView excavatorsLV;
    ListView directorsLV;

    private AlertDialog.Builder alert;

    private Site site;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew);
        fbh.updateCrewActivity(this);
        TextView title = findViewById(R.id.crew);
        TextView code = findViewById(R.id.code);
        title.setText("Crew");

        directorsLV = findViewById(R.id.directorList);
        excavatorsLV = findViewById(R.id.excavatorList);

        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra(TAG_SITENAME);
        fbh.getCrew(site);
        fbh.getUnitsFromSite(site);
        code.setText(site.getID());
    }

    public void listCrew()
    {
        ArrayList<HashMap<String, String>> excavatorsList = new ArrayList<>();
        ArrayList<HashMap<String, String>> directorsList = new ArrayList<>();
        Bundle siteRoles = site.getRoles();
        Bundle siteCrew = site.getCrew();
        Object[] users = siteRoles.keySet().toArray();
        for(int i = 0; i<users.length; i++)
        {
            HashMap<String, String> map = new HashMap<>();

            map.put("role", siteCrew.get(users[i].toString()+"NAME").toString());
            map.put(TAG_PID, users[i].toString());
            if(((ArrayList<String>) siteRoles.get(users[i].toString())).contains("excavator")) {
                String unitID = ((ArrayList<String>) siteRoles.get(users[i].toString())).get(1);
                Unit temp = new Unit(site, unitID, -1, -1, -1, -1, "", "");
                map.put("unit", temp.toString());
                excavatorsList.add(map);
            }
            else
            {
                if(((ArrayList<String>) siteRoles.get(users[i].toString())).contains("director"))
                {
                    directorsList.add(map);
                }
            }
        }

        ListAdapter excavatorAdapter = new SimpleAdapter(
                CrewActivity.this, excavatorsList,
                R.layout.list_item_two, new String[] { TAG_PID,
                "role", "unit"},
                new int[] { R.id.pid, R.id.name, R.id.unit }); //listview entries will contain unit's id and name

        // updating listview
        excavatorsLV.setAdapter(excavatorAdapter);

        ListAdapter directorAdapter = new SimpleAdapter(
                CrewActivity.this, directorsList,
                R.layout.list_item_with_delete, new String[] { TAG_PID,
                "role"},
                new int[] { R.id.pid, R.id.name }); //listview entries will contain unit's id and name

        // updating listview
        directorsLV.setAdapter(directorAdapter);

        ((BaseAdapter) directorAdapter).notifyDataSetChanged();
        ((BaseAdapter) excavatorAdapter).notifyDataSetChanged();
    }

    public void addCrewMembers(ArrayList<String[]> c)
    {
        for(int i = 0; i<c.size(); i++)
        {
            String[] info = c.get(i);
            site.addCrewMember(info[0], info[1], info[2]);
        }

        listCrew();

    }

    public void updateRoles(HashMap<String, ArrayList> roles)
    {
        Object[] rolesKeys = roles.keySet().toArray();
        for(int i = 0; i<roles.size(); i++)
        {
            if(!site.getRoles().containsKey(rolesKeys[i].toString()))
            {
                HashMap<String, ArrayList> temp = new HashMap<>();
                temp.put(rolesKeys[i].toString(), roles.get(rolesKeys[i].toString()));
                site.addRoles(temp);
            }
        }
    }

    public void loadUnits(ArrayList<Unit> u)
    {
        for(int i = 0; i<u.size(); i++)
        {
            if(!units.contains(u.get(i)))
            {
                units.add(u.get(i));
            }
            else
            {
                units.set(units.indexOf(u.get(i)), u.get(i));
            }

        }
    }

    public void goToRequests(View view)
    {
        Intent requestsIntent = new Intent(view.getContext(), CrewRequestActivity.class);
        requestsIntent.putExtra(TAG_SITENAME, site);
        startActivityForResult(requestsIntent, 226);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.isActive = true;
        // Check which request we're responding to
        if (requestCode == 226) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                fbh.getSiteRoles();
                fbh.getCrew(site);
            }
        }
    }

    public void deleteCrewMember(View view)
    {
        final String uidToDelete = ((TextView) ((View) view.getParent()).findViewById(R.id.pid)).getText().toString();

        ArrayList<String> director = new ArrayList<>();
        director.add("director");

        if(!uidToDelete.equals(fbA.getCurrentUser().getUid())) {
            String nameToDelete = ((TextView) ((View) view.getParent()).findViewById(R.id.name)).getText().toString();

            LayoutInflater inflater = getLayoutInflater();
            final View editLevelLayout = inflater.inflate(R.layout.edit_level_dialog, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
            } else {
                alert = new AlertDialog.Builder(CrewActivity.this);
            }
            TextView message = editLevelLayout.findViewById(R.id.alertMessage);
            message.setTextSize(24);
            message.setText("Are you sure you want to remove " + nameToDelete + " from the site?");

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                Bundle roles = site.getRoles();
                roles.remove(uidToDelete);

                fbh.updateRoles(uidToDelete, roles);
                listCrew();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    alert = null;
                    //Go back
                }
            });
            alert.setView(editLevelLayout);
            AlertDialog dialog = alert.create();
            dialog.show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "You cannot delete yourself from this site", Toast.LENGTH_SHORT).show();
        }
    }
}
