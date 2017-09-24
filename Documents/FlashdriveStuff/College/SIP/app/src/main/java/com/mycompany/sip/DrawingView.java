package com.mycompany.sip;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by Emily on 9/7/2017.
 */

public class DrawingView extends View {

    private Grid gr, keystone;
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xffffff;
    private int red = 0xff0000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    private String whichTool = "";
    private float startX, startY, endX, endY;
    private ArrayList<String> whatToUndo = new ArrayList<>(); //tells the system what the next thing to undo is
    private ArrayList<Path> toUndoPath = new ArrayList<>();
    private ArrayList<Grid> toUndoGrid = new ArrayList<>();
    private ArrayList<Paint> toUndoPaint = new ArrayList<>();
    private ArrayList<Path> toRedo = new ArrayList<>();
    private ArrayList<Paint> toRedoPaint = new ArrayList<>();
    private float[][] keystonePoints = new float[4][2];
    private float[][] points = new float[4][2];
    private int keystoneCounter=0;
    private float nsDim, ewDim;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }
    private void setupDrawing(){
//get drawing area setup for interaction
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//view given size
        super.onSizeChanged(w, h, oldw, oldh);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

        if(this.whichTool.equals("highlight"))
        {
            drawPaint.setAlpha(100);
            drawPaint.setStrokeWidth(10);
            canvas.drawPath(drawPath, drawPaint);
        }
        else
        {
            String[] dimensions = selectActivity.getUnitDimensions();
            ewDim = Float.parseFloat(dimensions[1]);
            nsDim = Float.parseFloat(dimensions[0]);
            if(this.whichTool.equals("grid"))
            {
                drawPaint.setAlpha(255);
                drawPaint.setStrokeWidth(5);

                points = new float[4][2];
                points[0][0]=startX;
                points[0][1]=startY;
                points[1][0]=endX;
                points[1][1]=startY;
                points[2][0]=endX;
                points[2][1]=endY;
                points[3][0]=startX;
                points[3][1]=endY;

                gr = new Grid("grid", points, nsDim, ewDim, canvas, drawPaint);
                gr.drawGrid();

                /*canvas.drawLine(startX, startY, startX, endY, drawPaint);
                canvas.drawLine(startX, endY, endX, endY, drawPaint);
                canvas.drawLine(endX, endY, endX, startY, drawPaint);
                canvas.drawLine(endX, startY, startX, startY, drawPaint);
                float intervalX = (endX-startX)/(10*nsDim);
                for(int i = 1; i<10*nsDim; i++)
                {
                    canvas.drawLine(startX+(intervalX*i), startY, startX+(intervalX*i), endY, drawPaint);
                }
                float intervalY = (endY-startY)/(10*ewDim);*/
                //for(int i = 1; i<10*ewDim; i++)
                //{
                    /*drawPaint.setColor(paintColor);
                    if(i%10==0)
                    {
                        drawPaint.setColor(red);
                    }*/
                    //canvas.drawLine(startX, startY+(intervalY*i), endX, startY+(intervalY*i), drawPaint);
                //}
            }
            else
            {
                if(this.whichTool.equals("keystone"))
                {
                    dimensions = selectActivity.getUnitDimensions();
                    drawPaint.setAlpha(255);
                    drawPaint.setStrokeWidth(5);
                    System.out.println("actually legit drawing it");

                    float nsDim = Float.parseFloat(dimensions[0]);
                    float ewDim = Float.parseFloat(dimensions[1]);

                    keystone = new Grid("keystone", keystonePoints, nsDim, ewDim, canvas, drawPaint);
                    keystone.drawGrid();

                   /* System.out.println(nsDim + " " + ewDim);

                    float topLength = keystonePoints[1][0]-keystonePoints[0][0];
                    System.out.println(topLength);
                    float botLength = keystonePoints[2][0]-keystonePoints[3][0];
                    System.out.println(botLength);
                    float height = keystonePoints[2][1]-keystonePoints[0][1];
                    float topHeightDiff = keystonePoints[1][1]-keystonePoints[0][1];
                    System.out.println(topHeightDiff);
                    float botHeightDiff = keystonePoints[2][1]-keystonePoints[3][1];
                    System.out.println(botHeightDiff);
                    float leftHeight = keystonePoints[3][1] - keystonePoints[0][1];
                    float rightHeight = keystonePoints[2][1] - keystonePoints[1][1];
                    float leftKeystone = keystonePoints[0][0]-keystonePoints[3][0];
                    System.out.println(leftKeystone);
                    float leftInt = leftKeystone/10;
                    float rightKeystone = keystonePoints[2][0]-keystonePoints[1][0];
                    System.out.println(rightKeystone);
                    float rightInt = rightKeystone/10;

                    //draws top line
                    canvas.drawLine(keystonePoints[0][0], keystonePoints [0][1], keystonePoints[1][0], keystonePoints[1][1], drawPaint);

                    //draws bottom line
                    canvas.drawLine(keystonePoints[3][0], keystonePoints[3][1], keystonePoints[2][0], keystonePoints[2][1], drawPaint);

                    //draws left line
                    canvas.drawLine(keystonePoints[0][0], keystonePoints [0][1], keystonePoints[3][0], keystonePoints[3][1], drawPaint);

                    //draws right line
                    canvas.drawLine(keystonePoints[1][0], keystonePoints[1][1], keystonePoints[2][0], keystonePoints[2][1], drawPaint);

                    //fills in vertical lines
                    //TODO: allow to keystone top and bottom too
                    for(int l=1; l<10*ewDim; l++)
                    {
                        canvas.drawLine(keystonePoints[0][0] + (l*(topLength/(10*ewDim))), (keystonePoints[0][1] + l*(topHeightDiff/(10*ewDim))), keystonePoints[3][0] + (l*(botLength/(10*ewDim))), (keystonePoints[3][1] + l*(botHeightDiff/(10*ewDim))), drawPaint);
                    }

                    //fills in horizontal lines
                    for(int h=1; h<10*nsDim; h++)
                    {
                        canvas.drawLine(keystonePoints[0][0] - (h*(leftKeystone/(10*nsDim))), keystonePoints[0][1]+ h*(leftHeight/(10*nsDim)), keystonePoints[1][0] + (h*(rightKeystone/(10*nsDim))), keystonePoints[0][1] + h*(rightHeight/(10*nsDim)), drawPaint);
                    }*/
                }
            }
        }
        //TODO: figure out how to add grid
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        if(this.whichTool.equals("highlight")) {this.drawPaint.setStrokeWidth(10);
            this.drawPaint.setAlpha(100);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.reset();
                    drawPath.moveTo(touchX, touchY);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    //invalidate();
                    selectActivity.saveLayer();
                    break;
                default:
                    return false;
            }
        }else
        {
            if(this.whichTool.equals("grid")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        endX = event.getX();
                        endY = event.getY();
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        endX = event.getX();
                        endY = event.getY();
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        endX = event.getX();
                        endY = event.getY();
                        selectActivity.saveGrid();
                        //invalidate();
                        break;
                    default:
                        return false;
                }
            }
            else
            {
                if(this.whichTool.equals("keystone")) {
                    System.out.println("keystoning!!!!");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if(keystoneCounter<4) {
                                keystonePoints[keystoneCounter][0] = event.getX();
                                keystonePoints[keystoneCounter][1] = event.getY();
                                keystoneCounter++;
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            if(keystoneCounter==4)
                            {
                                System.out.println("drawing keystone!");
                                //drawKeystone(keystonePoints);
                                invalidate();
                            }
                            break;
                        default:
                            return false;
                    }
                }
            }
        }
        invalidate();
        return true;
    }

    public void setCanvasBitmap(Bitmap im)
    {
        canvasBitmap=im.copy(Bitmap.Config.ARGB_8888, true);
    }


    //Added 9/7/2017 from https://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth;
        int desiredHeight;
        float ratio;
        if(canvasBitmap!=null) {
            desiredWidth = canvasBitmap.getWidth();
            desiredHeight = canvasBitmap.getHeight();
        }
        else
        {
            desiredWidth=100;
            desiredHeight=100;
        }
        ratio= (float)desiredWidth/(float)desiredHeight;

        System.out.println("desiredWidth: " + desiredWidth + " desiredHeight: " + desiredHeight);
        System.out.println("ratio: " + ratio);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            System.out.println("Exactly");
            //Must be this size
            //TODO: ensure that image isn't being cropped
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            System.out.println("At most");
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
            if(width==desiredWidth)
            {
                System.out.println("desired");
            }
            else {
                System.out.println("widthSize");
            }
            desiredHeight=(int) (width/ratio);
        } else {
            System.out.println("Whatever");
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            System.out.println("Height Exactly");
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            System.out.println("Height at most");
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
            if(height==heightSize)
            {
                System.out.println("heightSize");
                width= (int) (height*ratio);
            }
            else
            {
                System.out.println("desiredHeight");
            }
        } else {
            System.out.println("Height whatever");
            //Be whatever you want
            height = desiredHeight;
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(canvasBitmap, width, height, true);
        canvasBitmap=newBitmap;
        System.out.println("height: " + height + " width: " + width);
        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    public void grid()
    {
        this.whichTool="grid";
    }

    public void highlight()
    {
        this.whichTool="highlight";
    }

    public void noDraw()
    {
        this.whichTool="";
    }

    public String getTool()
    {
        return this.whichTool;
    }

    public void undo()
    {
        System.out.println(toUndoPath);
        if(whatToUndo.size()>0) {
            //TODO: fix this to implement grid undoing as well
            //toRedo.add(toUndoPath.remove(toUndoPath.size() - 1));
            //toRedoPaint.add(toUndoPaint.remove(toUndoPaint.size() - 1));
            System.out.println(toRedo.get(toRedo.size() - 1));
        }
        drawPath.reset();
        invalidate();
    }

    public void save()
    {
        Path temp = new Path(drawPath);
        Paint tempPaint = new Paint(drawPaint);
        toUndoPath.add(temp);
        toUndoPaint.add(tempPaint);
        drawCanvas.drawPath(temp, tempPaint);
        /*for(int i=0; i<toUndo.size()-1; i++)
        {
            System.out.println(toUndo.get(i));
            drawCanvas.drawPath(toUndo.get(i), toUndoPaint.get(i));
        }*/
    }

    public void keystone()
    {
        this.whichTool="keystone";
    }

    public void saveGrid()
    {
        System.out.println("saving grid!!!");
        if(gr!=null)
        {
            System.out.println("drawing gr!");
            drawGrid(drawCanvas, drawPaint, points, nsDim, ewDim);
            /*if(toUndoGrid!=null && !toUndoGrid.contains(gr))
            {
                gr.drawGrid();
                whatToUndo.add("grid");
                toUndoGrid.add(gr);
                toUndoPaint.add(drawPaint);
            }*/
        }
        if(keystone!=null)
        {
            System.out.println("Trying to draw keystone");
            keystone.drawGrid();
           /* if(toUndoGrid!=null && !toUndoGrid.contains(keystone))
            {
                keystone.drawGrid();
                whatToUndo.add("grid");
                toUndoGrid.add(keystone);
                toUndoPaint.add(drawPaint);
            }*/
        }

        System.out.println("Drew the thing");
    }

    //will this make save not break?
    public static void drawGrid(Canvas canvas, Paint drawPaint, float[][] points, float nsDim, float ewDim)
    {
        drawPaint.setAlpha(255);
        drawPaint.setStrokeWidth(5);
        System.out.println("actually legit drawing it");

        System.out.println(nsDim + " " + ewDim);

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
        canvas.drawLine(points[0][0], points[0][1], points[1][0], points[1][1], drawPaint);

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
}
