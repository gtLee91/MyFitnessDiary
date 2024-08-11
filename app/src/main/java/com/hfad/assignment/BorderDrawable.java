package com.hfad.assignment;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class BorderDrawable extends Drawable {

    private Paint paint;
    private int color;
    private float radius;

    public BorderDrawable() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
    }

    public void setCornersRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rectF = new RectF(getBounds());
        canvas.drawRoundRect(rectF, radius, radius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        // Not implemented
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Not implemented
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}

