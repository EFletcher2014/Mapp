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

        //TODO: add undo implementation
        /*for(int i=0; i<toUndo.size(); i++)
        {
            canvas.drawPath(toUndo.get(i), toUndoPaint.get(i));
        }*/
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
        }
        toUndo.add(drawPath);
        toUndoPaint.add(drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        if(this.whichTool.equals("highlight")) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    invalidate();
                    //TODO: pop up dialog asking user to save this highlight
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
        if(canvasBitmap!=null) {
            desiredWidth = canvasBitmap.getWidth();
            desiredHeight = canvasBitmap.getHeight();
        }
        else
        {
            desiredWidth=100;
            desiredHeight=100;
        }

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
        toRedo.add(toUndo.get(toUndo.size()-1));
        toUndo.remove(toUndo.size()-1);
        toRedoPaint.add(toUndoPaint.get(toUndoPaint.size()-1));
        toUndoPaint.remove(toUndoPaint.size()-1);
    }
}
