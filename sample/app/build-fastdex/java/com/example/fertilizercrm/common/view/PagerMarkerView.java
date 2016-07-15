package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * viewpage 下面的显示当前页的点控件
 * 下面的点可以设置为图片，也可以设置颜色，如果设置了图片会首先绘制图片，忽略掉颜色
 * @author tongyapeng
 */
public class PagerMarkerView extends View {
	private Context mContext;
	private int mWidth;
	private int mHeight;
	private int mCount;
	private int mCurrent;
	private int mDeviderSize = 24;//item之间的间隔大小
	private int mRadius = 5;//item半径
	private int mFocusColor = Color.WHITE;//当前item的颜色
	private int mNoFocusColor = Color.GRAY;//非当前item颜色
	/**
	 * 如果当前item图片和非当前item图片都不为null，就会优先显示图片， 为null的话会绘制不同颜色的圆 --mFocusColor--mNoFocusColor
	 */
	private Bitmap mFocusBitmap;//当前item对应图片
	private Bitmap mNoFocusBitmap;//非当前item对应图片
	
	public PagerMarkerView(Context context) {
		super(context);
		this.mContext = context;
	}

	public PagerMarkerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureWidthHeight();
		setMeasuredDimension(mWidth, mHeight);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		Paint p = new Paint();		
		for (int i = 0; i < mCount;i ++) {	
			if (mFocusBitmap != null && mNoFocusBitmap != null) {
				int left = 0;
				int top = 0;			
				Bitmap bitmap = null;
				if (i <= mCurrent) {
					left = i * mDeviderSize + i * mNoFocusBitmap.getWidth();
				} else {
					left = i * mDeviderSize + (i - 1) * mNoFocusBitmap.getWidth() + mFocusBitmap.getWidth();
				} 	
				if (i == mCurrent) {
					if (mFocusBitmap.getHeight() < mNoFocusBitmap.getHeight()) {
						top = (mNoFocusBitmap.getHeight() - mFocusBitmap.getHeight()) / 2;
					}
					bitmap = mFocusBitmap;
				} else {
					if (mFocusBitmap.getHeight() > mNoFocusBitmap.getHeight()) {
						top = (mFocusBitmap.getHeight() - mNoFocusBitmap.getHeight()) / 2;
					}
					bitmap = mNoFocusBitmap;					
				}
				canvas.drawBitmap(bitmap, left, top, p);
			} else {
				float cx = i * (2 * mRadius) + mRadius + i * mDeviderSize;
				float cy = mHeight / 2;
				p.setColor(i == mCurrent ? mFocusColor : mNoFocusColor);
				canvas.drawCircle(cx, cy, mRadius, p);
				cx += (mRadius * 2 + mDeviderSize);
			}
		}
	}
	/*
	 * 测试此控件需要的空间
	 */
	private void measureWidthHeight() {
		if (mFocusBitmap != null && mNoFocusBitmap != null) {
			mWidth = (mCount - 1) * mNoFocusBitmap.getWidth() + (mCount - 1) * mDeviderSize + mFocusBitmap.getWidth();
			mHeight = mFocusBitmap.getHeight() > mNoFocusBitmap.getHeight() ? mFocusBitmap.getHeight() : mNoFocusBitmap.getHeight();
		} else {
			mWidth = (mCount - 1) * mDeviderSize + mCount * mRadius * 2;
			mHeight = mRadius * 2;
		}
	}
	/**
	 * 设置Item个数
	 * @param count
	 */
	public void setCount(int count) {
		this.mCount = count;
		if (count == 0) {
			setVisibility(GONE);
			return;
		}
		setVisibility(VISIBLE);
		requestLayout();
		requestLayout();
		invalidate();
	}
	/**
	 * 设置当前条目
	 * @param current
	 */
	public void setCurrent(int current) {
		this.mCurrent = current;
		
		invalidate();
	}	
	/**
	 * 设置分割大小
	 * @param deviderSize
	 */
	public void setDeviderSize(int deviderSize) {
		this.mDeviderSize = deviderSize;
		
		invalidate();
	}	
	/**
	 * 设置item半径
	 * @param radius
	 */
	public void setRadius(int radius) {
		this.mRadius = radius;
		
		invalidate();
	}	
	/**
	 * 设置当前item颜色
	 * @param focusColor
	 */
	public void setFocusColor(int focusColor) {
		this.mFocusColor = focusColor;
		
		invalidate();
	}
	/**
	 * 设置非当前item颜色
	 * @param noFocusColor
	 */
	public void setNoFocusColor(int noFocusColor) {
		this.mNoFocusColor = noFocusColor;
		
		invalidate();
	}
	/**
	 * 设置当前item对应图片
	 * @param focusBitmap
	 */
	public void setFocusBitmap(Bitmap focusBitmap) {
		this.mFocusBitmap = focusBitmap;
		
		invalidate();
	}
	/**
	 * 设置非当前item对应图片
	 * @param noFocusBitmap
	 */
	public void setNoFocusBitmap(Bitmap noFocusBitmap) {
		this.mNoFocusBitmap = noFocusBitmap;
	}
	/**
	 * 设置非当前item对应图片
	 * @param
	 */
	public void setNoFocusResource(int  resId) {
		setNoFocusBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
	}
	/**
	 * 设置当前item对应图片
	 * @param
	 */
	public void setFocusResource(int  resId) {
		setFocusBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
	}	
}
