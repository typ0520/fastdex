package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 自动切换的viewpager
 * @author tong
 *
 */
public class AutoViewPager extends ViewPager {
	private PagerAdapter mAdapter;
	private OnTouchListener mOnTouchListenerBase;
	private Timer mAutoTimer;
	private long mDelay = 3000;//开启自动切换后的延迟
	private long mInterval = 5000;//切换的时间间隔
	private int currentItem;//当前处于的页数
	private Handler handler = new Handler();
	private boolean autoSwitch = true;
    private OnPageChangeListener mOnPageChangeListener;

	public AutoViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AutoViewPager(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		//设置当用户在滑动viewpager时，停止自动切换，当用户手指离开时再次开启自动切换
		super.setOnTouchListener(mOnTouchListener);
        super.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrolled(i,v,i2);
                }
            }

            @Override
            public void onPageSelected(int i) {
                currentItem = i;
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageSelected(i);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrollStateChanged(i);
                }
            }
        });
	}

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    @Override
	public void setAdapter(PagerAdapter adpter) {
		mAdapter = adpter;
		super.setAdapter(adpter);
		startTimer();
	}	
	
	public void setAutoSwitch(boolean autoSwitch) {
		this.autoSwitch = autoSwitch;
		if (!autoSwitch) {
			stopTimer();
		}
	}
	
	@Override
	public void setOnTouchListener(OnTouchListener l) {
		mOnTouchListenerBase = l;
		super.setOnTouchListener(l);
	}
	//启动自动切换定时器
	public void startTimer() {
		if (autoSwitch) {
			stopTimer();
			mAutoTimer = new Timer();
			mAutoTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					currentItem = (currentItem + 1) % mAdapter.getCount();
					handler.post(new Runnable() {
						@Override
						public void run() {
							setCurrentItem(currentItem);
						}
					});
				}
			}, mDelay, mInterval);
		}
	}

	//关闭自动切换定时器
	public void stopTimer() {
		if (mAutoTimer != null) {
			mAutoTimer.cancel();
		}
	}
	/**
	 * 设置切换的间隔
	 * @param interval
	 */
	public void setInterval(long interval) {
		mInterval = interval;
	}
	/**
	 * 设置开启自动切换后，开始切换前的延迟
	 */
	public void setDelay(long delay) {
		mDelay = delay;
	}
	
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (!autoSwitch)
				return false;
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				stopTimer();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				startTimer();
			}
			if (mOnTouchListenerBase != null) {
				return mOnTouchListenerBase.onTouch(v, event);
			}
			return false;
		}
	};
}
