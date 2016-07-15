package com.example.fertilizercrm.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.example.fertilizercrm.R;

import java.util.ArrayList;
import java.util.List;

import com.example.fertilizercrm.common.view.ImageViewPager;

/**
 * Created by tong on 15/10/22.
 */
public class ImageDialog extends Dialog {
    public ImageDialog(Context context, List<String> imgUrl, int defaultIdx) {
        super(context, android.R.style.Theme);
        setOwnerActivity((Activity) context);

        setContentView(R.layout.dialog_image);
        ImageViewPager view_pager = (ImageViewPager) findViewById(R.id.view_pager);
        List<ImageView> imageViews = new ArrayList<>();
        for (String url : imgUrl) {
//            BocopImageView imageView = new BocopImageView(context);
//            imageView.setImageUrl(url);
//            imageViews.add(imageView);
        }
        view_pager.setImageViews(imageViews);
        view_pager.setCurrentItem(defaultIdx);

        findViewById(R.id.rl_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
