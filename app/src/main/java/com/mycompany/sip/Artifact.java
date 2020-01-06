package com.mycompany.sip;

public class Artifact {

    private Site site;
    private Unit unit;
    private Level level;
    private ArtifactBag artifactBag;
    private String ID;
    private String description;
    private String imagePath;

    public Artifact(Site s, Unit u, Level l, ArtifactBag a, String i, String d)
    {
        this.site = s;
        this.unit = u;
        this.level = l;
        this.artifactBag = a;
        this.ID = i;
        this.description = d;

        if(site != null && ID != null)
        {
            this.imagePath = site.getID() + "/" + ID + ".jpg";
        }
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

    public String getImagePath() {
        return imagePath;
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

    public String[] tabulatedInfo() { return new String[]{this.getUnit().toString(),
            this.getLevel().toString(),
            this.getArtifactBag().getAccessionNumber() + "-" + this.getArtifactBag().getCatalogNumber(),
            this.getDescription()};}

    @Override
    public boolean equals(Object o)
    {
        String i = ((Artifact) o).getID();

        return this.ID.equals(i);
    }
    //TODO; should this be parcelable?
}
