package com.mycompany.sip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Artifacts
 */

//TODO: these probably don't need site/unit/level/bag, just lowest one so they can all link to each other
public class Artifact implements Parcelable {

    private Site site;
    private Unit unit;
    private Level level;
    private ArtifactBag bag;
    private int pk;
    private int remotePK;
    private String description;
    private Bitmap selection;
    private byte[] selectionAsBytes;
    private Timestamp firstCreated;
    private Timestamp lastUpdated;

    public Artifact(Site s, Unit u, Level l, ArtifactBag b, int p, String desc, byte[] sel, Timestamp created)
    {
        site=s;
        unit=u;
        level=l;
        bag = b;
        pk=p;
        description=desc;
        selection= BitmapFactory.decodeByteArray(sel, 0 ,sel.length);
        selectionAsBytes=sel;
        firstCreated=created;
    }

    public Artifact(Site s, Unit u, Level l, ArtifactBag b, int p, int rpk, String desc, byte[] sel, Timestamp created, Timestamp updated)
    {
        site=s;
        unit=u;
        level=l;
        bag=b;
        pk=p;
        remotePK=rpk;
        description=desc;
        selection=BitmapFactory.decodeByteArray(sel, 0 ,sel.length);
        selectionAsBytes=sel;
        firstCreated=created;
        lastUpdated=updated;
    }

    public Artifact(Site s, Unit u, Level l, ArtifactBag b, int p, int rpk, String desc, Bitmap sel, Timestamp created, Timestamp updated)
    {
        site=s;
        unit=u;
        level=l;
        bag=b;
        pk=p;
        remotePK=rpk;
        description=desc;
        selection=sel;
        selectionAsBytes=getSelectionAsBytes();
        firstCreated=created;
        lastUpdated=updated;
    }

    public Artifact(Parcel in)
    {
        System.out.println("Parcel received:" + in);
        this.site=in.readParcelable(Site.class.getClassLoader());//https://stackoverflow.com/questions/1996294/problem-unmarshalling-parcelables
        this.unit=in.readParcelable(Unit.class.getClassLoader());
        this.level=in.readParcelable(Level.class.getClassLoader());
        this.description=in.readString();
        this.selection=in.readParcelable(Bitmap.class.getClassLoader());
        this.selectionAsBytes=getSelectionAsBytes();
        this.pk=in.readInt();
        this.remotePK=in.readInt();
        this.firstCreated=new Timestamp(in.readLong()); //TODO: Will this work?
        this.lastUpdated=new Timestamp(in.readLong()); //TODO: same
    }

    public int getPk() {return pk; }

    public int getRemotePK() {return remotePK; }

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

    public ArtifactBag getBag()
    {
        return bag;
    }

    public String getDescription()
    {
        return description;
    }

    public Bitmap getSelection()
    {
        return selection;
    }

    public byte[] getSelectionAsBytes()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        selection.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bArray = bos.toByteArray();

        return bArray;
    }

    public Timestamp getFirstCreated() { return firstCreated; }

    public Timestamp getLastUpdated() { return lastUpdated; }


    @Override
    public String toString()
    {
        return /*bag.getAccessionNumber() + "-" + bag.getCatalogNumber() + " " + */description;
    }

    public void setLastUpdated(Timestamp t) { lastUpdated=t; }

    public void setRemotePK(int pk) { remotePK = pk; }

    @Override
    public boolean equals(Object o)
    {
        try {
            Artifact temp = ((Artifact) o);
            Site site = temp.getSite();
            Unit unit = temp.getUnit();
            Level level = temp.getLevel();
            ArtifactBag bag = temp.getBag();
            String desc = temp.getDescription();

            return (this.site.equals(site) && this.unit.equals(unit) && this.level.equals(level) && this.bag.equals(bag) && this.description.equals(desc));
        }catch(Exception e)
        {
            return false;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(site, flags);
        dest.writeParcelable(unit, flags);
        dest.writeParcelable(level, flags);
        dest.writeParcelable(bag, flags);
        dest.writeString(description);
        dest.writeValue(selection);
        dest.writeValue(selectionAsBytes);
        dest.writeInt(pk);
        dest.writeInt(remotePK);
        dest.writeLong((firstCreated!=null ? firstCreated.getTime() : 0));
        dest.writeLong((lastUpdated!=null ? lastUpdated.getTime() : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Artifact> CREATOR = new Parcelable.Creator<Artifact>() {

        public Artifact createFromParcel(Parcel in) {
            return new Artifact(in);
        }

        public Artifact[] newArray(int size) {
            return new Artifact[size];
        }
    };
}
