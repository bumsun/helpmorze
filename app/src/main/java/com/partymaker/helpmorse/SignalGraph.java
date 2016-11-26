package com.partymaker.helpmorse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class SignalGraph extends View {
    short[] data;
    int height;
    Paint paint;
    short threshold;
    int width;
    int f0x;

    public SignalGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.f0x = 0;
    }

    public SignalGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.f0x = 0;
    }

    public SignalGraph(Context context) {
        super(context);
        this.f0x = 0;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        this.data = new short[this.width];
    }

    public void emit(short level, short thres) {
        if (this.data != null) {
            this.data[this.f0x] = level;
            this.f0x = (this.f0x + 1) % this.width;
            this.threshold = thres;
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.paint == null) {
            this.paint = new Paint();
            this.paint.setStyle(Style.STROKE);
        }
        canvas.drawRGB(0, 0, 0);
        this.paint.setColor(-1);
        for (int i = 0; i < this.width; i++) {
            canvas.drawLine((float) i, (float) this.height, (float) i, (float) (this.height - ((int) ((((double) this.data[(this.f0x + i) % this.width]) / 3000.0d) * ((double) this.height)))), this.paint);
        }
        this.paint.setColor(-65536);
        int t = this.height - ((int) ((((double) this.threshold) / 3000.0d) * ((double) this.height)));
        canvas.drawLine(0.0f, (float) t, (float) (this.width - 1), (float) t, this.paint);
    }
}
