package com.mycompany.sip;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Sites
 */

public class Site {

    private String name;
    private String number;
    private String dateOpened;
    private String location;
    private String description;

    public Site(String n, String num, String date, String loc, String desc)
    {
        name=n;
        number=num;
        dateOpened=date;
        location=loc;
        description=desc;
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
}
