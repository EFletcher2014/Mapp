package com.mycompany.sip;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Emily on 9/21/2017.
 * Class to draw a grid on a given canvas
 */

public class Grid {

    private String gridType;
    private float[][] points = new float[4][2];
    private float nsDim;
    private float ewDim;
    private Canvas canvas;
    private Paint drawPaint;
    private boolean isDrawn=false;

    public Grid (String type, float[][] p, float nD, float eD, Canvas c, Paint dP)
    {
        this.gridType=type;
        this.points=p;
        this.nsDim=nD;
        this.ewDim=eD;
        this.canvas=c;
        this.drawPaint=dP;
    }

    public void drawGrid()
    {
        drawPaint.setAlpha(255);
        drawPaint.setStrokeWidth(5);
        System.out.println("actually legit drawing it");

        System.out.println(this.nsDim + " " + this.ewDim);

        float topLength = points[1][0]-points[0][0];
        System.out.println(" tL: " + topLength);
        float botLength = points[2][0]-points[3][0];
        System.out.println(" bL: " + botLength);
        float height = points[2][1]-points[0][1];
        float topHeightDiff = points[1][1]-points[0][1];
        System.out.println(" thd: " + topHeightDiff);
        float botHeightDiff = points[2][1]-points[3][1];
        System.out.println(" btd: " + botHeightDiff);
        float leftHeight = points[3][1] - points[0][1];
        float rightHeight = points[2][1] - points[1][1];
        float leftKeystone = points[0][0]-points[3][0];
        System.out.println(" lk: " + leftKeystone);
        float leftInt = leftKeystone/10;
        float rightKeystone = points[2][0]-points[1][0];
        System.out.println(" rk: " + rightKeystone);
        float rightInt = rightKeystone/10;


        //TODO: figure out why this isn't working from saveGrid, but is working from onDraw
        //draws top line
        this.canvas.drawLine(this.points[0][0], this.points[0][1], this.points[1][0], this.points[1][1], this.drawPaint);

        System.out.println("DRAW*******************************************************************************************************************************************************************************************");

        //draws bottom line
        canvas.drawLine(points[3][0], points[3][1], points[2][0], points
                [2][1], drawPaint);

        //draws left line
        canvas.drawLine(points[0][0], points[0][1], points[3][0], points[3][1], drawPaint);

        //draws right line
        canvas.drawLine(points[1][0], points[1][1], points[2][0], points[2][1], drawPaint);

        //fills in vertical lines
        //TODO: allow to keystone top and bottom too
        for(int l=1; l<10*ewDim; l++)
        {
            canvas.drawLine(points[0][0] + (l*(topLength/(10*ewDim))), (points[0][1] + l*(topHeightDiff/(10*ewDim))), points[3][0] + (l*(botLength/(10*ewDim))), (points[3][1] + l*(botHeightDiff/(10*ewDim))), drawPaint);
        }
        System.out.println("Drew the vertical ones!");

        //fills in horizontal lines
        for(int h=1; h<10*nsDim; h++)
        {
            canvas.drawLine(points[0][0] - (h*(leftKeystone/(10*nsDim))), points[0][1]+ h*(leftHeight/(10*nsDim)), points[1][0] + (h*(rightKeystone/(10*nsDim))), points[0][1] + h*(rightHeight/(10*nsDim)), drawPaint);
        }
        System.out.println("DONE!!!");
    }

    public boolean isDrawn()
    {
        return this.isDrawn;
    }


}
