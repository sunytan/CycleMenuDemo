package com.young.cyclemenulayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by yang.tan on 2018/1/5.
 */
public class CycleMenuLayout extends ViewGroup {

    private static final String TAG = "CycleMenuLayout";

    /**
     *  单个item的布局资源文件
     */
    private final static int mMenuItemLayoutId = R.layout.main_menu_item;

    /**
     * 布局旋转总角度
     */
    private static float mTotalAngle = 0.0f;

    /**
     * 判断是否是滑动事件,不是则响应点击事件
     */
    private static boolean isScroller = false;

    /**
     * 滑动超过这个角度，判断为滑动事件
     */
    private final static int SCROLL_ANGLE = 2;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private static final int SCOLLER_VALUE = 100;

    private Scroller mScroller;
    private CorrectRunnable mCorrectRunnable;

    // 菜单的个数
    private int mMenusItemCount;


    /**
     * 记录上一次的x，y坐标
     */
    private float mLastX;
    private float mLastY;

    /**
     * 检测按下到抬起时旋转的总角度
     */
    private float mTmpAngle = 0.0F;

    /**
     * 检测按下到抬起时使用的总时间
     */
    private long mDownTime = 0L;

    /**
     * 转动角速度(度每秒)
     */
    private float anglePerSecond;

    /**
     * 菜单项的图标
     */
    private int[] mMenusItemImgs;
    /**
     * 菜单项的文本
     */
    private String[] mMenusItemTexts;

    /**
     * 每个单项菜单项的角度
     */
    private float[] mMenusItemAngel;

    /**
     * 圆形菜单半径大小
     */
    private float radius;

    /**
     * 每个item间隔角度
     */
    private int angle_interval;

    /**
     * 单个item图标的宽度,高度
     */
    private int item_width;
    private int item_height;

    /**
     * 中心菜单按钮宽度，高度
     */
    private int center_width;
    private int center_height;

    /**
     * 圆心坐标
     */
    private int center_X;
    private int center_Y;

    public CycleMenuLayout(Context context) {
        this(context,null);
    }

