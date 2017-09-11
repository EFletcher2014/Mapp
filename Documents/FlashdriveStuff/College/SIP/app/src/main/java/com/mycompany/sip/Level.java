package com.mycompany.sip;

import android.os.Parcel;
import android.os.Parcelable;

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

    public Level (int n, double bD, double eD, Site s, Unit u, String date, String excM)
    {
        this.number=n;
        this.begDepth=bD;
        this.endDepth=eD;
        this.site=s;
        this.unit=u;
        this.dateStarted=date;
        this.excavationMethod=excM;
    }

    public Level(Parcel in)
    {
        this.number=in.readInt();
        this.begDepth=in.readDouble();
        this.endDepth=in.readDouble();
        this.site=in.readParcelable(Site.class.getClassLoader());//https://stackoverflow.com/questions/1996294/problem-unmarshalling-parcelables
        this.unit=in.readParcelable(Unit.class.getClassLoader());
        this.dateStarted=in.readString();
        this.excavationMethod=in.readString();
    }

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


    //TODO: override .equals()

}