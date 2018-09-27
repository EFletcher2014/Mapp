package com.mycompany.sip;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static com.mycompany.sip.Global.TAG_SITENAME;

public class SiteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);

        Intent openIntent = getIntent();
        Site site = openIntent.getParcelableExtra(TAG_SITENAME);

        TextView title = findViewById(R.id.siteTitle);
        TextView name = findViewById(R.id.siteActivityName);
        TextView datum = findViewById(R.id.siteDatum);
        title.setText(site.getNumber());
        name.setText(site.getName());
        datum.setText(site.getDatum().toString());

    }
}
