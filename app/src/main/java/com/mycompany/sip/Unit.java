package com.mycompany.sip;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Units
 */
public class Unit implements Parcelable {

    private String datum;
    private String dateOpened;
    private int NSCoor;
    private int EWCoor;
    private int NSDim;
    private int EWDim;
    private Site site;
    private String excavators;
    private String reasonForOpening;
    private String ID;

    public Unit(Site s, String i, int nsc, int ewc, int nsd, int ewd, String date, String r)
    {
        this.site = s;
        this.ID = i;
        this.NSCoor = nsc;
        this.EWCoor = ewc;
        this.NSDim = nsd;
        this.EWDim = ewd;
        this.dateOpened = date;
        this.reasonForOpening = r;
        this.datum = toDatum(nsc, ewc);
    }

    public Unit(Parcel in)
    {
        this.ID = in.readString();
        this.NSCoor = in.readInt();
        this.EWCoor = in.readInt();
        this.NSDim = in.readInt();
        this.EWDim = in.readInt();
        this.datum=in.readString();
        this.dateOpened=in.readString();
        this.site=in.readParcelable(Site.class.getClassLoader());
        this.excavators=in.readString();
        this.reasonForOpening=in.readString();
    }

    public String toDatum(int n, int e)
    {
        String temp = (n < 0) ? "S" : "N";
        int tempNSC = Math.abs(n);
        temp += (tempNSC > 9) ? tempNSC : "0" + tempNSC;
        temp += (e < 0) ? "W" : "E";
        int tempEWC = Math.abs(e);
        temp += (tempEWC > 9) ? tempEWC : "0" + tempEWC;

        return temp;
    }

    public String getDatum()
    {
        return datum;
    }

    public String getID() { return ID; }

    public int getNSCoor() { return NSCoor; }

    public int getEWCoor() { return EWCoor; }

    public int getNSDim() { return NSDim; }

    public int getEWDim() { return EWDim; }

    public String getDateOpened()
    {
        return dateOpened;
    }

    public int getNsDimension()
    {
        return NSDim;
    }

    public int getEwDimension()
    {
        return EWDim;
    }

    public Site getSite()
    {
        return site;
    }

    public String getExcavators()
    {
        return excavators;
    }

    public String getReasonForOpening()
    {
        return reasonForOpening;
    }

    @Override
    public String toString()
    {
        return datum;
    }

    public String[] tabulatedInfo() { return new String[]{getDatum(), getNSDim()+"", getEWDim()+"", getDateOpened(), reasonForOpening};}

    public void setDatum(String d)
    {
        datum=d;
    }

    public void setDateOpened(String d)
    {
        dateOpened=d;
    }

    public void setNsDimension(int ns)
    {
        NSDim=ns;
    }

    public void setEwDimension(int ew)
    {
        EWDim=ew;
    }

    public void setExcavators(String ex)
    {
        excavators=ex;
    }

    public void setReasonForOpening(String r)
    {
        reasonForOpening=r;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(ID);
        dest.writeInt(NSCoor);
        dest.writeInt(EWCoor);
        dest.writeInt(NSDim);
        dest.writeInt(EWDim);
        dest.writeString(datum);
        dest.writeString(dateOpened);
        dest.writeParcelable(site, flags);
        dest.writeString(excavators);
        dest.writeString(reasonForOpening);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Unit> CREATOR = new Parcelable.Creator<Unit>() {

        public Unit createFromParcel(Parcel in) {
            return new Unit(in);
        }

        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        try {
            //String site = ((Unit) o).getSite().getNumber();
            //String num = ((Unit) o).getDatum();
            //return (this.getSite().getNumber().equals(site) && this.getDatum().equals(num));
            String id = ((Unit) o).getID();
            return this.ID.equals(id);
        }catch(Exception e)
        {
            return false;
        }
    }
}
