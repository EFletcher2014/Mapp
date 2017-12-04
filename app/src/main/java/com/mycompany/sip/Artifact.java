package com.mycompany.sip;

import android.os.Parcelable;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Artifacts
 */
public class Artifact{

    private Site site;
    private Unit unit;
    private Level level;
    private String accessionNumber; //TODO: should this go with site?
    private int catalogNumber;
    private String contents;
    private int pk;

    public Artifact(Site s, Unit u, Level l, String acc, int cat, String con, int p)
    {
        site=s;
        unit=u;
        level=l;
        accessionNumber=acc;
        catalogNumber=cat;
        contents=con;
        pk=p;
    }

    public int getPk() {return pk; }

    public Site getSite()
    {
        return site;
    }

    public Unit getUnit()
    {
        return unit;
    }

    public Level getLevel()
    {
        return level;
    }

    public int getCatalogNumber()
    {
        return catalogNumber;
    }

    public String getAccessionNumber()
    {
        return accessionNumber;
    }

    public String getContents()
    {
        return contents;
    }

    @Override
    public String toString()
    {
        return accessionNumber + "-" + catalogNumber + " " + contents;
    }

    public void setCatalogNumber(int c)
    {
        catalogNumber=c;
    }

    public void setContents(String c)
    {
        contents=c;
    }

    public void setAccessionNumber(String a)
    {
        accessionNumber=a;
    }


    //TODO: override .equals()
}
