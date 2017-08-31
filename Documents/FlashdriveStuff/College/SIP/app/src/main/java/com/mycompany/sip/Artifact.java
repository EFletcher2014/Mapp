package com.mycompany.sip;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Artifacts
 */
public class Artifact {

    private Site site;
    private Unit unit;
    private Level level;
    private String catalogNumber; //TODO: should this go with site?
    private int accessionNumber;
    private String contents;

    public Artifact(Site s, Unit u, Level l, String cat, int acc, String con)
    {
        site=s;
        unit=u;
        level=l;
        catalogNumber=cat;
        accessionNumber=acc;
        contents=con;
    }

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

    public String getCatalogNumber()
    {
        return catalogNumber;
    }

    public int getAccessionNumber()
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

    public void setAccessionNumber(int a)
    {
        accessionNumber=a;
    }

    public void setContents(String c)
    {
        contents=c;
    }


}
