package com.mycompany.sip;

public class Artifact {

    private Site site;
    private Unit unit;
    private Level level;
    private ArtifactBag artifactBag;
    private String ID;
    private String description;

    public Artifact(Site s, Unit u, Level l, ArtifactBag a, String i, String d)
    {
        this.site = s;
        this.unit = u;
        this.level = l;
        this.artifactBag = a;
        this.ID = i;
        this.description = d;
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

    public ArtifactBag getArtifactBag() {
        return artifactBag;
    }

    public String getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d)
    {
        this.description = d;
    }

    @Override
    public String toString()
    {
        return this.description;
    }
    //TODO; should this be parcelable?
}
