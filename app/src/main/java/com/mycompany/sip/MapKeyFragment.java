package com.mycompany.sip;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.*;


/**
 * A ListFragment to display a list of artifacts and features selected on a canvas in selectActivity
 * Not working as of 8/11/2018--listview won't populate
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapKeyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapKeyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapKeyFragment extends ListFragment
{

    private OnFragmentInteractionListener mListener;

    private ProgressDialog pDialog;
    private Context context;


    //HashMap to be passed to ListView, contains artifact's name and primary key
    ArrayList<HashMap<String, String>> artifactsList;
    private ArrayList<HashMap<String, String>> artifactsMap;
    private Artifact artifact;
    private LocalDatabaseHandler ldb;

    public MapKeyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapKeyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapKeyFragment newInstance(Artifact artifact) {
        MapKeyFragment fragment = new MapKeyFragment();
        Bundle args = new Bundle();
        args.putParcelable(TAG_ARTIFACT,  artifact);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //alv = (ListView) view.findViewById(android.R.id.list);
        System.out.println("View created!");
        this.context = view.getContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public ListAdapter refreshArtifactsLV(Artifact artifact, Context con, ListView alv, Level level)
    {
        //TODO: use asynctasks
        //TODO: split into add artifact and load artifacts methods
        pDialog = new ProgressDialog(con);
        ldb = new LocalDatabaseHandler(con);
        this.artifact = artifact;
        System.out.println("Adding artifact: " + artifact);
        if(artifact != null) {
            ldb.addArtifact(this.artifact);
            //new CreateNewArtifact().execute();
        }
        ArrayList<Artifact> afacts = (ArrayList) ldb.getAllArtifacts(); //TODO: should be getAllArtifactsFromLevel, but that's broken and isn't worth fixing
        artifactsMap = new ArrayList<HashMap<String, String>>();

        for(int i=0; i<afacts.size(); i++)
        {
            System.out.println(afacts.get(i));
            // creating new HashMap
            HashMap<String, String> testMap = new HashMap<String, String>();

            // adding each child node to HashMap key => value
            testMap.put(TAG_PID, afacts.get(i).getPk() + "");
            testMap.put("name", afacts.get(i).toString());
            System.out.println("testmap: " + testMap);

            // adding HashList to ArrayList
            artifactsMap.add(testMap);
            System.out.println(artifactsMap.get(i));
        }


        //From androidhive tutorial
        ListAdapter adapter = new SimpleAdapter(
                con, artifactsMap,
                R.layout.list_item, new String[]{TAG_PID,
                "name"},
                new int[]{R.id.pid, R.id.name});
        // updating listview

        ArrayAdapter<Artifact> aadapter = new ArrayAdapter<Artifact>(con, R.layout.list_item, afacts);
        setListAdapter(aadapter);
        aadapter.notifyDataSetChanged();

        return adapter;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Background Async Task to Create new artifact
     * Not used right now, will complicate listview which already isn't working.
     * */
    class LoadAllArtifacts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog.setMessage("Loading artifacts...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
            //new UpdateDBs(getApplicationContext()).execute();
            System.out.println("Loading all artifacts...........");
        }

        /**
         * Loading artifacts
         * */
        protected String doInBackground(String... args) {
            //TODO: add remote capability
            //madeArtifact=(rdb.createNewArtifact(artifact)>-1);
            artifactsList = (ArrayList) ldb.getAllArtifactsFromLevel(artifact.getLevel().getPk());
            artifactsMap = new ArrayList<HashMap<String, String>>();

            for(int i=0; i<artifactsList.size(); i++)
            {
                // creating new HashMap
                HashMap<String, String> testMap = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                testMap.put(TAG_PID, i + "");
                testMap.put("name", artifactsList.get(i).toString());

                // adding HashList to ArrayList
                artifactsMap.add(testMap);
            }


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            //pDialog.dismiss();

            //From androidhive tutorial
            ListAdapter adapter = new SimpleAdapter(
                    context, artifactsMap,
                    R.layout.list_item, new String[]{TAG_PID,
                    TAG_DESC},
                    new int[]{R.id.pid, R.id.name});
            // updating listview
            setListAdapter(adapter);
        }
    }

    /**
     * Background Async Task to Create new artifact
     * Not used right now, will complicate listview
     * */
    class CreateNewArtifact extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog.setMessage("Creating Level..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
            //new UpdateDBs(getApplicationContext()).execute();
        }

        /**
         * Creating artifact
         * */
        protected String doInBackground(String... args) {
            //TODO: add remote capability
            //madeArtifact=(rdb.createNewArtifact(artifact)>-1);
            ldb.addArtifact(artifact);
            System.out.println("Artifact " + artifact + " added");


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            //pDialog.dismiss();
        }
    }
}
