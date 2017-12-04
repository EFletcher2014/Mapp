package com.mycompany.sip;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Units
 */
public class Unit implements Parcelable {

    private String datum;
    private String dateOpened;
    private String nsDimension;
    private String ewDimension;
    private Site site;
    private String excavators;
    private String reasonForOpening;
    private int pk;

    //TODO: add pk and maybe fk?
    public Unit(String dat, String date, String nsDim, String ewDim, Site st, String exc, String reas, int p)
    {
        this.datum=dat;
        this.dateOpened=date;
        this.nsDimension=nsDim;
        this.ewDimension=ewDim;
        this.site=st;
        this.excavators=exc;
        this.reasonForOpening=reas;
        this.pk=p;

    }

    public Unit(Parcel in)
    {
        this.datum=in.readString();
        this.dateOpened=in.readString();
        this.nsDimension=in.readString();
        this.ewDimension=in.readString();
        this.site=in.readParcelable(Site.class.getClassLoader());
        this.excavators=in.readString();
        this.reasonForOpening=in.readString();
        this.pk=in.readInt();
    }

    public String getDatum()
    {
        return datum;
    }

    public String getDateOpened()
    {
        return dateOpened;
    }

    public String getNsDimension()
    {
        return nsDimension;
    }

    public String getEwDimension()
    {
        return ewDimension;
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

    public int getPk() { return pk; }

    @Override
    public String toString()
    {
        return datum;
    }

    public void setDatum(String d)
    {
        datum=d;
    }

    public void setDateOpened(String d)
    {
        dateOpened=d;
    }

    public void setNsDimension(String ns)
    {
        nsDimension=ns;
    }

    public void setEwDimension(String ew)
    {
        ewDimension=ew;
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
        dest.writeString(datum);
        dest.writeString(dateOpened);
        dest.writeString(nsDimension);
        dest.writeString(ewDimension);
        dest.writeParcelable(site, flags);
        dest.writeString(excavators);
        dest.writeString(reasonForOpening);
        dest.writeInt(pk);
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

    //TODO: override .equals()
}
