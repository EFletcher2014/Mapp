package com.mycompany.sip;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.TAG_FEATURE;
import static com.mycompany.sip.Global.TAG_PID;

public class AllFeaturesActivity extends ListActivity {

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

    ArrayList<Feature> allFeatures = new ArrayList<>();
    ArrayList<HashMap<String, String>> featuresList;
    private AlertDialog.Builder alert;

    EditText description;
    Site site;

    boolean isActive = true;

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
        setContentView(R.layout.activity_all_features);
        fbh.updateFeatureActivity(this);

        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra("siteName");

        TextView title = findViewById(R.id.siteName);
        title.setText(site.getName() + " Features");

        if(savedInstanceState!=null && savedInstanceState.getBoolean("alert"))
        {
            final String desc = savedInstanceState.getString("description");
            showDialog(new Feature("", desc, -1, site, new ArrayList<Level>()));
        }

        //on clicking new feature button
        //launching new feature dialog
        Button newFeature = (Button) findViewById(R.id.newFeatureBtn);
        newFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialog(null);
            }

        });
    }

    public void loadFeatures(ArrayList<Feature> newFeatures)
    {
        //adding new features passed from FirebaseHandler
        for (int i = 0; i < newFeatures.size(); i++) {
            Feature temp = newFeatures.get(i);
            int index = allFeatures.indexOf(temp);
            if (index < 0) {
                allFeatures.add(temp);
            } else {
                allFeatures.set(index, temp);
            }
        }

        //ArrayList containing feature datum and id to populate listview
        featuresList = new ArrayList<HashMap<String, String>>();

        //Looping through all features to add them to listview
        for(int i = 0; i < allFeatures.size(); i++)
        {
            Feature temp = allFeatures.get(i);

            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TAG_PID, temp.getID());
            map.put(TAG_FEATURE, temp.toString());

            // adding HashList to ArrayList
            featuresList.add(map);
        }

        //adding arraylist to listview
        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        AllFeaturesActivity.this, featuresList,
                        R.layout.list_item, new String[] { TAG_PID,
                        TAG_FEATURE},
                        new int[] { R.id.pid, R.id.name }); //listview entries will contain feature's id and name

                // updating listview
                setListAdapter(adapter);
            }
        });
    }

    private void showDialog(final Feature feature)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View featureLayout = inflater.inflate(R.layout.new_feature_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(AllFeaturesActivity.this);
        }
        description = (EditText) featureLayout.findViewById(R.id.featureDescription);

        if(feature!=null)
        {
            description.setText(feature.getDescription());
        }
        alert.setTitle("Create A New Feature");
        alert.setPositiveButton("Create Feature", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Feature feat = new Feature("", description.getText().toString(), -1, site, new ArrayList<Level>());

                if(!description.toString().equals("")) {

                    fbh.createFeature(feat);
                    alert = null;
                }
                else
                {
                    //user must fill out all necessary fields
                    Toast.makeText(featureLayout.getContext(), "You must fill out all fields before saving", Toast.LENGTH_SHORT).show();
                    showDialog(feat);
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
        alert.setView(featureLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now
        if(alert!=null)
        {
            //TODO: replace these with feature parcelable?
            outState.putBoolean("alert", true);
            outState.putString("desc", description.getText().toString());
        }
        else
        {
            outState.putBoolean("alert", false);
        }
    }
}
