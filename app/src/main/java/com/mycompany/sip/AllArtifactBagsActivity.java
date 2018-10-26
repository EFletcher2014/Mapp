//All from androidhive 7/30/17
package com.mycompany.sip;

import java.util.ArrayList;
import java.util.HashMap;

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

import static com.mycompany.sip.Global.*;

public class AllArtifactBagsActivity extends ListActivity {

    public static boolean isActive;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

    ArrayList<HashMap<String, String>> artifactBagsList;

    private static Site site;
    private static Unit unit;
    private static Level level;
    private static ArtifactBag artifactBag;

    private AlertDialog.Builder alert;
    private EditText accNum;
    private EditText catNum;
    private EditText contents;
    private String pid;
    private String depth;
    ArrayList<ArtifactBag> allArtifactBags = new ArrayList<>();

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
        setContentView(R.layout.activity_all_artifacts);
        fbh.updateArtifactBagActivity(this);

        // Hashmap for ListView
        artifactBagsList = new ArrayList<HashMap<String, String>>();

        //added by Emily Fletcher 8/29/17
        Intent openIntent = getIntent();
        //foreignKey = openIntent.getIntExtra("PrimaryKey", -1); TODO: is this still passed? Should it be?
        site = openIntent.getParcelableExtra("name");
        unit = openIntent.getParcelableExtra("datum");
        level = openIntent.getParcelableExtra("depth");
        TextView titleText = (TextView) findViewById(R.id.artifactsLabel);
        String title = site.getName() + " " + unit.getDatum() + " Level " + level.getNumber() + " Artifacts";
        titleText.setText(title);

        fbh.getArtifactBagsFromLevel(level);

        // Get listview
        ListView lv = getListView();

        //if a dialog was up when the screen rotated, populate that dialog with the users' inputs
        if(savedInstanceState!=null)
        {
            if(savedInstanceState.getBoolean("alert"))
            {
                artifactBag = new ArtifactBag(site, unit, level, savedInstanceState.get("ID").toString(), savedInstanceState.get("AccNum").toString(), Integer.parseInt(savedInstanceState.get("CatNum").toString()), savedInstanceState.get("Contents").toString());
                showDialog(artifactBag);
            }
        }

        // on selecting single artifact
        // launching Edit artifact dialog
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                depth = ((TextView) view.findViewById(R.id.name)).getText()
                    .toString();
                pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                artifactBag = allArtifactBags.get(allArtifactBags.indexOf(new ArtifactBag(site, unit, level, pid, "", -1, "" )));
                showDialog(artifactBag);

                //TODO: accNum can probably auto-fill, save with site?

            }
        });

        //on clicking new ArtifactBag button
        //launching new artifact bag dialog
       Button newArtifactBag = (Button) findViewById(R.id.newArtifactBtn);

       if(!fbh.userIsExcavator(unit))
       {
           newArtifactBag.setVisibility(View.INVISIBLE);
       }
       else
       {
           newArtifactBag.setVisibility(View.VISIBLE);

           newArtifactBag.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   artifactBag = new ArtifactBag(site, unit, level, "", "", -1, "");
                   showDialog(artifactBag);
               }
           });
       }

    }

    //method called by FirebaseHandler to populate listview
    public void loadArtifactBags(ArrayList<ArtifactBag> newABags)
    {
        //adding new artifact bags passed from FirebaseHandler
        for (int i = 0; i < newABags.size(); i++) {
            ArtifactBag temp = newABags.get(i);
            int index = allArtifactBags.indexOf(temp);
            if (index < 0) {
                allArtifactBags.add(temp);
            } else {
                allArtifactBags.set(index, temp);
            }
        }

        //ArrayList containing artifact bag info and id to populate listview
        artifactBagsList = new ArrayList<HashMap<String, String>>();

        //Looping through all artifact bags to add them to listview
        for(int i = 0; i < allArtifactBags.size(); i++)
        {
            ArtifactBag temp = allArtifactBags.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_UNITNAME, temp.toString());

            // adding HashList to ArrayList
            artifactBagsList.add(map);
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        AllArtifactBagsActivity.this, artifactBagsList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_UNITNAME},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain unit's id and name

                // updating listview
                setListAdapter(adapter);
            }
        });
    }

    public Level getLevel()
    {
        return level;
    }

    //TODO: is this necessary
    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted level
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
            outState.putBoolean("alert", true);
            outState.putString("ID", artifactBag == null ? "" : artifactBag.getID());
            outState.putString("AccNum", accNum.getText().toString());
            try {
                outState.putInt("CatNum", Integer.parseInt(catNum.getText().toString()));
            }catch(NumberFormatException e)
            {
                outState.putInt("CatNum", -1);
            }
            outState.putString("Contents", contents.getText().toString());

        }
        else
        {
            outState.putBoolean("alert", false);
        }
    }
    private void showDialog(final ArtifactBag art)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View artifactLayout = inflater.inflate(R.layout.new_artifact_bag_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(AllArtifactBagsActivity.this);
        }
        accNum = (EditText) artifactLayout.findViewById(R.id.accNum);
        catNum = (EditText) artifactLayout.findViewById(R.id.catNum);
        contents = (EditText) artifactLayout.findViewById(R.id.contents);

        if(art!=null)
        {
            accNum.setText(art.getAccessionNumber());
            if(art.getCatalogNumber() > -1) {
                String cnum = art.getCatalogNumber() + "";
                catNum.setText(cnum);
            }
            contents.setText(art.getContents());
        }
        alert.setTitle("Edit ArtifactBag");
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                int c = 0;
                try
                {
                    c=Integer.parseInt(catNum.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    c=0;
                }

                if(art != null) {
                    art.setAccessionNumber(accNum.getText().toString());
                    art.setCatalogNumber(c);
                    art.setContents(contents.getText().toString());
                }

                if(!(accNum.getText().toString().equals("")) && !(catNum.getText().toString().equals("")) && !(contents.getText().toString().equals("")))
                {
                    fbh.createArtifactBag(art);
                    alert=null;
                }
                else
                {
                    Toast.makeText(artifactLayout.getContext(), "You must fill out all fields before saving", Toast.LENGTH_SHORT).show();
                    showDialog(artifactBag);
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
        alert.setView(artifactLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }
}