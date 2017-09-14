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

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xffffff;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    private String whichTool = "";
    private float startX, startY, endX, endY;
    private ArrayList<Path> toUndo = new ArrayList<>();
    private ArrayList<Paint> toUndoPaint = new ArrayList<>();
    private ArrayList<Path> toRedo = new ArrayList<>();
    private ArrayList<Paint> toRedoPaint = new ArrayList<>();
    private float[][] keystonePoints = new float[4][2];
    private int keystoneCounter=0;

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
            if(this.whichTool.equals("grid"))
            {
                drawPaint.setAlpha(255);
                drawPaint.setStrokeWidth(5);
                canvas.drawLine(startX, startY, startX, endY, drawPaint);
                canvas.drawLine(startX, endY, endX, endY, drawPaint);
                canvas.drawLine(endX, endY, endX, startY, drawPaint);
                canvas.drawLine(endX, startY, startX, startY, drawPaint);
                float intervalX = (endX-startX)/10;
                for(int i = 1; i<=9; i++)
                {
                    canvas.drawLine(startX+(intervalX*i), startY, startX+(intervalX*i), endY, drawPaint);
                }
                float intervalY = (endY-startY)/10;
                for(int i = 1; i<=9; i++)
                {
                    canvas.drawLine(startX, startY+(intervalY*i), endX, startY+(intervalY*i), drawPaint);
                }
            }
            else
            {
                if(this.whichTool.equals("keystone"))
                {
                    drawPaint.setAlpha(255);
                    drawPaint.setStrokeWidth(5);
                    System.out.println("actually legit drawing it");
                    float topLength = keystonePoints[1][0]-keystonePoints[0][0];
                    System.out.println(topLength);
                    float botLength = keystonePoints[2][0]-keystonePoints[3][0];
                    System.out.println(botLength);
                    float height = keystonePoints[2][1]-keystonePoints[1][1];
                    System.out.println(height);
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
                    for(int l=1; l<=9; l++)
                    {
                        canvas.drawLine(keystonePoints[0][0] + (l*(topLength/10)), keystonePoints[0][1], keystonePoints[3][0] + (l*(botLength/10)), keystonePoints[3][1], drawPaint);
                    }

                    //fills in horizontal lines
                    for(int h=1; h<=9; h++)
                    {
                        canvas.drawLine(keystonePoints[0][0] - (h*(leftKeystone/10)), keystonePoints[0][1]+ h*(height/10), keystonePoints[1][0] + (h*(rightKeystone/10)), keystonePoints[0][1] + h*(height/10), drawPaint);
                    }
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
                    //toUndo.add(drawPath);
                    //toUndoPaint.add(drawPaint);
                    //invalidate();
                    //TODO: add undo implementation
                    //TODO: pop up dialog asking user to save this highlight
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
                        invalidate();
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
        ratio=desiredWidth/desiredHeight;

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
        } else {
            System.out.println("Height whatever");
            //Be whatever you want
            height = desiredHeight;
        }

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
        System.out.println(toUndo);
        if(toUndo.size()>0) {
            toRedo.add(toUndo.remove(toUndo.size() - 1));
            toRedoPaint.add(toUndoPaint.remove(toUndoPaint.size() - 1));
            System.out.println(toRedo.get(toRedo.size() - 1));
        }
        drawPath.reset();
        invalidate();
    }

    public void save()
    {
        Path temp = new Path(drawPath);
        Paint tempPaint = new Paint(drawPaint);
        toUndo.add(temp);
        toUndoPaint.add(tempPaint);
        drawCanvas.drawPath(temp, tempPaint);
        /*for(int i=0; i<toUndo.size()-1; i++)
        {
            System.out.println(toUndo.get(i));
            drawCanvas.drawPath(toUndo.get(i), toUndoPaint.get(i));
        }*/
    }

    public void drawKeystone(float[][] points)
    {
        float topLength = points[0][0]-points[1][0];
        float botLength = points[3][0]-points[2][0];
        float height = points[0][1]-points[2][1];
        float leftKeystone = points[0][0]-points[3][0];
        float rightKeystone = points[2][0]-points[1][0];

        //draws top line
        drawCanvas.drawLine(points[0][0], points [0][1], points[1][0], points[1][1], drawPaint);

        //draws bottom line
        drawCanvas.drawLine(points[3][0], points[3][1], points[2][0], points[2][1], drawPaint);

        //draws left line
        drawCanvas.drawLine(points[0][0], points [0][1], points[3][0], points[3][1], drawPaint);

        //draws right line
        drawCanvas.drawLine(points[1][0], points[1][1], points[2][0], points[2][1], drawPaint);

        //fills in vertical lines
        for(int l=1; l<9; l++)
        {
            drawCanvas.drawLine(points[0][0] + (l*(topLength/10)), points[0][1], points[3][0] + (l*(botLength/10)), points[3][1], drawPaint);
        }

        //fills in horizontal lines
        for(int h=1; h<9; h++)
        {
            drawCanvas.drawLine(points[0][0] - (h*(leftKeystone/10)), points[0][1]+ h*(height/10), points[2][0] + (h*(rightKeystone/10)), points[0][1] + h*(height/10), drawPaint);
        }
    }

    public void keystone()
    {
        this.whichTool="keystone";
    }
}
