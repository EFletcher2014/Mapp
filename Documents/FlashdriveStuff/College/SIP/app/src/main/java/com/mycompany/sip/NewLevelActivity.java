package com.mycompany.sip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NewLevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_level);
    }
}

//TODO: Do I need this activity? Can/should it just create a new "MapHome" activity?
//I think the answer to the above question is yes, I can just use MapHome
