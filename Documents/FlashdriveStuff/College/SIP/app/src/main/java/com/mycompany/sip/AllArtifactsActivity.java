//All from androidhive 7/30/17
package com.mycompany.sip;

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
import android.os.Bundle;
import android.util.Log;
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

public class AllArtifactsActivity extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> artifactsList;

    private static Site site;
    private static Unit unit;
    private static Level level;
    private static Artifact artifact;

    private AlertDialog.Builder alert;
    private EditText accNum;
    private EditText catNum;
    private EditText contents;
    private String pid;
    private String depth;
    private int foreignKey;
    ArrayList<Artifact> allArtifacts = new ArrayList<>();

    boolean test=false;
    ArrayList<Artifact> testArtifactsList = new ArrayList<>();
    Artifact[] testArtifacts = {new Artifact(site, unit, level, "17-2", 17, "seed bead"),
            new Artifact(site, unit, level, "17-2", 16, "projectile point"),
            new Artifact(site, unit, level, "17-2", 27, "flint flake")};

    // url to get all artifacts list
    //TODO: Get real URL
    private static String url_all_artifacts = "http://75.134.106.101:80/mapp/get_all_artifacts.php";

    // JSON Node names
    //TODO: get correct variables (once server is running)
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ARTIFACTS = "artifacts";
    private static final String TAG_PID = "PrimaryKey";
    private static final String TAG_ANUM = "accNum";
    private static final String TAG_CNUM = "catNum";
    private static final String TAG_CONT = "contents";

    // artifacts JSONArray
    JSONArray artifacts = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_artifacts);

        if(savedInstanceState!=null)
        {
            if(savedInstanceState.getBoolean("alert"))
            {
                showDialog(savedInstanceState.getString("Accession Number"), savedInstanceState.getString("Catalog Number"), savedInstanceState.getString("Contents"));
            }
        }

        // Hashmap for ListView
        artifactsList = new ArrayList<HashMap<String, String>>();

        //added by Emily Fletcher 8/29/17
        Intent openIntent = getIntent();
        foreignKey = openIntent.getIntExtra("PrimaryKey", -1);
        site = openIntent.getParcelableExtra("name");
        unit = openIntent.getParcelableExtra("datum");
        level = openIntent.getParcelableExtra("depth");
        TextView titleText = (TextView) findViewById(R.id.artifactsLabel);
        String title = site.getName() + " " + unit.getDatum() + " Level " + level.getNumber() + " Artifacts";
        titleText.setText(title);

        if(!test) {
            // Loading sites in Background Thread
            new LoadAllArtifacts().execute();
        }
        else
        {
            testArtifactsList.add(new Artifact(site, unit, level, "17-2", 17, "seed bead"));
            testArtifactsList.add(new Artifact(site, unit, level, "17-2", 16, "projectile point"));
            testArtifactsList.add(new Artifact(site, unit, level, "17-2", 27, "flint flake"));
            // looping through All artifacts
            for (int i = 0; i < testArtifactsList.size(); i++) {

                String artifact = testArtifactsList.get(i).toString();

                // creating new HashMap
                HashMap<String, String> testMap = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                testMap.put(TAG_PID, i + "");
                testMap.put("name", artifact);

                // adding HashList to ArrayList
                artifactsList.add(testMap);
                System.out.println(artifactsList);
            }
            ListAdapter adapter = new SimpleAdapter(
                    AllArtifactsActivity.this, artifactsList,
                    R.layout.list_item, new String[] { TAG_PID,
                    "name"},
                    new int[] { R.id.pid, R.id.name });
            // updating listview
            setListAdapter(adapter);
        }

        // Get listview
        ListView lv = getListView();

        //TODO: make this do something else (but what??). Fix in other activities
        // on seleting single level
        // launching Edit level Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                depth = ((TextView) view.findViewById(R.id.name)).getText()
                    .toString();
                pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                showDialog(testArtifacts[Integer.parseInt(pid)].getAccessionNumber(), testArtifacts[Integer.parseInt(pid)].getCatalogNumber() + "", testArtifacts[Integer.parseInt(pid)].getContents());

                //TODO: make these fields autofill, like the other edit screens
                // getting values from selected ListItem

            }
        });

        //on clicking new Level button
        //launching new level activity
       Button newArtifact = (Button) findViewById(R.id.newArtifactBtn);
        newArtifact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("", "", "");

                //TODO: When user clicks save, should save to server and add new artifact to list
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
            // means user edited/deleted level
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllArtifacts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllArtifactsActivity.this);
            pDialog.setMessage("Loading artifacts. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All artifacts from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            HashMap params = new HashMap();

            params.put("foreignKey", foreignKey);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All artifacts: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // artifacts found
                    // Getting Array of artifacts
                    artifacts = json.getJSONArray(TAG_ARTIFACTS);

                    // looping through All sites
                    for (int i = 0; i < artifacts.length(); i++) {
                        JSONObject c = artifacts.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String anum = c.getString(TAG_ANUM);
                        int cnum = c.getInt(TAG_CNUM);
                        String cont = c.getString(TAG_CONT);

                        Artifact temp = new Artifact(site, unit, level, anum, cnum, cont);
                        String name = temp.toString();
                        allArtifacts.add(temp);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put("name", name);

                        // adding HashList to ArrayList
                        artifactsList.add(map);
                    }
                } else {
                    // no artifacts found
                    // Launch Add New level Activity
                    Intent i = new Intent(getApplicationContext(),
                            MapHome.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
                            AllArtifactsActivity.this, artifactsList,
                            R.layout.list_item, new String[] { TAG_PID,
                            "name"},
                            new int[] { R.id.pid, R.id.name });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

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
            outState.putString("Accession Number", accNum.getText().toString());
            outState.putString("Catalog Number", catNum.getText().toString());
            outState.putString("Contents", contents.getText().toString());

        }
        else
        {
            outState.putBoolean("alert", false);
        }
    }
    private void showDialog(String aNum, String cNum, String con)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View artifactLayout = inflater.inflate(R.layout.new_artifact_dialog, null);
        alert = new AlertDialog.Builder(AllArtifactsActivity.this);
        accNum = (EditText) artifactLayout.findViewById(R.id.accNum);
        accNum.setText(aNum);
        catNum = (EditText) artifactLayout.findViewById(R.id.catNum);
        catNum.setText(cNum);
        contents = (EditText) artifactLayout.findViewById(R.id.contents);
        contents.setText(con);
        alert.setTitle("Edit Artifact");
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO: Do server stuff
                CharSequence toastMessage = "Saving Artifact...";
                Toast toast = Toast.makeText(artifactLayout.getContext(), toastMessage, Toast.LENGTH_LONG);
                toast.show();
                alert=null;
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