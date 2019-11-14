package com.czt.pencheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ArcView extends View {
    Bitmap bitmap;
    public ArcView(Context context) {
        this(context, null);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.annotation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF oval = new RectF(0, 0, 300, 300);
        Path path = new Path();
        path.moveTo(150, 150);
        path.arcTo(oval, 90, 90);


        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GREEN);


        canvas.drawPath(path, paint);


        canvas.drawBitmap(bitmap, 150, 150, paint);
    }
}
