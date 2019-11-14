package com.czt.pencheck;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BottomMenuView extends View {

    private static final String TAG = "BottomMenuView";

    private List<Bitmap> icons = new ArrayList<>();
    private boolean isShowing = false;

    private ValueAnimator showSectorAnimator;
    private ValueAnimator hideSectorAnimator;

    private int width;
    private int height;
    private Paint sectorPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint strokePaint;

    private Bitmap shutDownIcon;

    private double childRadian;
    private float childAngle;

    private float ratio = 0.5f;
    private Region totalRegion;

    private PathRegion hideRegion;

    private List<PathRegion> pathRegionList = new ArrayList<>();



    public BottomMenuView(Context context) {
        this(context, null);
    }

    public BottomMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        totalRegion = new Region(0, 0, w, h);
        divideArea();

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        sectorPaint = new Paint();
        sectorPaint.setAntiAlias(true);
        sectorPaint.setDither(true);
        sectorPaint.setColor(Color.parseColor("#B31A1A1A"));
        sectorPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.WHITE);
    }

    /**
     * 初始化
     */
    private void init(){

        shutDownIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.shut_down);

        showSectorAnimator = ValueAnimator.ofFloat(0f, 1.2f);
        showSectorAnimator.setDuration(300);
        showSectorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (val > 0 && val <= 1) {
                    clearCanvas();
                    drawContent(val);
                    if (val >= 0.8) {
                        drawIcons(val);
                    }
                    invalidate();
                } else if (val > 1 && val <= 1.2f) {
                    clearCanvas();
                    drawContent(1);
                    drawIcons(val);
                    invalidate();
                }
                if (val == 1.2f) isShowing = true;
            }
        });


        hideSectorAnimator = ValueAnimator.ofFloat(1f, 0f);
        hideSectorAnimator.setDuration(300);
        hideSectorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (val > 0 && val <= 1) {
                    clearCanvas();
                    drawContent(val);
                    if (val >= 0.8) {
                        drawIcons(val);
                    }
                    invalidate();
                } else if (val > 1 && val <= 1.2f) {
                    clearCanvas();
                    drawContent(1);
                    drawIcons(val);
                    invalidate();
                }
                if (val == 0) isShowing = false;
            }
        });

    }

    /**
     * 绘制扇形区域
     * @param val 缩放大小
     */
    private void drawContent(float val) {
        float len = width * val;
        float oldLen = len;
        RectF oval = new RectF(width - len, height - len,
                width + len, height + len);

        len = len * ratio;
        RectF halfOval = new RectF(width - len, height - len,
                width + len, height + len);
        bitmapCanvas.drawArc(oval, 180, 90,
                true, sectorPaint);

        bitmapCanvas.drawArc(halfOval, 180, 90,
                true, sectorPaint);

        Path path = new Path();
        path.addArc(oval, 180, 90);
        path.addArc(halfOval, 180, 90);

        drawSplitLine(oldLen);

        bitmapCanvas.drawPath(path, strokePaint);


    }

    /**
     * 绘制图标
     * @param scale 缩放大小
     */
    private void drawIcons(float scale) {

        float len = width * scale;
        float divideRadian = (float) (childRadian / 2);
        float iconLen = (float) (0.42 * (1 + ratio) * len);

        float iconWidth = 0;
        float iconHeight = 0;

        if (icons.size() != 0) {
            iconWidth = icons.get(0).getWidth();
            iconHeight = icons.get(0).getHeight();
        }

        if (scale > 1) scale = 1;

        Matrix shutDownMatrix = new Matrix();

        float sX = width - width * ratio * 0.35f - shutDownIcon.getWidth() * 0.5f;
        float sY = width - width * ratio * 0.35f - shutDownIcon.getWidth() * 0.5f;

        shutDownMatrix.postTranslate(sX, sY);
        shutDownMatrix.postScale(scale, scale, sX, sY);

        bitmapCanvas.drawBitmap(shutDownIcon, shutDownMatrix, sectorPaint);

        for (int i = 0; i < icons.size(); i++) {
            float iconX = width - iconLen * (float) Math.cos((childRadian * i) + divideRadian);
            float iconY = (float) (height - iconLen * Math.sin(childRadian * i + divideRadian));

            Matrix matrix = new Matrix();
            matrix.postTranslate(iconX - iconWidth * scale * 0.5f,
                    iconY - iconHeight * scale * 0.5f);
            matrix.postScale(scale, scale, width, height);
            bitmapCanvas.drawBitmap(icons.get(i), matrix, sectorPaint);
        }

    }

    public void addIcons(@NonNull List<Integer> iconIds){
        for (Integer id : iconIds){
            Bitmap icon = BitmapFactory.decodeResource(getResources(), id);
            icons.add(icon);
        }

        childRadian = Math.PI / (2 * iconIds.size());
        childAngle = 90 / iconIds.size();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if (width > height) {
            width = height;
        } else {
            height = width;
        }

        setMeasuredDimension(width, height);
    }

    private void divideArea() {

        RectF oval = new RectF(0, 0, width * 2, height * 2);
        RectF halfOval = new RectF(width - width * ratio, height - height * ratio,
                width + width * ratio, height + height * ratio);

        hideRegion = new PathRegion();
        Path hidePath = new Path();
        hidePath.moveTo(width, height);
        hidePath.arcTo(halfOval, 180, 90);
        hideRegion.path = hidePath;

        for (int i = 0; i < icons.size(); i++) {
            Path path = new Path();
            path.moveTo(width, height);

            path.arcTo(oval, 180 + childAngle * i, childAngle);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                path.op(hidePath, Path.Op.DIFFERENCE);
            }

            Region region = new Region();
            region.setPath(path, totalRegion);
            PathRegion pathRegion = new PathRegion();
            pathRegion.path = path;
            pathRegion.region = region;
            pathRegionList.add(pathRegion);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, sectorPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isShowing && !isAnimatorRunning()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    onDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    onMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onUp();
                    break;
            }
        }
        return true;
    }

    private void onDown(MotionEvent event) {
        clearCanvas();
        drawContent(1);
        float x = event.getX();
        float y = event.getY();
        for (PathRegion pathRegion : pathRegionList) {
            if (pathRegion.region.contains((int) x, (int) y)){
                bitmapCanvas.drawPath(pathRegion.path, sectorPaint);
            }
        }

        invalidate();
    }

    private void onUp() {
        clearCanvas();
        drawContent(1);
        invalidate();
    }

    private void onMove(MotionEvent event){
        clearCanvas();
        drawContent(1);
        float x = event.getX();
        float y = event.getY();
        for (PathRegion pathRegion : pathRegionList) {
            if (pathRegion.region.contains((int) x, (int) y)){
                bitmapCanvas.drawPath(pathRegion.path, sectorPaint);
            }
        }

        invalidate();

    }

    public void showMenu() {
        if (!isShowing) {
            showSectorAnimator.start();
        }
    }

    public void hideMenu() {
        if (isShowing) {
            hideSectorAnimator.start();
        }
    }

    /**
     * 清除画布
     */
    private void clearCanvas() {
        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        bitmapCanvas.drawPaint(p);
    }

    public boolean isShowing() {
        return isShowing;
    }

    public boolean isAnimatorRunning(){
        return showSectorAnimator.isRunning() || hideSectorAnimator.isRunning();
    }

    private void drawSplitLine(float r){

        for (int i = 1; i < icons.size(); i++) {
            float temp = r;

            float outX = (float) (width - temp * Math.cos(childRadian * i));
            float outY = (float) (height - temp * Math.sin(childRadian * i));

            temp *= ratio;

            float inX = (float) (width - temp * Math.cos(childRadian * i));
            float inY = (float) (height - temp * Math.sin(childRadian * i));

            bitmapCanvas.drawLine(inX, inY, outX, outY, strokePaint);

        }

    }

    private class PathRegion{
        Path path;
        Region region;

        @NonNull
        @Override
        public String toString() {
            return path.toString() + region.toString();
        }
    }
}
