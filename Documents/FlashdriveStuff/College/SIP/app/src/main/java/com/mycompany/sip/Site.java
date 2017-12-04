package com.mycompany.sip;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Sites
 */

public class Site implements Parcelable {

    private String name;
    private String number;
    private String dateOpened;
    private String location;
    private String description;

    public Site(String n, String num, String date, String loc, String desc)
    {
        this.name=n;
        this.number=num;
        this.dateOpened=date;
        this.location=loc;
        this.description=desc;
    }

    public Site(Parcel in) {
        this.name = in.readString();
        this.number = in.readString();
        this.dateOpened = in.readString();
        this.location = in.readString();
        this.description = in.readString();
    }

    public String getName()
    {
        return name;
    }

    public String getNumber()
    {
        return number;
    }

    public String getDateOpened()
    {
        return dateOpened;
    }

    public String getLocation()
    {
        return location;
    }

    public String getDescription()
    {
        return description;
    }

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

    public void setLocation(String l)
    {
        location=l;
    }

    public void setDescription(String des)
    {
        description=des;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(dateOpened);
        dest.writeString(location);
        dest.writeString(description);
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


    //TODO: override .equals()
}
