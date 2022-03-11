package com.zcs.demo.album;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.zcs.demo.album.databinding.ActivityMainBinding;
import com.zcs.demo.album.usage.AppUsageActivity;

/**
 * Created by ZengCS on 2019/5/30.
 * E-mail:zengcs@vip.qq.com
 * Add:成都市天府软件园E3
 */
public class MainActivity extends AppCompatActivity {
    private int screenHeight;
    private int screenWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding bind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        bind.btnAppUsage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AppUsageActivity.class)));
//        PieChart pieChart = new PieChart(getApplicationContext());
//        pieChart.setScreenWidth(screenWidth);
//        pieChart.setScreenHeight(screenHeight);
//        getWindowHeightOrWidth(this);
        Log.d("屏幕宽高", "onDraw: "+screenWidth+" "+screenHeight);
    }

    public void getWindowHeightOrWidth(Activity activity) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        //取得窗口属性
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //窗口高度
        screenHeight = dm.heightPixels;
        //窗口的宽度
        screenWidth = dm.widthPixels;
        //可以通过DisplayMetrics类来获取当前窗口的一些信息,DisplayMetrics对象中，取得的宽高维度是以像素为单位(Pixel)，“像素”所指的是“绝对像素”而非“相对像素”,
        //绝对像素就是设备的物理像素.PS:手机分辨率就是设备像素
//        Log.e(Constants.TAG, dm.toString());
    }
}
