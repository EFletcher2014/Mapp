package com.mycompany.sip;

/**
 * Created by Emily Fletcher on 8/30/2017.
 * Model Object for Units
 */
public class Unit {

    private String datum;
    private String dateOpened;
    private String nsDimension;
    private String ewDimension;
    private Site site;
    private String excavators;
    private String reasonForOpening;

    public Unit(String dat, String date, String nsDim, String ewDim, Site st, String exc, String reas)
    {
        datum=dat;
        dateOpened=date;
        nsDimension=nsDim;
        ewDimension=ewDim;
        site=st;
        excavators=exc;
        reasonForOpening=reas;

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

}
