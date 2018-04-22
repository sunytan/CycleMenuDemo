package com.young.cyclemenulayout;



import android.view.View;

/**
 * Created by yang.tan on 2018/1/5.
 * MenuItem的点击事件接口
 */
public interface OnMenuItemClickListener {

    void itemClick(View view, int pos,int resId);

    void itemCenterClick(View view);
}
