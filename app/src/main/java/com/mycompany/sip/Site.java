package com.mycompany.sip;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Timestamp;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Sites
 */

public class Site implements Parcelable {

    private String name;
    private String number;
    private String dateOpened;
    private String description;
    private String id;
    private LatLng datum;

    public Site(String i, String n, String nu, String desc, String date, double latude, double lotude)
    {
        name = n;
        id = i;
        number = nu;
        description = desc;
        dateOpened = date;
        datum = new LatLng(latude, lotude);
    }

    public Site(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.number = in.readString();
        this.dateOpened = in.readString();
        this.description = in.readString();
        this.datum = new LatLng(in.readDouble(), in.readDouble());
    }

    public String getName()
    {
        return name;
    }

    public String getID()
    {
        return id;
    }

    public String getNumber()
    {
        return number;
    }

    public String getDateOpened()
    {
        return dateOpened;
    }

    public String getDescription()
    {
        return description;
    }

    public LatLng getDatum() { return datum; }



    @Override
    public String toString()
    {
        return number + " " + name;
    }

    public void setName(String n)
    {
        name=n;
    }

    public void setNumber(String n)
    {
        number=n;
    }

    public void setDateOpened(String d)
    {
        dateOpened=d;
    }

    public void setDescription(String des)
    {
        description=des;
    }

    public void setDatum(double la, double lo)
    {
        datum = new LatLng(la, lo);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(dateOpened);
        dest.writeString(description);
        dest.writeDouble(datum.latitude);
        dest.writeDouble(datum.longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Site> CREATOR = new Parcelable.Creator<Site>() {

        public Site createFromParcel(Parcel in) {
            return new Site(in);
        }

        public Site[] newArray(int size) {
            return new Site[size];
        }
    };

    @Override
    public boolean equals(Object o)
    {
        try {
            String id = ((Site) o).getID();
            Boolean same = this.getID().equals(id);
            return same;
            //String num = ((Site) o).getNumber();
            //return this.getNumber().equals(num);
        }catch(Exception e)
        {
            return false;
        }
    }
}
