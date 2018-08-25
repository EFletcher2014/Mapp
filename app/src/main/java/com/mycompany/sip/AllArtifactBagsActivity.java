//All from androidhive 7/30/17
package com.mycompany.sip;

import java.util.ArrayList;
import java.util.HashMap;
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

import static com.mycompany.sip.Global.*;

public class AllArtifactsActivity extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

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
    ArrayList<ArtifactBag> allArtifactBags = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_artifacts);

        if(savedInstanceState!=null)
        {
            if(savedInstanceState.getBoolean("alert"))
            {
                showDialog(new ArtifactBag(site, unit, level, "", savedInstanceState.getString("Accession Number"), Integer.parseInt(savedInstanceState.getString("Catalog Number")), savedInstanceState.getString("Contents")));
            }
        }

        // Hashmap for ListView
        artifactBagsList = new ArrayList<HashMap<String, String>>();

        //added by Emily Fletcher 8/29/17
        Intent openIntent = getIntent();
        //foreignKey = openIntent.getIntExtra("PrimaryKey", -1); tODO: is this still passed? Should it be?
        site = openIntent.getParcelableExtra("name");
        unit = openIntent.getParcelableExtra("datum");
        level = openIntent.getParcelableExtra("depth");
        TextView titleText = (TextView) findViewById(R.id.artifactsLabel);
        String title = site.getName() + " " + unit.getDatum() + " Level " + level.getNumber() + " Artifacts";
        titleText.setText(title);

        //TODO: load artifactBags from Firebase


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
                String su = ((TextView) view.findViewById(R.id.su)).getText().toString();
                if(test)
                {
                    showDialog(testArtifactBags[Integer.parseInt(pid)]);
                }
                else
                {
                    showDialog(allArtifactBags.get(Integer.parseInt(su)));
                }


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
                showDialog(null);

                //TODO: When user clicks save, should save to server and add new artifactBag to list
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
            //new UpdateDBs(getApplicationContext()).execute();
        }

        /**
         * getting All artifacts from url
         * */
        protected String doInBackground(String... args) {
            //allArtifactBags = rdb.loadAllArtifacts(level, null);
            for(int i = 0; i< allArtifactBags.size(); i++)
            {
                ArtifactBag temp = allArtifactBags.get(i);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put(TAG_PID, temp.getPk() + "");
                map.put("name", temp.toString());
                map.put("Level ArtifactBag", i + "");

                // adding HashList to ArrayList
                artifactsList.add(map);
            }
            /*// Building Parameters
            HashMap params = new HashMap();

            params.put("foreignKey", foreignKey);

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_artifacts, "GET", params);

            try {
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
                            System.out.println(anum);
                            int cnum = c.getInt(TAG_CNUM);
                            String cont = c.getString(TAG_CONT);

                            ArtifactBag temp = new ArtifactBag(site, unit, level, anum, cnum, cont, Integer.parseInt(id));
                            String name = temp.toString();
                            System.out.println(temp.toString());
                            allArtifactBags.add(temp);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_PID, id);
                            map.put("name", name);
                            map.put("Level ArtifactBag", i + "");

                            // adding HashList to ArrayList
                            artifactsList.add(map);

                            //save to local database
                            if (ldb.updateArtifact(temp, temp.getLevel().getPk()) == 0) {
                                System.out.println("Adding new artifactBag to SQLite DB");
                                ldb.addArtifact(temp);
                            } else {
                                System.out.println();
                            }
                            System.out.println(ldb.getArtifactsCount());
                            System.out.println(ldb.getArtifact(i));
                            System.out.println(ldb.getAllArtifacts().toString());
                        }
                    } else {
                        // no artifacts found
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch(NullPointerException e)
            {
                allArtifactBags = (ArrayList) ldb.getAllArtifactsFromLevel(level.getPk());
                for(int i=0; i<allArtifactBags.size(); i++)
                {
                    ArtifactBag temp = allArtifactBags.get(i);

                    // creating new HashMap
                    HashMap<String, String> map = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    map.put(TAG_PID, temp.getPk() + "");
                    map.put("name", temp.toString());
                    map.put("Level ArtifactBag", i + "");

                    // adding HashList to ArrayList
                    artifactsList.add(map);
                }
            }*/
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
                    if(artifactsList.size()!=0) {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        ListAdapter adapter = new SimpleAdapter(
                                AllArtifactsActivity.this, artifactsList,
                                R.layout.list_item, new String[]{TAG_PID,
                                "name", "Level ArtifactBag"},
                                new int[]{R.id.pid, R.id.name, R.id.su});
                        // updating listview
                        setListAdapter(adapter);
                    }else
                    {
                        showDialog(null);
                    }
                }
            });

        }

    }


    /**
     * Background Async Task to Create new artifactBag
     * */
    class CreateNewArtifact extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllArtifactsActivity.this);
            pDialog.setMessage("Creating ArtifactBag..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            //new UpdateDBs(getApplicationContext()).execute();
        }

        /**
         * Creating artifactBag
         * */
        protected String doInBackground(String... args) {
            if (true/*rdb.createNewArtifact(artifactBag)>0*/) {
                // successfully created artifactBag
                // closing this screen
                finish();

                //restarting activity so list will include new artifactBag
                startActivity(getIntent());
            } else {
                // failed to create artifactBag
            }
            /*// Building Parameters
            HashMap params = new HashMap();
            params.put("foreignKey", foreignKey);
            params.put("accNum", artifactBag.getAccessionNumber());
            params.put("catNum", artifactBag.getCatalogNumber());
            params.put("contents", artifactBag.getContents());

            // getting JSON Object
            // Note that create site url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_artifact,
                    "POST", params);

            try {
                // check log cat fro response
                Log.d("Create Response", json.toString());

                // check for success tag
                try {
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {

                        // closing this screen
                        finish();
                        startActivity(getIntent());
                    } else {
                        // failed to create artifactBag
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch(NullPointerException e)
            {
                ldb.addArtifact(artifactBag);//TODO: ldb's primary keys must be the same as the remote server's, but this one isn't there and won't be until the user connects
                                        //TODO: to the internet again. So what should we do? Let it default set for now and update it when we back up to remote server?
                                        //TODO: Then the ldb.update methods will have to be able to update PKs which I'm not sure is allowed...
                                        //TODO: Since both servers will have the same set of primary keys I guess we could just go with it and set the remote server's
                                        //TODO: when we're updating...But then we have to do more PHP stuff I think
                // closing this screen
                finish();

                //restarting activity so list will include new site
                startActivity(getIntent());
            }
*/
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
    private void showDialog(ArtifactBag art)
    {
        LayoutInflater inflater = getLayoutInflater();
        final View artifactLayout = inflater.inflate(R.layout.new_artifact_dialog, null);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        }
        else
        {
            alert = new AlertDialog.Builder(AllArtifactsActivity.this);
        }
        accNum = (EditText) artifactLayout.findViewById(R.id.accNum);
        catNum = (EditText) artifactLayout.findViewById(R.id.catNum);
        contents = (EditText) artifactLayout.findViewById(R.id.contents);

        if(art!=null)
        {
            String cnum = art.getCatalogNumber() + "";
            accNum.setText(art.getAccessionNumber());
            catNum.setText(cnum);
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
                artifactBag = new ArtifactBag(site, unit, level, accNum.getText().toString(), c, contents.getText().toString(), -1, -1, null, null);

                if(!(accNum.getText().toString().equals("")) && !(catNum.getText().toString().equals("")) && !(contents.getText().toString().equals("")))
                {
                    if(test) {
                        CharSequence toastMessage = "Saving ArtifactBag...";
                        Toast toast = Toast.makeText(artifactLayout.getContext(), toastMessage, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else
                    {
                        new CreateNewArtifact().execute();
                    }
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