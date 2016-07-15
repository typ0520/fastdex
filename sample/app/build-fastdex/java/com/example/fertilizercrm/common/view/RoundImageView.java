package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 图片显示成圆形的ImageView
 * @author tong
 *
 */
public class RoundImageView extends ImageView {
	private static final Xfermode MASK_XFERMODE;
	private Bitmap bitmap;
	private Paint paint;

	static {
		PorterDuff.Mode localMode = PorterDuff.Mode.DST_IN;
		MASK_XFERMODE = new PorterDuffXfermode(localMode);
	}

	public RoundImageView(Context paramContext) {
		super(paramContext);
	}

	public RoundImageView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public RoundImageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null)
			return;
		try {
			if (paint == null) {
				paint = new Paint();
				paint.setFilterBitmap(false);
				Xfermode localXfermode1 = MASK_XFERMODE;
				@SuppressWarnings("unused")
				Xfermode localXfermode2 = paint.setXfermode(localXfermode1);
			}
			float f1 = getWidth();
			float f2 = getHeight();
			int i = canvas.saveLayer(0.0F, 0.0F, f1, f2, null, 31);
			int j = getWidth();
			int k = getHeight();
			drawable.setBounds(0, 0, j, k);
			drawable.draw(canvas);
			if (bitmap == null || bitmap.isRecycled()) {
				this.bitmap = createMask();
			}
			canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint);
			canvas.restoreToCount(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap createMask() {
		int i = getWidth();
		int j = getHeight();
		Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
		Bitmap localBitmap = Bitmap.createBitmap(i, j, localConfig);
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint(1);
		localPaint.setColor(-16777216);
		float f1 = getWidth();
		float f2 = getHeight();
		RectF localRectF = new RectF(0.0F, 0.0F, f1, f2);
		localCanvas.drawOval(localRectF, localPaint);
		return localBitmap;
	}
}
