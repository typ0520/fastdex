package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动切换的图片ViewPager
 * @author tong
 *
 */
public class AutoImageViewPager extends AutoViewPager {
	private List<ImageView> mImageViews = new ArrayList<ImageView>();

	public AutoImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoImageViewPager(Context context) {
		super(context);
	}	

	public void setImageUrls(List<String> imageUrls) {
		setImageUrls(-1,-1,imageUrls);
	}

	public void setImageUrls(int failRes,int loadingRes,List<String> imageUrls) {
		class MyBocopImageView extends BocopImageView {

			public MyBocopImageView(Context context, AttributeSet attrs,
					int defStyle) {
				super(context, attrs, defStyle);
			}

			public MyBocopImageView(Context context, AttributeSet attrs) {
				super(context, attrs);
			}

			public MyBocopImageView(Context context) {
				super(context);
			}

			@Override
			public void setImageDrawable(Drawable drawable) {
				super.setBackgroundDrawable(drawable);
			}

			@Override
			public void setImageResource(int resId) {
				super.setBackgroundResource(resId);
			}
		}

		List<ImageView> imageViews = new ArrayList<ImageView>();
		for (int i = 0; i < imageUrls.size(); i++) {
			BocopImageView imageView = new MyBocopImageView(getContext());
			imageView.setImageUrl(imageUrls.get(i), failRes, loadingRes);
			imageViews.add(imageView);
		}
		setImageViews(imageViews);
	}

	public void setImages(int... imageIds) {
		List<ImageView> imageViews = new ArrayList<ImageView>();
		for (int i = 0; i < imageIds.length; i++) {
			ImageView imageView = new ImageView(getContext());
			imageView.setBackgroundResource(imageIds[i]);
			imageViews.add(imageView);
		}
		setImageViews(imageViews);
	}
	
	public void setBitmaps(Bitmap[] bitmaps) {
		List<ImageView> imageViews = new ArrayList<ImageView>();
		for (int i = 0; i < bitmaps.length; i++) {
			ImageView imageView = new ImageView(getContext());
			imageView.setBackgroundDrawable(new BitmapDrawable(getContext().getResources(),bitmaps[i]));
			imageViews.add(imageView);
		}
		setImageViews(imageViews);
	}
	
	public ImageView getCurrentImageView(int position){
		return mImageViews.get(position);
	}
	
	@Override
	public void setAdapter(PagerAdapter adpter) {
		
	}
	public void setImageViews(List<ImageView> imageViews) {
		mImageViews = imageViews;
		super.setAdapter(new AdPaperAdapter());
	}
	
	private class AdPaperAdapter extends PagerAdapter {
		public int getCount() {
			return mImageViews.size();
		}

		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		public void destroyItem(ViewGroup container, int position, Object object) {
			if (position < getCount())
				container.removeView(mImageViews.get(position));
		}

		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mImageViews.get(position));
			return mImageViews.get(position);
		}

	}
}
