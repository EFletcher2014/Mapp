//All from androidhive 7/30/17
package com.mycompany.sip;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import static com.mycompany.sip.Global.*;

public class AllLevelsActivity extends ListActivity {

    public static boolean isActive;

    // Progress Dialog
    private ProgressDialog pDialog;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

    ArrayList<HashMap<String, String>> levelsList;

    private static Site site;
    private static Unit unit;
    private static Level level;

    private AlertDialog.Builder alert;
    ArrayList<Level> allLevels = new ArrayList<>();

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
        setContentView(R.layout.activity_all_levels);
        fbh.updateLevelActivity(this);

        if(savedInstanceState!=null)
        {
            if(savedInstanceState.getBoolean("alert"))
            {
                showDialog((Level) savedInstanceState.getParcelable("level"));
            }
        }

        // Hashmap for ListView
        levelsList = new ArrayList<HashMap<String, String>>();

        //added by Emily Fletcher 8/27/17
        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra(TAG_SITENAME);
        unit = openIntent.getParcelableExtra(TAG_UNITNAME);
        TextView titleText = (TextView) findViewById(R.id.siteNameUnitNumber);
        String title = site.getName() + " " + unit.getDatum() + " Levels";
        titleText.setText(title);

        fbh.getLevelsFromUnit(unit);

        // Get listview
        ListView lv = getListView();

        //TODO: make this do something else (but what??). Fix in other activities
        // on seleting single level
        // launching Edit level Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                level = allLevels.get(allLevels.indexOf(new Level(site, unit, pid, 0, 0.0, 0.0, "", "", null)));

                if(!fbh.userIsExcavator(unit))
                {
                    Intent in = new Intent(AllLevelsActivity.this,
                            LevelDocument.class);
                    in.putExtra("depth", level);

                    // starting new activity and expecting some response back
                    startActivityForResult(in, 100);
                }
                else {
                    showDialog(level);
                }
            }
        });

        //on clicking new Level button
        //launching new level activity
        Button newLevel = (Button) findViewById(R.id.newLevelBtn);

        if(!fbh.userIsExcavator(unit))
        {
            newLevel.setVisibility(View.INVISIBLE);
        }
        else
        {
            newLevel.setVisibility(View.VISIBLE);

            newLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Launch Add New product Activity
                    Intent i = new Intent(view.getContext(),
                            LevelDocument.class);
                    i.putExtra("depth", new Level(site, unit, null, allLevels.size()+1, 0.0, 0.0, "", "", null));

                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(i, 333);
                }
            });
        }

    }

    public Unit getUnit()
    {
        return unit;
    }


    //Method called from FirebaseHandler to populate listview
    public void loadLevels(ArrayList<Level> newLevels)
    {
        //adding new levels from Firebase
        for(int i = 0; i < newLevels.size(); i++)
        {
            Level temp = newLevels.get(i);
            int index = allLevels.indexOf(temp);
            if (index < 0) {
                allLevels.add(temp);
            } else {
                allLevels.set(index, temp);
            }
        }

        //populating listview with ID and name of all levels
        levelsList = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i<allLevels.size(); i++)
        {
            Level temp = allLevels.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_LVLNUM, temp.toString());

            // adding HashList to ArrayList
            levelsList.add(map);
        }

        //actually populating the listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        AllLevelsActivity.this, levelsList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_LVLNUM},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain unit's pk and name

                // updating listview
                setListAdapter(adapter);
            }
        });
    }


    // Response from LevelDocument Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if result code 100
        if (resultCode == RESULT_OK) {
            // if result code 100 is received
            // means user edited/deleted level
            // reload this screen again

            //Gets all levels from Firebase
            //TODO: is this necessary? Shouldn't it update on its own?
            allLevels = new ArrayList<Level>();
            fbh.getLevelsFromUnit(unit);
        }

        if(resultCode == RESULT_CANCELED)//if user canceled without saving a new level
        {
            //do nothing, just stay on current empty list of levels
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
            outState.putBoolean("alert", true);
            outState.putParcelable("level", level);

        }
        else
        {
            outState.putBoolean("alert", false);
        }
    }

    public void showDialog(Level lvl)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View editLevelLayout = inflater.inflate(R.layout.edit_level_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(AllLevelsActivity.this);
        }
        alert.setTitle("Level " + lvl.toString());

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Pass intent
                // getting values from selected ListItem

                // Starting new intent
                //TODO: Will have to add call to server to pick the correct level sheet to edit
                //TODO: is this the right context?
                Intent in = new Intent(editLevelLayout.getContext(),
                        LevelDocument.class);
                in.putExtra("depth", level);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
                alert=null;
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                alert=null;
                //Go back
            }
        });
        alert.setView(editLevelLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }
}