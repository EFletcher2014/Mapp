package com.mycompany.sip;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class CrewActivity extends ListActivity {

    public static boolean isActive = true;

    FirebaseHandler fbh = FirebaseHandler.getInstance();

    HashMap<String, ArrayList> crewRoles = new HashMap<>();
    HashMap<String, String> excavators = new HashMap<>();
    ArrayList<String> director = new ArrayList<>();

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
        setContentView(R.layout.activity_crew);
        fbh.updateCrewActivity(this);
    }

    public void loadCrew(HashMap<String, ArrayList> roles)
    {
        Object[] crewMembers = roles.keySet().toArray();
        for(int i = 0; i<roles.size(); i++)
        {
            if(crewRoles.get(crewMembers[i].toString()).contains("excavator"))
            {
                if(excavators.containsKey(crewMembers[i].toString()))
                {

                }
            }
        }



    }
}
