package com.mycompany.sip;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
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

    //drawing path
    private int dstWidth;
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

    private int width;
    private int height;

    private Uri selectedImageUri;

    //Level map
    private static WeakReference<LevelMap> levelMapActivityRef;

    public static void updateLevelMapActivity(LevelMap activity) {
        levelMapActivityRef = new WeakReference<LevelMap>(activity);
    }

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
        Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawBitmap(canvasBitmap, null, rect, canvasPaint);
        if(this.whichTool.equals("highlight"))
        {
            drawPaint.setAlpha(100);
            drawPaint.setStrokeWidth(10);
            canvas.drawPath(drawPath, drawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    //detect user touch
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
        canvasBitmap = im.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void setCanvasBitmap()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImageUri.getPath(), options);

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        dstWidth = srcWidth;

        float scale = (float) srcWidth / srcHeight;

        if (srcWidth > srcHeight && srcWidth > width) {
            dstWidth = width;
        } else if (srcHeight > srcWidth && srcHeight > height) {
            dstWidth = (int) (height * scale);
        }

        options.inJustDecodeBounds = false;
        options.inDensity = options.outWidth;
        options.inTargetDensity = dstWidth;
        options.inScaled = true;
        options.inMutable = true;

        canvasBitmap = BitmapFactory.decodeFile(selectedImageUri.getPath(), options);
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
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(selectedImageUri.getPath(), options);

            desiredWidth = options.outWidth;
            desiredHeight = options.outHeight;
        }
        ratio= (float)desiredWidth/(float)desiredHeight;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
            desiredHeight=(int) (width/ratio);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
            if(height==heightSize)
            {
                width= (int) (height*ratio);
            }
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        if(selectedImageUri != null)
        {
            setCanvasBitmap();
        }
        //MUST CALL THIS
        setMeasuredDimension(width, height);
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
        if(canvasBitmap!=null) {
            this.setCanvasBitmap(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
        }
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawCanvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        drawPath.reset();
        invalidate();
    }

    public void save(Bitmap tempBm)
    {
        canvasBitmap=tempBm.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void setUri(Uri uri)
    {
        selectedImageUri = uri;
    }
}
