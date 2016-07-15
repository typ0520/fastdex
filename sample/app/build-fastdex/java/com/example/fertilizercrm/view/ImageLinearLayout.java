package com.example.fertilizercrm.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.fertilizercrm.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.DimensionPixelUtil;
import com.example.fertilizercrm.common.utils.ImageCropHelper;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.view.BocopImageView;

/**
 * Created by tong on 16/1/7.
 */
public class ImageLinearLayout extends LinearLayout {
    private ImageCropHelper imageCropHelper;
    public int max = 10;
    private boolean enable = true;

    @Bind(R.id.ll_container) LinearLayout ll_container;

    public ImageLinearLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_image_linear_layout, this);

        ButterKnife.bind(this);

        imageCropHelper = new ImageCropHelper((Activity)getContext());
        imageCropHelper.setCropImg(false);
        imageCropHelper.setOnCropListener(new ImageCropHelper.OnCropListener() {
            @Override
            public void onGetImage(File imgFile, Bitmap bitmap) {
                ImageView imageView = createImageView();
                imageView.setTag(imgFile);
                imageView.setImageBitmap(bitmap);

                ll_container.addView(imageView, ll_container.getChildCount() - 1);
            }
        });
    }

    @OnClick(R.id.ib_add_pic) void addPic() {
        if (!enable || getImageCount() >= max) {
            return;
        }
        imageCropHelper.start();
    }

    public void setImageUrls(List<String> imgUrls) {
        Logger.e(">>imgUrls: " + imgUrls);
        if (imgUrls == null) {
            return;
        }
        for (int i = 0;i < imgUrls.size();i++) {
            String url = imgUrls.get(i);
            BocopImageView imageView = createImageView();
            imageView.setImageUrl(url,R.drawable.default_img,R.drawable.default_img);
            ll_container.addView(imageView, ll_container.getChildCount() - 1);
        }
    }

    //获取图片的数量
    private int getImageCount() {
        return ll_container.getChildCount() - 1;
    }

    private BocopImageView createImageView() {
        BocopImageView imageView = new BocopImageView(getContext());
        int width = (int) DimensionPixelUtil.dip2px(getContext(),60.f);
        int height = width;
        imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height) {{
            setMargins((int) DimensionPixelUtil.dip2px(getContext(), 5.f), 0, 0, 0);
        }});
        return imageView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageCropHelper.onActivityResult(requestCode, resultCode, data);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isEnabled() {
        return enable;
    }

    public void setEnabled(boolean enable) {
        this.enable = enable;
    }

    public List<File> getImageFiles() {
        List<File> imageFiles = new ArrayList<>();
        for (int i = 0;i < ll_container.getChildCount() - 1;i++) {
            imageFiles.add((File) ll_container.getChildAt(i).getTag());
        }
        return imageFiles;
    }
}
