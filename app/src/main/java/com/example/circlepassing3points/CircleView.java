package com.example.circlepassing3points;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class CircleView extends View {

    public static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;
    public int counter = 0;

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        paintScreen = new Paint();
        paintLine = new Paint();

        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        pathMap = new HashMap<>();
        previousPointMap = new HashMap<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
      bitmapCanvas = new Canvas(bitmap);
      bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //drawing a circle statically
//        canvas.drawCircle(getMeasuredWidth()/2, getMeasuredHeight()/2, 78, paintLine);
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (Integer key: pathMap.keySet()) {
            canvas.drawPath(pathMap.get(key), paintLine);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();   //event type
        int actionIndex = event.getActionIndex();   //pointer finger

        if(action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_POINTER_UP) {
            touchStarted(event.getX(actionIndex),
                    event.getY(actionIndex),
                    event.getPointerId(actionIndex));
        } else if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_POINTER_UP){
            touchEnded(event.getPointerId(actionIndex));
        } else {
            touchMoved(event);
        }
        invalidate();   //redraw the screen

        return true;
    }

    private void touchMoved(MotionEvent event) {

        for (int i = 0; i < event.getPointerCount(); i++) {

            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if(pathMap.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);

                    //Move the path to new location
                    path.quadTo(point.x, point.y,
                            (newX + point.x)/2,
                            (newY + point.y)/2);

                    //Store the new coordinates
                    point.x = (int) newX;
                    point.y = (int) newY;

            }
        }
    }

    private void touchEnded(int pointerId) {
        Path path = pathMap.get(pointerId);     //get the corresponding path
        bitmapCanvas.drawPath(path, paintLine);     //draw to bitmap canvas
        path.reset();
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path;  //store the path for given touch
        Point point;    //store last point in path

         if(pathMap.containsKey(pointerId)) {
             path = pathMap.get(pointerId);
             point = previousPointMap.get(pointerId);
         } else {
             path = new Path();
             pathMap.put(pointerId, path);
             point = new Point();
             previousPointMap.put(pointerId, point);
         }

        int mx = (int) ((x + point.x) / 2);
        int my = (int) ((y + point.y) / 2);
        float radius = (float) Math.sqrt(Math.pow(point.x - x, 2)
                + Math.pow(point.y - y, 2));

        //move to the coordinates of the touch
         path.moveTo(x, y);
         point.x = (int) x;
         point.y = (int) y;

        counter++;
        if(counter >= 3) {
            counter = 0;
            bitmapCanvas.drawCircle(mx,my, radius/2, paintLine);
        }
    }
}