    public CycleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
        mScroller = new Scroller(context);
        mCorrectRunnable = new CorrectRunnable();
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CycleMenuLayout);
        try {
            radius = typedArray.getDimension(R.styleable.CycleMenuLayout_radius,getResources().getDimension(R.dimen.radius));
            item_width = (int)typedArray.getDimension(R.styleable.CycleMenuLayout_item_width,getResources().getDimension(R.dimen.item_width));
            item_height = (int)typedArray.getDimension(R.styleable.CycleMenuLayout_item_height,getResources().getDimension(R.dimen.item_height));
            angle_interval = (int)typedArray.getFloat(R.styleable.CycleMenuLayout_angle_interval,30);
            center_width = (int)typedArray.getDimension(R.styleable.CycleMenuLayout_center_width,getResources().getDimension(R.dimen.center_width));
            center_height = (int)typedArray.getDimension(R.styleable.CycleMenuLayout_center_height,getResources().getDimension(R.dimen.center_height));
            center_X = (int)typedArray.getDimension(R.styleable.CycleMenuLayout_center_X,0.0f);
            center_Y = (int)typedArray.getDimension(R.styleable.CycleMenuLayout_center_Y,0.0f);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * 设置菜单条目的图标和文本
     *
     * @param resIds
     */
    public void setMenuItemIcons(int[] resIds, String[] texts) {
        mMenusItemImgs = resIds;
        mMenusItemTexts = texts;

        // 参数检查
        if (resIds == null || texts == null) {
            throw new IllegalArgumentException("菜单项icons为空");
        }
        // 初始化mMenuCount
        mMenusItemCount = resIds.length;
        addMenuItems();
    }

    /**
     * 设置中心菜单的坐标
     * @param x
     * @param y
     */
    public void setCenterXY(int x,int y){
        center_X = x;
        center_Y = y;
        requestLayout();
    }

    /**
     * 设置半径大小
     * @param radius
     */
    public void setRadius(float radius){
        this.radius = radius;
        requestLayout();
    }

    /**
     * 添加菜单项
     */
    private void addMenuItems() {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        //根据用户设置的参数，初始化view
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(item_width,item_height);
        for (int i = 0; i < mMenusItemCount; i++) {
            final int pos = i;
            final int iconResourcId = mMenusItemImgs[i];
            View view = mInflater.inflate(mMenuItemLayoutId, this, false);

            ImageView iv = (ImageView) view.findViewById(R.id.menu_item_image);
            TextView tv = (TextView) view.findViewById(R.id.menu_item_text);
            iv.setLayoutParams(lp);
            if (iv != null) {
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(iconResourcId);
            }
            if (tv != null ){
                // 中心菜单默认不显示文字
                if (!mMenusItemTexts[i].isEmpty() && i != 0) {
                    tv.setText(mMenusItemTexts[i]);
                    tv.setVisibility(VISIBLE);
                }
            }
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.itemClick(v, pos, iconResourcId);
                    }
                }
            });
            // 添加view到ViewGroup容器中
            addView(view);
        }
    }

    private int getVisibleCount(){
        int count = 180 / angle_interval + 1;
        return count;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        int left, top;
        mTotalAngle %= 360.0f;
        float startAngle = mTotalAngle;
        // (n-7)*30 + 180 ,n 就是item个数
        float maxAngle = (mMenusItemImgs.length - getVisibleCount())*angle_interval + 180;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            int childWitdh = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            Log.d(TAG,"cw = "+childWitdh + ",ch = "+childHeight);
            if (i == 0) {
                // 中心菜单绘制
                child.layout(center_X - childWitdh / 2, center_Y - childHeight / 2, center_X + childWitdh / 2, center_Y + childHeight / 2);
                child.setVisibility(VISIBLE);
                continue;
            }
            if (child.getVisibility() == GONE) {
                Log.d(TAG, "mTotalAngle GONE ");
                continue;
            }
            if (startAngle < 0) {
                //为了边缘分界线处，图标平滑出现，在-60 的基础+angle_interval/2度
                if (startAngle <= -60 + angle_interval/2) {
                    startAngle = startAngle + maxAngle;
                }
            } else {
                //为了边缘分界线处，图标平滑出现，在240 的基础 -angle_interval/2度
                if (startAngle >= 240 - angle_interval/2) {
                    startAngle = startAngle - maxAngle;
                }
            }

            left = center_X
                    + (int) Math.round(radius * Math.cos(Math.toRadians(startAngle))
                    - 1f / 2f * childWitdh);
            top = center_Y
                    - (int) Math.round(radius * Math.sin(Math.toRadians(startAngle))
                    + 1f / 2f * childHeight);
            child.layout(left, top, left + childWitdh, top + childHeight);
            child.setRotation(90-startAngle);
            if (i == 1) {
                mTotalAngle = startAngle;
            }
            startAngle += angle_interval;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        doMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        final int count = getChildCount();
        if (count > 0) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(TAG,"dispatchTouchEvent action = "+event.getAction());
        super.dispatchTouchEvent(event);
        Log.d(TAG, " analysisTouchEvent . mTotalAngle = " + mTotalAngle + ",,,action ="+event.getAction());
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            //----------------------------------------------------------------------------
            case MotionEvent.ACTION_DOWN: {
                isScroller = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;
            }
            break;
            //------------------------------------------------------------------------------
            case MotionEvent.ACTION_MOVE: {
                float start = getAngle(mLastX, mLastY);
                float end = getAngle(x, y);
                Log.d(TAG, "start = " + start + " , end =" + end + " , mTotalAngle = " + mTotalAngle);
                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mTotalAngle += end - start;
                    mTmpAngle += end - start;
                } else { // 二、三象限，色角度值是付值
                    mTotalAngle += start - end;
                    mTmpAngle += start - end;
                }
                // 如果滑动的总角度大于2度，就判断为滑动事件
                if (Math.abs(mTmpAngle) > 2 ){
                    isScroller = true;
                }
                Log.d(TAG, " mTotalAngle = " + mTotalAngle);
                requestLayout();

                mLastX = x;
                mLastY = y;
            }
            break;
            //--------------------------------------------------------------------------------
            case MotionEvent.ACTION_UP: {
                anglePerSecond = mTmpAngle * 1000 / (System.currentTimeMillis() - mDownTime);
                Log.d(TAG, " 掉帧 ACTION_UP x = " + x + " . deltaX = " + (x - mLastX) + " . mTotalAngle = " + mTotalAngle
                        +" ,, angelePerSecond = "+anglePerSecond);
                int startAngle = (int)mTotalAngle;

                if (Math.abs(anglePerSecond) > SCOLLER_VALUE) {
                    mScroller.fling(startAngle, 0, (int) (anglePerSecond * 2.0f), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                    mScroller.setFinalX(mScroller.getFinalX() + computeCorrectAngle(mScroller.getFinalX() % angle_interval));
                } else {
                    Log.d(TAG, "start scroller");
                    mScroller.startScroll(startAngle, 0, computeCorrectAngle(startAngle % angle_interval), 0, 500);
                }
                post(mCorrectRunnable);
                break;
            }
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        Log.d(TAG,"onInterceptTouchEvent action ="+action);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                // 如果被判断为滑动事件，测拦截up事件
                if (isScroller){
                    isScroller = false;
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"onTouchEvent action = "+event.getAction());
        return super.onTouchEvent(event);
    }

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - center_X;
        double y = center_Y - yTouch;
        //(center_X,center_Y) 圆心位置  sin a = y / sqrt( (xTouch - center_X)^2 + (center_Y - yTouch)^2) => a
        float angle = (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
        return angle;
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - center_X);
        int tmpY = (int) (y - center_Y);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }
    }

    /**
     * 计算修正角度,如果滚到大于间隔的一半，就修正到下一个位置，否者就回到原来位置
     * @param remaindAngle
     * @return
     */
    private int computeCorrectAngle(int remaindAngle) {
        int dAngle;
        if (Math.abs(remaindAngle) > angle_interval / 2) {
            if (mTmpAngle < 0) {
                //顺时针
                if(remaindAngle > 0) {
                    dAngle = angle_interval - Math.abs(remaindAngle);
                }else {
                    dAngle = Math.abs(remaindAngle) -angle_interval;
                }
            } else {
                //逆时针
                if (remaindAngle > 0) {
                    dAngle = angle_interval - Math.abs(remaindAngle);
                } else {
                    dAngle = Math.abs(remaindAngle) - angle_interval;
                }
            }
        } else {
            dAngle = -remaindAngle;
        }
        Log.d(TAG,"dAngle = "+dAngle);
        return dAngle;
    }

    /**
     * Item的点击事件接口
     */
    private OnMenuItemClickListener mOnMenuItemClickListener;

    /**
     * 设置item的点击事件接口
     * @param mOnMenuItemClickListener
     */
    public void setOnMenuItemClickListener(OnMenuItemClickListener mOnMenuItemClickListener) {
        this.mOnMenuItemClickListener = mOnMenuItemClickListener;
    }

    /**
     * 自动矫正位置
     */
    private class CorrectRunnable implements Runnable {

        public void run() {
            if (mScroller.computeScrollOffset()) {
                mTotalAngle = mScroller.getCurrX();
                postDelayed(mCorrectRunnable, 20);
                requestLayout();
            }
        }
    }
}
