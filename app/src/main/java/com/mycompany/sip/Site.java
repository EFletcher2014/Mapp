package com.mycompany.sip;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

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
    private int pk;
    private int remotePK;
    private Timestamp lastUpdated;
    private Timestamp firstCreated;

    public Site(String n, String num, int pk, Timestamp created)
    {
        this.name=n;
        this.number=num;
        this.pk=pk;
        this.firstCreated=created;
    }
    public Site(String n, String num, String date, String loc, String desc, int p, int rpk, Timestamp created, Timestamp updated)
    {
        this.name=n;
        this.number=num;
        this.dateOpened=date;
        this.location=loc;
        this.description=desc;
        this.pk=p;
        this.remotePK=rpk;
        this.firstCreated=created;
        this.lastUpdated=updated;
    }

    public Site(Parcel in) {
        this.name = in.readString();
        this.number = in.readString();
        this.dateOpened = in.readString();
        this.location = in.readString();
        this.description = in.readString();
        this.pk = in.readInt();
        this.remotePK = in.readInt();
        this.firstCreated=new Timestamp(in.readLong()); //TODO: Will this work?
        this.lastUpdated=new Timestamp(in.readLong()); //TODO: same
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

    public Timestamp getFirstCreated() { return firstCreated; }

    public Timestamp getLastUpdated() { return lastUpdated; }

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

    public void setLastUpdated(Timestamp t) { lastUpdated=t; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(dateOpened);
        dest.writeString(location);
        dest.writeString(description);
        dest.writeInt(pk);
        dest.writeInt(remotePK);
        dest.writeLong(firstCreated.getTime());
        dest.writeLong(lastUpdated.getTime());
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

    public int getPk(){
        return pk;
    }

    public int getRemotePK() { return remotePK; }

    public void setRemotePK(int rpk) { remotePK = rpk; }
    @Override
    public boolean equals(Object o)
    {
        try {
            String num = ((Site) o).getNumber();
            return this.getNumber().equals(num);
        }catch(Exception e)
        {
            return false;
        }
    }
}
