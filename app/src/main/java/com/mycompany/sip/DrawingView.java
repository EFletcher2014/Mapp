package com.mycompany.sip;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Emily on 9/7/2017.
 */

public class DrawingView extends View {

    private Grid gr, keystone;
    //drawing path
    private Path drawPath, gridPath, keyPath;
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
    //private ArrayList<Bitmap> toUndo = new ArrayList<>();
    private ArrayList<Bitmap> toRedo = new ArrayList<>();
    private float[][] keystonePoints = new float[4][2];
    private float[][] points = new float[4][2];
    private int keystoneCounter=0;
    private float nsDim, ewDim;
    private Bitmap temp;
    private Bitmap toUndo;

    //Level map
    private static WeakReference<LevelMap> levelMapActivityRef;

    public static void updateLevelMapActivity(LevelMap activity) {
        levelMapActivityRef = new WeakReference<LevelMap>(activity);
    }

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        System.out.println("Initializing DrawingView");
        setupDrawing();
    }
    private void setupDrawing(){
//get drawing area setup for interaction
        System.out.println("setting up drawing");
        drawPath = new Path();
        gridPath = new Path();
        keyPath = new Path();
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
        System.out.println("size changed!");
        super.onSizeChanged(w, h, oldw, oldh);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//draw view
        System.out.println("on draw!");
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        if(this.whichTool.equals("highlight"))
        {
            drawPaint.setAlpha(100);
            drawPaint.setStrokeWidth(10);
            canvas.drawPath(drawPath, drawPaint);
        }
        else
        {
            //TODO: should we allow dimensions to be floats?
            int[] dimensions = levelMapActivityRef.get().getUnitDimensions();
            ewDim = dimensions[1];
            nsDim = dimensions[0];
            if(this.whichTool.equals("grid"))
            {
                canvas.drawPath(gridPath, drawPaint);
            }
            else
            {
                if(this.whichTool.equals("keystone"))
                {
                    System.out.println("actually legit drawing it");
                    canvas.drawPath(keyPath, drawPaint);
                }
            }
        }
        //TODO: figure out how to add grid
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    //detect user touch
        System.out.println("on touch event!");
        float touchX = event.getX();
        float touchY = event.getY();
        if(this.whichTool.equals("highlight")) {
            this.drawPaint.setStrokeWidth(10);
            this.drawPaint.setAlpha(100);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    //invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    return false;
            }
        }
        invalidate();
        return true;
    }

    public void setCanvasBitmap(Bitmap im)
    {
        canvasBitmap=im.copy(Bitmap.Config.ARGB_8888, true);
        toUndo = im.copy(Bitmap.Config.ARGB_8888, true);
        System.out.println("New Canvas Bitmap: " + canvasBitmap);
    }


    //Added 9/7/2017 from https://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("Measuring!");
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
        toUndo=newBitmap.copy(Bitmap.Config.ARGB_8888, true);
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
        if(toUndo!=null/*toUndo.size()>0*/) {
            //TODO: fix this to implement grid undoing as well
            System.out.println("Undo: " + toUndo);
            System.out.println("Current Bitmap: " + canvasBitmap);
            //toRedo.add(toUndo.get(toUndo.size()-1));
            //System.out.println("Redo: " + toRedo.get(toRedo.size() - 1));
            this.setCanvasBitmap(toUndo.copy(Bitmap.Config.ARGB_8888, true));
        }

        /*if(gr!=null)
        {
            Paint temp1 = new Paint();
            temp1.setColor(Color.TRANSPARENT);
            temp1.setXfermode(new PorterDuffXfermode(
                    PorterDuff.Mode.CLEAR));
            gr.drawGrid(temp1);
        }*/
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        System.out.println("Canvas bitmap is what it is meant to be?" + canvasBitmap.equals(toUndo));
        drawCanvas.drawBitmap(toUndo, 0, 0, canvasPaint);
        drawPath.reset();
        gridPath.reset();
        gr = null;
        keyPath.reset();
        keystone = null;
        keystoneCounter=0;
        invalidate();
    }

    public void save(Bitmap tempBm)
    {
        toUndo=tempBm.copy(Bitmap.Config.ARGB_8888, true);
        canvasBitmap=tempBm.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void keystone()
    {
        this.whichTool="keystone";
    }

    public Path drawGrid()
    {
        points = new float[4][2];
        points[0][0]=startX;
        points[0][1]=startY;
        points[1][0]=endX;
        points[1][1]=startY;
        points[2][0]=endX;
        points[2][1]=endY;
        points[3][0]=startX;
        points[3][1]=endY;

        gr = new Grid("grid", points, nsDim, ewDim, /*canvas*/ drawPaint);
        return gr.drawGrid(drawPaint);
    }
}
