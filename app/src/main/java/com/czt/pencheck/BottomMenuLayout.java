package com.czt.pencheck;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BottomMenuLayout extends FrameLayout {

    private static final String TAG = "BottomMenuView";

    private boolean isShowing = false;

    private int width;
    private int height;

    private OnItemClickListener onItemClickListener;
    private OnShutDownClickListener onShutDownClickListener;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    //分割线画笔
    private Paint strokePaint;
    //扇形填充画笔
    private Paint sectorPaint;
    //扇形蒙版画笔
    private Paint maskPaint;

    //判断控件是否在右下角
    private boolean isRight = true;

    //子扇形夹角弧度
    private double childRadian;
    //子扇形夹角角度
    private float childAngle;

    //内扇形半径比值
    private float ratio = 0.5f;

    //整块画布区域
    private Region totalRegion;

    //所有点击区域的扇形path和region
    private List<PathRegion> pathRegionList = new ArrayList<>();

    //展示图标的动画
    private AnimatorSet showIconAnimatorSet;
    //隐藏图标的动画
    private AnimatorSet hideIconAnimatorSet;

    //展示扇形的动画
    private ValueAnimator showSectorAnimator;
    //隐藏扇形的动画
    private ValueAnimator hideSectorAnimator;


    public BottomMenuLayout(Context context) {
        this(context, null);
    }

    public BottomMenuLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomMenuLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomMenuLayout);

        isRight = typedArray.getBoolean(R.styleable.BottomMenuLayout_isRight, true);

        typedArray.recycle();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        totalRegion = new Region(0, 0, w, h);
        divideArea();

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        init();
    }

    /**
     * 初始化
     */
    private void init(){

        //初始化画笔
        sectorPaint = new Paint();
        sectorPaint.setAntiAlias(true);
        sectorPaint.setDither(true);
        sectorPaint.setColor(Color.parseColor("#B31A1A1A"));
        sectorPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);
        strokePaint.setStrokeWidth(2);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.parseColor("#1AFFFFFF"));

        maskPaint = new Paint();
        maskPaint.setAntiAlias(true);
        maskPaint.setDither(true);
        maskPaint.setColor(Color.parseColor("#0BFFFFFF"));
        maskPaint.setStyle(Paint.Style.FILL);


        showSectorAnimator = ValueAnimator.ofFloat(0f, 1f);
        showSectorAnimator.setDuration(300);
        showSectorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (val > 0 && val <= 1) {
                    clearCanvas();
                    drawContent(val);
                    invalidate();
                }
                if (val == 1) isShowing = true;
            }
        });

        hideSectorAnimator = ValueAnimator.ofFloat(1f, 0f);
        hideSectorAnimator.setDuration(300);
        hideSectorAnimator.setStartDelay(60);
        hideSectorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (val > 0 && val <= 1) {
                    clearCanvas();
                    drawContent(val);
                    invalidate();
                }
                if (val == 0) isShowing = false;
            }
        });

        showIconAnimatorSet = createAnimatorSet(true);
        showIconAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    view.setScaleX(0);
                    view.setScaleY(0);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        showIconAnimatorSet.setStartDelay(60);

        hideIconAnimatorSet = createAnimatorSet(false);

    }

    /**
     * 根据是否显示来构造组合动画
     * @param isShow 是否显示
     * @return 组合动画
     */
    private AnimatorSet createAnimatorSet(boolean isShow) {

        AnimatorSet resultSet;
        int cnt = getChildCount() - 1;
        childRadian = Math.PI / (2 * cnt);

        float iconLen = (float) (0.5 * (1 + ratio) * width);
        float divideRadian = (float) (childRadian / 2);

        float startX = width - width * ratio * 0.4f;
        float startY = height - height * ratio * 0.4f;

        if (!isRight) {
            startX = width * ratio * 0.4f;
        }

        List<Animator> animatorList = new ArrayList<>();

        float startScale = 0;
        float endScale = 1;

        if (!isShow) {
            float temp = startScale;
            startScale = endScale;
            endScale = temp;
        }

        for (int i = 0; i < cnt; i++) {
            View child = getChildAt(i);
            child.setVisibility(INVISIBLE);

            float iconX = width - iconLen * (float) Math.cos((childRadian * i) + divideRadian) -
                    child.getMeasuredWidth() * 0.5f;

            if (!isRight) {
                iconX = iconLen * (float) Math.cos((childRadian * i) + divideRadian) -
                        child.getMeasuredWidth() * 0.5f;
            }

            float iconY = (float) (height - iconLen * Math.sin(childRadian * i + divideRadian)) -
                    child.getMeasuredHeight() * 0.5f;

            float iconStartX = startX - child.getMeasuredWidth() * 0.5f;
            float iconStartY = startY - child.getMeasuredHeight() * 0.5f;

            //根据显示隐藏来确定是否交换起始点与终点的值
            if (!isShow) {
                float temp = iconX;
                iconX = iconStartX;
                iconStartX = temp;

                temp = iconY;
                iconY = iconStartY;
                iconStartY = temp;
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(child, "translationX",iconStartX, iconX),
                    ObjectAnimator.ofFloat(child, "translationY",iconStartY, iconY),
                    ObjectAnimator.ofFloat(child, "scaleX",startScale, endScale),
                    ObjectAnimator.ofFloat(child, "scaleY",startScale, endScale)
            );
            animatorList.add(set);
        }

        AnimatorSet menuAnimatorSet = new AnimatorSet();
        menuAnimatorSet.setDuration(300);
        menuAnimatorSet.playTogether(animatorList);

        AnimatorSet shutDownAnimatorSet = new AnimatorSet();
        shutDownAnimatorSet.setDuration(300);
        View view = getChildAt(getChildCount() - 1);
        view.setVisibility(INVISIBLE);
        view.setPivotX(view.getMeasuredWidth() * 0.5f);
        view.setPivotY(view.getMeasuredHeight() * 0.5f);

        float shutDownX = width - width * ratio * 0.4f - view.getMeasuredWidth() * 0.5f;
        float shutDownY = width - width * ratio * 0.4f - view.getMeasuredHeight() * 0.5f;

        if (!isRight) {
            shutDownX = width * ratio * 0.4f - view.getMeasuredWidth() * 0.5f;
        }

        shutDownAnimatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "translationX",shutDownX, shutDownX),
                ObjectAnimator.ofFloat(view, "translationY",shutDownY, shutDownY),
                ObjectAnimator.ofFloat(view, "scaleX", startScale, endScale),
                ObjectAnimator.ofFloat(view, "scaleY", startScale, endScale)
        );

        resultSet = new AnimatorSet();
        resultSet.playTogether(menuAnimatorSet, shutDownAnimatorSet);

        return resultSet;
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

        if (!isRight) {
            oval = new RectF(-len, height - len, len, height + len);
        }

        len = len * ratio;
        RectF halfOval = new RectF(width - len, height - len,
                width + len, height + len);

        if (!isRight) {
            halfOval = new RectF(-len, height - len, len, height + len);
        }

        float startAngle = 180;

        if (!isRight) startAngle = 270;

        bitmapCanvas.drawArc(oval, startAngle, 90,
                true, sectorPaint);

        bitmapCanvas.drawArc(halfOval, startAngle, 90,
                true, sectorPaint);

        Path path = new Path();
        path.addArc(oval, startAngle, 90);
        path.addArc(halfOval, startAngle, 90);

        drawSplitLine(oldLen);

        bitmapCanvas.drawPath(path, strokePaint);
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

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(width, height);
    }

    /**
     * 分割区域
     */
    private void divideArea() {

        childRadian = Math.PI / (2 * (getChildCount() - 1));
        childAngle = 90 / (getChildCount() - 1);

        RectF oval = new RectF(0, 0, width * 2, height * 2);
        RectF halfOval = new RectF(width - width * ratio, height - height * ratio,
                width + width * ratio, height + height * ratio);

        if (!isRight) {
            oval = new RectF(-width, 0, width, height * 2);
            halfOval = new RectF(- width * ratio, height - height * ratio,
                    width * ratio, height + height * ratio);
        }

        PathRegion hideRegion = new PathRegion();
        Path hidePath = new Path();

        if (isRight) {
            hidePath.moveTo(width, height);
        }
        else {
            hidePath.moveTo(0, height);
        }

        float startAngle = 180;
        if (!isRight) startAngle = 270;

        hidePath.arcTo(halfOval, startAngle, 90);
        hideRegion.path = hidePath;
        Region hide = new Region();
        hide.setPath(hidePath, totalRegion);
        hideRegion.region = hide;

        for (int i = 0; i < getChildCount() - 1; i++) {
            Path path = new Path();
            if (isRight) {
                path.moveTo(width, height);
            } else {
                path.moveTo(0, height);
            }

            path.arcTo(oval, startAngle + childAngle * i, childAngle);

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
        pathRegionList.add(hideRegion);

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
        for (int i = 0; i < pathRegionList.size(); i++) {
            if (pathRegionList.get(i).region.contains((int) x, (int) y)) {
                bitmapCanvas.drawPath(pathRegionList.get(i).path, maskPaint);
                if (i < pathRegionList.size() - 1) {
                    if (onItemClickListener != null) {
                        if (isRight) {
                            onItemClickListener.onItemClick(i);
                        } else {
                            onItemClickListener.onItemClick(pathRegionList.size() - 2 - i);
                        }
                    }
                } else {
                    if (onShutDownClickListener != null) {
                        onShutDownClickListener.onShutDownClick();
                    }
                }
            }
        }
        invalidate();
    }

    private void onUp() {
        clearCanvas();
        drawContent(1);
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private void onMove(MotionEvent event){
        clearCanvas();
        drawContent(1);
        float x = event.getX();
        float y = event.getY();
        for (PathRegion pathRegion : pathRegionList) {
            if (pathRegion.region.contains((int) x, (int) y)){
                bitmapCanvas.drawPath(pathRegion.path, maskPaint);
            }
        }

        invalidate();

    }

    public void showMenu() {
        if (!isShowing && !isAnimatorRunning()) {
            getChildAt(getChildCount() - 1).setVisibility(VISIBLE);
            for (int i = 0; i < getChildCount() - 1; i++) {
                View view = getChildAt(i);
                view.setVisibility(VISIBLE);
            }
            showSectorAnimator.start();
            showIconAnimatorSet.start();
        }
    }

    public void hideMenu() {
        if (isShowing && !isAnimatorRunning()) {
            hideIconAnimatorSet.start();
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
        return showIconAnimatorSet.isRunning() || showSectorAnimator.isRunning()
                || hideSectorAnimator.isRunning() || hideIconAnimatorSet.isRunning();
    }

    /**
     * 绘制分割线
     * @param r 半径
     */
    private void drawSplitLine(float r){

        for (int i = 1; i < getChildCount() - 1; i++) {
            float temp = r;

            float outX = (float) (width - temp * Math.cos(childRadian * i));
            float outY = (float) (height - temp * Math.sin(childRadian * i));

            if (!isRight) outX = (float) (temp * Math.cos(childRadian * i));

            temp *= ratio;

            float inX = (float) (width - temp * Math.cos(childRadian * i));
            float inY = (float) (height - temp * Math.sin(childRadian * i));

            if (!isRight) inX = (float) (temp * Math.cos(childRadian * i));

            bitmapCanvas.drawLine(inX, inY, outX, outY, strokePaint);

        }

    }

    /**
     * 用于判断点击区域
     */
    private class PathRegion{
        Path path;
        Region region;

        @NonNull
        @Override
        public String toString() {
            return path.toString() + region.toString();
        }
    }

    interface OnItemClickListener {
        void onItemClick(int position);
    }

    interface OnShutDownClickListener {
        void onShutDownClick();
    }

    public void setOnShutDownClickListener(OnShutDownClickListener listener) {
        this.onShutDownClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
