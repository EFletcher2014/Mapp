package com.mycompany.sip;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.okhttp.internal.DiskLruCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_SITENAME;
import static com.mycompany.sip.Global.sites;

/**
 * Created by Emily Fletcher 8/15/2018 during migration to Firebase
 * Replaces LocalDatabaseHandler and RemoteDatabaseHandler
 */
public class DatabaseHandler {

   private FirebaseFirestore db;
   Task<QuerySnapshot> task;
   CollectionReference docRef; //todo: will be a docref once users added

    String number;

   ArrayList<DocumentSnapshot> results;

   public DatabaseHandler ()
   {
       Global.sites = new ArrayList<Site>();
       db = FirebaseFirestore.getInstance();

       docRef = db.collection("sites");
       docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
           @Override
           public void onEvent(@Nullable QuerySnapshot snapshot,
                               @Nullable FirebaseFirestoreException e) {

               if (e != null) {
                   Log.w(TAG, "Listen failed.", e);
                   return;
               }

               for (QueryDocumentSnapshot doc : snapshot) {
                   if(doc != null) {
                       Object tempName = doc.get("Name");
                       Object tempNum = doc.get("Number");
                       Object tempDesc = doc.get("Description");

                       Site temp = new Site(doc.getId(), (tempName == null ? "" : tempName.toString()),
                               (tempNum == null ? "" : tempNum.toString()), (tempDesc == null ? "" : tempDesc.toString()));
                       if (!Global.sites.contains(temp)) {
                           Global.sites.add(temp);
                       }
                   }
               }
           }
       });
   }

   public void getAllSites(final Context con)
   {
       Global.sites = new ArrayList<Site>();

       db.collection("sites")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Site site = new Site(document.getId(), document.get("Name").toString(), document.get("Number").toString(), document.get("Description").toString());
                            if(!Global.sites.contains(site)) {
                                Global.sites.add(site);
                            }
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    }
                }
            });
   }

    public Site getSite(String num)
    {
        Query query = docRef.whereEqualTo("Number", num);
        task = query.get();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Tasks.await(task);
                    } catch (Exception e)
                    {
                        System.out.println(e);
                        //TODO: handle
                    }
                }
            }).start();
            results = (ArrayList) task.getResult().getDocuments();

        DocumentSnapshot temp = (results == null ? null : results.get(0)); //TODO: why is this returning all?
        Site site = (temp == null ? null : new Site(temp.getId(), (String) temp.get("Name"), (String) temp.get("Number"), (String) temp.get("Description")));

        return site;
    }

//    class LoadAllSites extends AsyncTask<String, String, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pDialog = new ProgressDialog(AllSitesActivity.this);
//            pDialog.setMessage("Loading sites. Please wait...");
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//
//        /**
//         * getting All sites from url
//         * */
//        protected String doInBackground(String... args) {
//
//            CollectionReference docRef = db.collection("sites");
//            Query query = docRef.whereEqualTo("Number", number);
//            ArrayList<DocumentSnapshot> sites = (ArrayList) query.get().getResult().getDocuments();
//
//            DocumentSnapshot temp = sites.get(0);
//            Site site = (temp == null ? null : new Site(temp.getId(), (String) temp.get("Name"), (String) temp.get("Number"), (String) temp.get("Description")));
//        }
//
//        /**
//         * After completing background task Dismiss the progress dialog
//         * and add data to listview
//         * **/
//        protected void onPostExecute(String file_url) {
//            // dismiss the dialog after getting all sites
//            pDialog.dismiss();
//
//        }
//    }


}
