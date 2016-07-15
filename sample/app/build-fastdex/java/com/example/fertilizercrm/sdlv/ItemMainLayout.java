package com.example.fertilizercrm.sdlv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by yuyidong on 15/9/24.
 */
class ItemMainLayout extends FrameLayout {
    private static final int INTENTION_LEFT_OPEN = 1;
    private static final int INTENTION_LEFT_CLOSE = 2;
    private static final int INTENTION_LEFT_ALREADY_OPEN = 3;
    private static final int INTENTION_RIGHT_OPEN = -1;
    private static final int INTENTION_RIGHT_CLOSE = -2;
    private static final int INTENTION_RIGHT_ALREADY_OPEN = -3;
    private static final int INTENTION_SCROLL_BACK = -4;
    private static final int INTENTION_ZERO = 0;
    private int mIntention = INTENTION_ZERO;

    private static final int SCROLL_STATE_OPEN = 1;
    private static final int SCROLL_STATE_CLOSE = 0;
    private int mScrollState = SCROLL_STATE_CLOSE;
    /* 时间 */
    private static final int SCROLL_TIME = 500;//500ms
    private static final int SCROLL_BACK = 250;//250MS
    private static final int SCROLL_DELETE_TIME = 300;//300ms
    /* 控件高度 */
    private int mHeight;
    /* 子控件中button的总宽度 */
    private int mBtnLeftTotalWidth;
    private int mBtnRightTotalWidth;
    /* 子view */
    private ItemBackGroundLayout mItemLeftBackGroundLayout;
    private ItemBackGroundLayout mItemRightBackGroundLayout;
    private ItemCustomLayout mItemCustomLayout;
    /* Scroller */
    private Scroller mScroller;
    /* 控件是否滑动 */
    private boolean mIsMoving = false;
    /* 是不是要滑过(over) */
    private boolean mWannaOver = true;
    /* 坐标 */
    private float mXDown;
    private float mYDown;
    /* 最小滑动距离，超过了，才认为开始滑动 */
    private int mTouchSlop = 0;
    /* X方向滑动距离 */
    private float mLeftDistance;
    /* 滑动的监听器 */
    private OnItemSlideListenerProxy mOnItemSlideListenerProxy;

    public ItemMainLayout(Context context) {
        this(context, null);
    }

