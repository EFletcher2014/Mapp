package com.mycompany.sip;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Artifacts
 */
public class ArtifactBag {

    private Site site;
    private Unit unit;
    private Level level;
    private String id;
    private String accessionNumber; //TODO: should this go with site?
    private int catalogNumber;
    private String contents;

    public ArtifactBag(Site s, Unit u, Level l, String i, String acc, int cat, String con)
    {
        site=s;
        unit=u;
        level=l;
        id=i;
        accessionNumber=acc;
        catalogNumber=cat;
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

    public String getID() { return id; }

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

    @Override
    public boolean equals(Object o)
    {
        String i = ((ArtifactBag) o).getID();
        return this.id.equals(i);
    }
}
