package com.mycompany.sip;

import java.util.ArrayList;

public class Feature {
    private String description;
    private String ID;
    private int number;
    private Site site;
    private ArrayList<Unit> units;
    private ArrayList<Level> levels;

    public Feature (String i, String desc, int n, Site s, ArrayList<Level> l)
    {
        this.ID = i;
        this.description = desc;
        this.number = n;
        this.site = s;
        this.levels = l;
//
//        if(levels != null) {
//            for (int j = 0; j < levels.size(); j++) {
//                units.add(levels.get(j).getUnit());
//            }
        //}
    }

    public void setDescription(String d)
    {
        this.description = d;
    }

    public void setID(String i)
    {
        this.ID = i;
    }

    public void setNumber(int n)
    {
        this.number = n;
    }

    public void addLevel(Level l)
    {
        if(!levels.contains(l)) {
            this.levels.add(l);
            //this.units.add(l.getUnit());
        }
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getID()
    {
        return this.ID;
    }

    public int getNumber()
    {
        return this.number;
    }

    public Site getSite() {
        return this.site;
    }

    public ArrayList<Level> getLevels() {
        return this.levels;
    }

    public ArrayList<Unit> getUnits() {
        return this.units;
    }

    @Override
    public String toString()
    {
        return "Feature " + this.number;
    }

    @Override
    public boolean equals(Object o)
    {
        String tempID = ((Feature) o).getID();
        return this.ID.equals(tempID);
    }
}
