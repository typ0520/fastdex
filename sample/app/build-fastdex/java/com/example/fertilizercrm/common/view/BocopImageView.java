package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.example.fertilizercrm.common.imageloader.core.DisplayImageOptions;
import com.example.fertilizercrm.common.imageloader.core.ImageLoader;
import com.example.fertilizercrm.common.imageloader.core.assist.FailReason;
import com.example.fertilizercrm.common.imageloader.core.assist.ImageLoadingListener;

/**
 * 支持图片异步加载的ImageView，内部逻辑使用ImageLoader实现
 * @author tong
 */
public class BocopImageView extends ImageView {
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public BocopImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BocopImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BocopImageView(Context context) {
		super(context);
	}

	public void setImageUrl(String url) {
		setImageUrl(url, -1, -1, null);
	}

	public void setImageUrl(String url, final OnCompleteListener completeListener) {
		setImageUrl(url, -1, -1, completeListener);
	}

	public void setImageUrl(String url, int failRes) {
		setImageUrl(url, failRes, -1, null);
	}

	public void setImageUrl(String url,int failRes, OnCompleteListener completeListener) {
		setImageUrl(url, failRes, -1, completeListener);
	}

	public void setImageUrl(String url, int failRes, int loadingRes) {
		setImageUrl(url, failRes, loadingRes, null);
	}

	public void setImageUrl(String url, int failRes, int loadingRes,final OnCompleteListener completeListener) {
		DisplayImageOptions.Builder builder = getDefaultBuilder();
		if (failRes != -1) {
			builder.showImageOnFail(failRes);
		}
		if (loadingRes != -1) {
			builder.showImageOnLoading(loadingRes);
		}
		if (completeListener == null) {
			
			imageLoader.displayImage(url, this, builder.build());
		} else {
			imageLoader.displayImage(url, this, builder.build(),new BaseImageLoadingListener(){
				@Override
				public void onLoadingComplete(String imageUri, View view,
						Bitmap loadedImage) {
					completeListener.onComplete(loadedImage);
				}
			});
		}
	}
	
	
	public DisplayImageOptions.Builder getDefaultBuilder() {
		return  new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565);
	}

	private class BaseImageLoadingListener implements ImageLoadingListener {
		@Override
		public void onLoadingStarted(String imageUri, View view) {
			
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			
		}
		
	}
	public interface OnCompleteListener {
		public void onComplete(Bitmap loadedImage);
	}
}
