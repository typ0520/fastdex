package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.List;

/**
 * 自动切换的图片ViewPager
 * @author tong
 *
 */
public class ImageViewPager extends AutoImageViewPager {
	
	public ImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageViewPager(Context context) {
		super(context);
	}	

	@Override
	public void setImageViews(List<ImageView> imageViews) {
		super.setImageViews(imageViews);
		setAutoSwitch(false);
	}
}
