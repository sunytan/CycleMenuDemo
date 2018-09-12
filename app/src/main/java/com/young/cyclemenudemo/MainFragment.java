package com.young.cyclemenudemo;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cyclyjavademo.CycleTest;
import com.young.cyclemenulayout.CycleMenuLayout;
import com.young.cyclemenulayout.OnMenuItemClickListener;

/**
 * Created by Administrator on 2017/10/19.
 */

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private CycleMenuLayout menuLayout;
    CycleTest test;
    private int[] resImages = new int[]{
            R.mipmap.menu,
            R.mipmap.camera, R.mipmap.setting,
            R.mipmap.contact, R.mipmap.community,
            R.mipmap.map, R.mipmap.message,
            R.mipmap.phone, R.mipmap.security,
    };
    private String[] menuTexts = new String[]{
            "中心菜单",
            "摄像",
            "设置",
            "通讯录",
            "社区",
            "地图",
            "消息提示",
            "电话",
            "安全",
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, " onCreateView");
        test = new CycleTest();
        View view = inflater.inflate(R.layout.fragment_main, null, false);
        menuLayout = (CycleMenuLayout) view.findViewById(R.id.fragment_cyclemenulayout);
        menuLayout.setMenuItemIcons(resImages, menuTexts);
        menuLayout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int pos, int resId) {
                Log.d(TAG, " pos : " + pos + "  text: " + menuTexts[pos]);
                doOnClick(view,pos,resId);
            }

            @Override
            public void itemCenterClick(View view) {
                Log.d(TAG, " itemCenterClick");
            }

        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //menuLayout.setCenterXY(800,600);
            }
        },10000);
        return view;
    }

    private void doOnClick(View view,int pos,int resId) {
        if (resId == 0)
            return;
        Toast.makeText(getActivity(), menuTexts[pos] + test.getCycleTest(), Toast.LENGTH_SHORT).show();

        switch (resId) {
            case R.mipmap.camera:
                break;
            case R.mipmap.security:
                break;
            case R.mipmap.community:
                break;
            case R.mipmap.contact:
                break;
            case R.mipmap.map:
                break;
            case R.mipmap.message:
                break;
            case R.mipmap.phone:
                break;
            case R.mipmap.setting:
                break;
            default:
                break;
        }
    }
}
