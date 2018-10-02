package com.mycompany.sip;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.HashMap;

import static com.mycompany.sip.Global.TAG_PID;
import static com.mycompany.sip.Global.TAG_SITENAME;

public class SiteActivity extends AppCompatActivity {

    private Site site;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    }

    public void goToCrew(View view)
    {
        HashMap<String, String> role = new HashMap<>();
        role.put(site.getID(), "excavator");
        Intent intent = new AppInviteInvitation.IntentBuilder("You were invited to use Mapp to document an excavation")
                .setMessage("Click the link below to join this project")
                .setDeepLink(Uri.parse("https://mappdocumentation.page.link/join-as-excavator"))
                .setCallToActionText("Join Project")
                .setAdditionalReferralParameters(role)
                .build();
        startActivityForResult(intent, 123);
//        Intent crewIntent = new Intent(view.getContext(), CrewActivity.class);
//        startActivityForResult(crewIntent, 0);
    }

    public void goToUnits(View view)
    {
        Intent in = new Intent(view.getContext(), AllUnitsActivity.class);
        in.putExtra(TAG_PID, -1);
        in.putExtra(TAG_SITENAME, site);
        startActivity(in);
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
}
