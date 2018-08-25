package com.mycompany.sip;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for levels made parcelable 9/2/17
 */
public class Level implements Parcelable {
    private String ID;
    private int number;
    private double begDepth;
    private double endDepth;
    private Site site; //TODO: do I need a site since it's listed in the unit that this level has?
    private Unit unit;
    private String dateStarted;
    private String excavationMethod;
    private String notes="";

    public Level (Site s, Unit u, String i, int n, double bD, double eD, String exMeth, String notes, Uri p)
    {
        this.site = s;
        this.unit = u;
        this.ID = i;
        this.number = n;
        this.begDepth = bD;
        this.endDepth = eD;
        this.excavationMethod = exMeth;
        this.notes = notes;
    }

    public Level(Parcel in)
    {
        this.ID = in.readString();
        this.number=in.readInt();
        this.begDepth=in.readDouble();
        this.endDepth=in.readDouble();
        this.site=in.readParcelable(Site.class.getClassLoader());//https://stackoverflow.com/questions/1996294/problem-unmarshalling-parcelables
        this.unit=in.readParcelable(Unit.class.getClassLoader());
        this.dateStarted=in.readString();
        this.excavationMethod=in.readString();
        this.notes=in.readString();
    }

    public String getID() { return ID; }

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


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(ID);
        dest.writeInt(number);
        dest.writeDouble(begDepth);
        dest.writeDouble(endDepth);
        dest.writeParcelable(site, flags);
        dest.writeParcelable(unit, flags);
        dest.writeString(dateStarted);
        dest.writeString(excavationMethod);
        dest.writeString(notes);
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

    @Override
    public boolean equals(Object o)
    {
        try {
            String id = ((Level) o).getID();
            return this.getID().equals(id);
        }catch(Exception e)
        {
            return false;
        }
    }

}