package com.mycompany.sip;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_SITENAME;

public class SiteActivity extends AppCompatActivity {

    private Site site;
    private ArrayList<Unit> allUnits = new ArrayList<>();
    private ArrayList<Level> allLevels = new ArrayList<>();
    private ArrayList<Artifact> allArtifacts = new ArrayList<>();
    private ArrayList<ArtifactBag> allArtifactBags = new ArrayList<>();
    private ArrayList<Feature> allFeatures = new ArrayList<>();

    public static boolean isActive;

    //Firebase
    FirebaseHandler fbh = FirebaseHandler.getInstance();

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
        fbh.updateSiteActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);

        Intent openIntent = getIntent();
        site = openIntent.getParcelableExtra(TAG_SITENAME);

        TextView title = findViewById(R.id.siteTitle);
        TextView name = findViewById(R.id.siteActivityName);
        TextView datum = findViewById(R.id.siteDatum);
        title.setText(site.getNumber());
        name.setText(site.getName());
        datum.setText(site.getDatum().toString());

        //notify firebase that a site has been selected to get its units and levels in case the user attempts to print a report
        fbh.siteSelected(site);

    }

    public void goToCrew(View view)
    {
        HashMap<String, String> role = new HashMap<>();
        role.put(site.getID(), "excavator");
        Intent crewIntent = new Intent(view.getContext(), CrewActivity.class);
        crewIntent.putExtra(TAG_SITENAME, site);
        startActivityForResult(crewIntent, 0);
    }

    public void goToUnits(View view)
    {
        Intent in = new Intent(view.getContext(), AllUnitsActivity.class);
        in.putExtra(TAG_PID, -1);
        in.putExtra(TAG_SITENAME, site);
        startActivity(in);
    }

    public void saveReport(View view) {

        ReportHandler pdfHandler = new ReportHandler(site, getExternalFilesDir(null));

        pdfHandler.siteReport(site, allUnits);
        pdfHandler.addLevelReports(allLevels, allArtifactBags, allArtifacts, allFeatures);
        pdfHandler.addArtifactsCatalog(allArtifacts);
        pdfHandler.addFeaturesCatalog(allFeatures);

        Toast.makeText(getApplicationContext(), "Site Report has been updated", Toast.LENGTH_SHORT).show();
    }

    public void goToFeatures(View view) {
        Intent in = new Intent(view.getContext(), AllFeaturesActivity.class);
        in.putExtra(TAG_SITENAME, site);
        startActivity(in);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                FirebaseDynamicLinks.getInstance().getDynamicLink(data).addOnCompleteListener(new OnCompleteListener<PendingDynamicLinkData>() {
                    @Override
                    public void onComplete(@NonNull Task<PendingDynamicLinkData> task) {
                        //FirebaseAppInvite.getInvitation(task.getResult()).;
                        //FirebaseAuth.getInstance()

                    }
                });
                String[] ids = AppInviteInvitation.getInvitationIds(
                        resultCode, data);
                for (String id : ids) {
                    System.out.println(id);
                }
            } else {
                // Failed to send invitations
            }
        }
    }

    //method called by FirebaseHandler to populate list of units
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
    }

    //Method called from FirebaseHandler to populate list of levels
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
    }

    //Method called from FirebaseHandler to populate list of artifacts
    public void loadArtifacts(ArrayList<Artifact> newArtifacts)
    {
        //adding new artifacts from Firebase
        for(int i = 0; i < newArtifacts.size(); i++)
        {
            Artifact temp = newArtifacts.get(i);
            int index = allArtifacts.indexOf(temp);
            if (index < 0) {
                allArtifacts.add(temp);
            } else {
                allArtifacts.set(index, temp);
            }
        }
    }

    //Method called from FirebaseHandler to populate list of artifact bags
    public void loadArtifactBags(ArrayList<ArtifactBag> newArtifactBags)
    {
        //adding new artifacts from Firebase
        for(int i = 0; i < newArtifactBags.size(); i++)
        {
            ArtifactBag temp = newArtifactBags.get(i);
            int index = allArtifactBags.indexOf(temp);
            if (index < 0) {
                allArtifactBags.add(temp);
            } else {
                allArtifactBags.set(index, temp);
            }
        }
    }

    //Method called from FirebaseHandler to populate list of features
    public void loadFeatures(ArrayList<Feature> newFeatures)
    {
        //adding new features from Firebase
        for(int i = 0; i < newFeatures.size(); i++)
        {
            Feature temp = newFeatures.get(i);
            int index = allFeatures.indexOf(temp);
            if (index < 0) {
                allFeatures.add(temp);
            } else {
                allFeatures.set(index, temp);
            }
        }
    }

    //Method called from FirebaseHandler to populate list of feature links
    public void updateFeatureLinks(ArrayList<String[]> featureLinks)
    {
        for(String[] featureLink : featureLinks)
        {
            for( Feature feat : allFeatures)
            {
                if(feat.getID().equals(featureLink[0])) //finds the feature
                {
                    for( Level lev : allLevels) {
                        if (lev.getID().equals(featureLink[1]))
                        {
                            feat.addLevel(lev);
                        }
                    }
                }
            }
        }
    }
}
