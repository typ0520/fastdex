package com.example.fertilizercrm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tong on 9/18/15.
 * 扇形视图
 */
public class SectorView extends View {
    private SectorInfo[] mSectorInfos = new SectorInfo[0];

    public SectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int dsize = - 90;
        RectF oval = new RectF(0,0,getMeasuredWidth(),getMeasuredHeight());

        Paint paint = new Paint();

        int startAngle = 0;
        int endAngle = 0;
        for (int i = 0;i < mSectorInfos.length;i++) {
            SectorInfo sectorInfo = mSectorInfos[i];
            paint.setColor(getResources().getColor(sectorInfo.color));
            canvas.drawArc(oval, startAngle, (int)getAngleFromProgress(sectorInfo.progress), true, paint);
            startAngle += getAngleFromProgress(sectorInfo.progress);
        }
    }

    private double getAngleFromProgress(double progress) {
        return progress * 360 / 100;
    }

    public void setSectorInfos(SectorInfo[] sectorInfos) {
        verifySectorInfos(sectorInfos);
        this.mSectorInfos = sectorInfos;
        invalidate();
    }

    private void verifySectorInfos(SectorInfo[] sectorInfos) {
        int total = 0;
        for (SectorInfo sectorInfo : sectorInfos) {
            total += sectorInfo.progress;
        }
//        if (total != 100) {
//            throw new IllegalArgumentException("total progress must be 100 !!!");
//        }
    }

    public static class SectorInfo {
        public double progress;
        public int color;

        public double getProgress() {
            return progress;
        }

        public void setProgress(double progress) {
            this.progress = progress;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }
}