    public ItemMainLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemMainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        mItemRightBackGroundLayout = new ItemBackGroundLayout(context);
        addView(mItemRightBackGroundLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mItemLeftBackGroundLayout = new ItemBackGroundLayout(context);
        addView(mItemLeftBackGroundLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mItemCustomLayout = new ItemCustomLayout(context);
        addView(mItemCustomLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * 得到CustomView
     *
     * @return
     */
    public ItemCustomLayout getItemCustomLayout() {
        return mItemCustomLayout;
    }

    /**
     * 得到左边的背景View
     *
     * @return
     */
    public ItemBackGroundLayout getItemLeftBackGroundLayout() {
        return mItemLeftBackGroundLayout;
    }

    /**
     * 得到右边的背景View
     *
     * @return
     */
    public ItemBackGroundLayout getItemRightBackGroundLayout() {
        return mItemRightBackGroundLayout;
    }

    /**
     * @param height
     * @param btnLeftTotalWidth
     * @param btnRightTotalWidth
     * @param wannaOver
     */
    public void setParams(int height, int btnLeftTotalWidth, int btnRightTotalWidth, boolean wannaOver) {
        mHeight = height;
        mBtnLeftTotalWidth = btnLeftTotalWidth;
        mBtnRightTotalWidth = btnRightTotalWidth;
        mWannaOver = wannaOver;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
        for (int i = 0; i < getChildCount(); i++) {
            measureChild(getChildAt(i), widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(false);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getX();
                mYDown = ev.getY();
                //控件初始距离
                mLeftDistance = mItemCustomLayout.getLeft();
                //是否有要scroll的动向，目前没有
                mIsMoving = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (fingerNotMove(ev) && !mIsMoving) {//手指的范围在50以内
                    //执行ListView的手势操作
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else if (fingerLeftAndRightMove(ev) || mIsMoving) {//上下范围在50，主要检测左右滑动
                    //是否有要scroll的动向
                    mIsMoving = true;
                    //执行控件的手势操作
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float moveDistance = ev.getX() - mXDown;//这个往右是正，往左是负
                    //判断意图
                    if (moveDistance > 0) {//往右
                        if (mLeftDistance == 0) {//关闭状态
                            mIntention = INTENTION_LEFT_OPEN;
                            setBackGroundVisible(true, false);
                        } else if (mLeftDistance < 0) {//右边的btn显示出来的
                            mIntention = INTENTION_RIGHT_CLOSE;
                        } else if (mLeftDistance > 0) {//左边的btn显示出来的
                            mIntention = INTENTION_LEFT_ALREADY_OPEN;
                        }
                    } else if (moveDistance < 0) {//往左
                        if (mLeftDistance == 0) {//关闭状态
                            mIntention = INTENTION_RIGHT_OPEN;
                            setBackGroundVisible(false, true);
                        } else if (mLeftDistance < 0) {//右边的btn显示出来的
                            mIntention = INTENTION_RIGHT_ALREADY_OPEN;
                        } else if (mLeftDistance > 0) {//左边的btn显示出来的
                            mIntention = INTENTION_LEFT_CLOSE;
                        }
                    }
                    //计算出距离
                    switch (mIntention) {
                        case INTENTION_LEFT_OPEN:
                        case INTENTION_LEFT_ALREADY_OPEN:
                            //此时moveDistance为正数，mLeftDistance为0
                            float distanceLeftOpen = mLeftDistance + moveDistance;
                            if (!mWannaOver) {
                                distanceLeftOpen = distanceLeftOpen > mBtnLeftTotalWidth ? mBtnLeftTotalWidth : distanceLeftOpen;
                            }
                            //滑动
                            mItemCustomLayout.layout((int) distanceLeftOpen, mItemCustomLayout.getTop(),
                                    mItemCustomLayout.getWidth() + (int) distanceLeftOpen, mItemCustomLayout.getBottom());
                            break;
                        case INTENTION_LEFT_CLOSE:
                            //此时moveDistance为负数，mLeftDistance为正数
                            float distanceLeftClose = mLeftDistance + moveDistance < 0 ? 0 : mLeftDistance + moveDistance;
                            //滑动
                            mItemCustomLayout.layout((int) distanceLeftClose, mItemCustomLayout.getTop(),
                                    mItemCustomLayout.getWidth() + (int) distanceLeftClose, mItemCustomLayout.getBottom());
                            break;
                        case INTENTION_RIGHT_OPEN:
                        case INTENTION_RIGHT_ALREADY_OPEN:
                            //此时moveDistance为负数，mLeftDistance为0
                            float distanceRightOpen = mLeftDistance + moveDistance;
                            //distanceRightOpen为正数
                            if (!mWannaOver) {
                                distanceRightOpen = -distanceRightOpen > mBtnRightTotalWidth ? -mBtnRightTotalWidth : distanceRightOpen;
                            }
                            //滑动
                            mItemCustomLayout.layout((int) distanceRightOpen, mItemCustomLayout.getTop(),
                                    mItemCustomLayout.getWidth() + (int) distanceRightOpen, mItemCustomLayout.getBottom());
                            break;
                        case INTENTION_RIGHT_CLOSE:
                            //此时moveDistance为正数，mLeftDistance为负数
                            float distanceRightClose = mLeftDistance + moveDistance > 0 ? 0 : mLeftDistance + moveDistance;
                            //滑动
                            mItemCustomLayout.layout((int) distanceRightClose, mItemCustomLayout.getTop(),
                                    mItemCustomLayout.getWidth() + (int) distanceRightClose, mItemCustomLayout.getBottom());

                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                switch (mIntention) {
                    case INTENTION_LEFT_CLOSE:
                    case INTENTION_LEFT_OPEN:
                    case INTENTION_LEFT_ALREADY_OPEN:
                        //如果滑出的话，那么就滑到固定位置(只要滑出了 mBtnLeftTotalWidth / 2 ，就算滑出去了)
                        if (Math.abs(mItemCustomLayout.getLeft()) > mBtnLeftTotalWidth / 2) {
                            //滑出
                            mIntention = INTENTION_LEFT_OPEN;
                            int delta = mBtnLeftTotalWidth - Math.abs(mItemCustomLayout.getLeft());
                            mScroller.startScroll(mItemCustomLayout.getLeft(), 0, delta, 0, SCROLL_TIME);
                            if (mOnItemSlideListenerProxy != null && mScrollState != SCROLL_STATE_OPEN) {
                                mOnItemSlideListenerProxy.onSlideOpen(this, MenuItem.DIRECTION_LEFT);
                            }
                            mScrollState = SCROLL_STATE_OPEN;
                        } else {
                            mIntention = INTENTION_LEFT_CLOSE;
                            //滑回去,归位
                            mScroller.startScroll(mItemCustomLayout.getLeft(), 0, -mItemCustomLayout.getLeft(), 0, SCROLL_TIME);
                            if (mOnItemSlideListenerProxy != null && mScrollState != SCROLL_STATE_CLOSE) {
                                mOnItemSlideListenerProxy.onSlideClose(this, MenuItem.DIRECTION_LEFT);
                            }
                            mScrollState = SCROLL_STATE_CLOSE;
                        }
                        break;
                    case INTENTION_RIGHT_CLOSE:
                    case INTENTION_RIGHT_OPEN:
                    case INTENTION_RIGHT_ALREADY_OPEN:
                        if (Math.abs(mItemCustomLayout.getLeft()) > mBtnRightTotalWidth / 2) {
                            //滑出
                            mIntention = INTENTION_RIGHT_OPEN;
                            int delta = mBtnRightTotalWidth - Math.abs(mItemCustomLayout.getLeft());
                            mScroller.startScroll(mItemCustomLayout.getLeft(), 0, -delta, 0, SCROLL_TIME);
                            if (mOnItemSlideListenerProxy != null && mScrollState != SCROLL_STATE_OPEN) {
                                mOnItemSlideListenerProxy.onSlideOpen(this, MenuItem.DIRECTION_RIGHT);
                            }
                            mScrollState = SCROLL_STATE_OPEN;
                        } else {
                            mIntention = INTENTION_RIGHT_CLOSE;
                            mScroller.startScroll(mItemCustomLayout.getLeft(), 0, -mItemCustomLayout.getLeft(), 0, SCROLL_TIME);
                            //滑回去,归位
                            if (mOnItemSlideListenerProxy != null && mScrollState != SCROLL_STATE_CLOSE) {
                                mOnItemSlideListenerProxy.onSlideClose(this, MenuItem.DIRECTION_RIGHT);
                            }
                            mScrollState = SCROLL_STATE_CLOSE;
                        }
                        break;
                }
                mIntention = INTENTION_ZERO;
                postInvalidate();
                mIsMoving = false;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 设置哪边显示哪边不显示
     *
     * @param leftVisible
     * @param rightVisible
     */
    private void setBackGroundVisible(boolean leftVisible, boolean rightVisible) {
        if (leftVisible) {
            if (mItemLeftBackGroundLayout.getVisibility() != VISIBLE) {
                mItemLeftBackGroundLayout.setVisibility(VISIBLE);
            }
        } else {
            if (mItemLeftBackGroundLayout.getVisibility() == VISIBLE) {
                mItemLeftBackGroundLayout.setVisibility(GONE);
            }
        }
        if (rightVisible) {
            if (mItemRightBackGroundLayout.getVisibility() != VISIBLE) {
                mItemRightBackGroundLayout.setVisibility(VISIBLE);
            }
        } else {
            if (mItemRightBackGroundLayout.getVisibility() == VISIBLE) {
                mItemRightBackGroundLayout.setVisibility(GONE);
            }
        }
    }

    /**
     * 上下左右不能超出50
     *
     * @param ev
     * @return
     */
    private boolean fingerNotMove(MotionEvent ev) {
        return (mXDown - ev.getX() < mTouchSlop && mXDown - ev.getX() > -mTouchSlop &&
                mYDown - ev.getY() < mTouchSlop && mYDown - ev.getY() > -mTouchSlop);
    }

    /**
     * 左右得超出50，上下不能超出50
     *
     * @param ev
     * @return
     */
    private boolean fingerLeftAndRightMove(MotionEvent ev) {
        return ((ev.getX() - mXDown > mTouchSlop || ev.getX() - mXDown < -mTouchSlop) &&
                ev.getY() - mYDown < mTouchSlop && ev.getY() - mYDown > -mTouchSlop);
    }

    /**
     * 删除Item
     */
    public void deleteItem(final OnItemDeleteListenerProxy onItemDeleteListenerProxy) {
        scrollBack();
        final int originHeight = mHeight;
        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHeight = originHeight;
                ItemMainLayout.this.requestLayout();
                ItemMainLayout.this.getItemCustomLayout().refreshBackground();
                if (onItemDeleteListenerProxy != null) {
                    onItemDeleteListenerProxy.onDelete(ItemMainLayout.this);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1.0f) {
                    mHeight = originHeight;
                } else {
                    mHeight = originHeight - (int) (originHeight * interpolatedTime);
                }
                ItemMainLayout.this.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setAnimationListener(animationListener);
        animation.setDuration(SCROLL_DELETE_TIME);
        startAnimation(animation);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mItemCustomLayout.layout(mScroller.getCurrX(), mItemCustomLayout.getTop(),
                    mScroller.getCurrX() + mItemCustomLayout.getWidth(), mItemCustomLayout.getBottom());
            postInvalidate();
        }
        super.computeScroll();
    }

    /**
     * 归位
     */
    protected void scrollBack() {
        mIntention = INTENTION_SCROLL_BACK;
        mScroller.startScroll(mItemCustomLayout.getLeft(), 0, -mItemCustomLayout.getLeft(), 0, SCROLL_BACK);
        postInvalidate();
    }

    /**
     * 设置item滑动的监听器
     *
     * @param onItemSlideListenerProxy
     */
    protected void setOnItemSlideListenerProxy(OnItemSlideListenerProxy onItemSlideListenerProxy) {
        mOnItemSlideListenerProxy = onItemSlideListenerProxy;
    }

    protected interface OnItemSlideListenerProxy {
        void onSlideOpen(View view, int direction);

        void onSlideClose(View view, int direction);
    }

    protected interface OnItemDeleteListenerProxy {
        void onDelete(View view);
    }
}
