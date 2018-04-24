package com.mycompany.sip;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for levels made parcelable 9/2/17
 */
public class Level implements Parcelable {
    private int number;
    private double begDepth;
    private double endDepth;
    private Site site; //TODO: do I need a site since it's listed in the unit that this level has?
    private Unit unit;
    private String dateStarted;
    private String excavationMethod;
    private String notes="";
    private int pk=-1;
    private int remotePK;
    private String imagePath = "";
    private Timestamp firstCreated;
    private Timestamp lastUpdated;

    public Level (int n, double bD, Site s, Unit u, int p, Timestamp created)
    {
        this.number=n;
        this.begDepth=bD;
        this.site=s;
        this.unit=u;
        this.pk=p;
        this.firstCreated=created;
    }

    public Level (int n, double bD, double eD, Site s, Unit u, String date, String excM, String no, int p, int rpk, Timestamp created, Timestamp updated)
    {
        this.number=n;
        this.begDepth=bD;
        this.endDepth=eD;
        this.site=s;
        this.unit=u;
        this.dateStarted=date;
        this.excavationMethod=excM;
        this.notes=no;
        System.out.println(notes);
        this.pk=p;
        this.remotePK=rpk;
        this.firstCreated=created;
        this.lastUpdated=updated;
    }

    public Level(Parcel in)
    {
        System.out.println(in);
        this.number=in.readInt();
        this.begDepth=in.readDouble();
        this.endDepth=in.readDouble();
        this.site=in.readParcelable(Site.class.getClassLoader());//https://stackoverflow.com/questions/1996294/problem-unmarshalling-parcelables
        this.unit=in.readParcelable(Unit.class.getClassLoader());
        this.dateStarted=in.readString();
        this.excavationMethod=in.readString();
        this.notes=in.readString();
        this.pk=in.readInt();
        this.remotePK=in.readInt();
        this.firstCreated=in.readParcelable(Timestamp.class.getClassLoader());
        this.lastUpdated=in.readParcelable(Timestamp.class.getClassLoader());
    }

    public int getPk() { return pk; }

    public int getRemotePK() { return remotePK; }

    public void setRemotePK(int pk) { remotePK = pk; }

    public int getNumber()
    {
        return number;
    }

    public double getBegDepth()
    {
        return begDepth;
    }

    public double getEndDepth()
    {
        return endDepth;
    }

    public Site getSite()
    {
        return site;
    }

    public Unit getUnit()
    {
        return unit;
    }

    public String getDateStarted()
    {
        return dateStarted;
    }

    public String getExcavationMethod()
    {
        return excavationMethod;
    }

    public String getDepth()
    {
        return begDepth + "cmbd - " + endDepth + "cmbd";
    }

    public String getNotes()
    {
        return notes;
    }

    public Timestamp getFirstCreated() { return firstCreated; }

    public Timestamp getLastUpdated() { return lastUpdated; }

    @Override
    public String toString()
    {
        return number + " (" + this.getDepth() + ")";
    }

    public void setBegDepth(double bd)
    {
        begDepth=bd;
    }

    public void setEndDepth(double ed)
    {
        endDepth=ed;
    }

    public void setDateStarted(String d)
    {
        dateStarted=d;
    }

    public void setExcavationMethod(String ex)
    {
        excavationMethod=ex;
    }

    public void setNotes(String no)
    {
        notes=no;
    }

    public void setLastUpdated(java.sql.Timestamp t) { lastUpdated=t; }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(number);
        dest.writeDouble(begDepth);
        dest.writeDouble(endDepth);
        dest.writeParcelable(site, flags);
        dest.writeParcelable(unit, flags);
        dest.writeString(dateStarted);
        dest.writeString(excavationMethod);
        dest.writeString(notes);
        dest.writeInt(pk);
        dest.writeInt(remotePK);
        dest.writeParcelable((Parcelable)firstCreated, flags);
        dest.writeParcelable((Parcelable)lastUpdated, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Level> CREATOR = new Parcelable.Creator<Level>() {

        public Level createFromParcel(Parcel in) {
            return new Level(in);
        }

        public Level[] newArray(int size) {
            return new Level[size];
        }
    };

    public void setImagePath(String path)
    {
        this.imagePath=path;
    }

    public String getImagePath()
    {
        return this.imagePath;
    }

    @Override
    public boolean equals(Object o)
    {
        try {
            int num = ((Level) o).getNumber();
            return (this.getNumber()==num && this.getUnit().equals(((Level) o).getUnit()));
        }catch(Exception e)
        {
            return false;
        }
    }

}