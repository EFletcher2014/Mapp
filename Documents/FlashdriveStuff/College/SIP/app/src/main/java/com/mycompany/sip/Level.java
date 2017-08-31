package com.mycompany.sip;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for levels
 */
public class Level {
    private int number;
    private double begDepth;
    private double endDepth;
    private Site site;
    private Unit unit;
    private String dateStarted;
    private String excavationMethod;

    public Level (int n, double bD, double eD, Site s, Unit u, String date, String excM)
    {
        number=n;
        begDepth=bD;
        endDepth=eD;
        site=s;
        unit=u;
        dateStarted=date;
        excavationMethod=excM;
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

}