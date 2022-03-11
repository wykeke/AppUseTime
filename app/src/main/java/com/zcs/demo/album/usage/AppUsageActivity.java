package com.zcs.demo.album.usage;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.tabs.TabLayout;
import com.zcs.demo.album.PieChart;
import com.zcs.demo.album.PieData;
import com.zcs.demo.album.R;
import com.zcs.demo.album.SelfStatistics;
import com.zcs.demo.album.base.BaseActivity;
import com.zcs.demo.album.base.CommonRecyclerAdapter;
import com.zcs.demo.album.databinding.ActivityAppUsageBinding;
import com.zcs.demo.album.utils.JDateKit;
import com.zcs.demo.album.utils.JListKit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by ZengCS on 2019/5/30.
 * E-mail:zengcs@vip.qq.com
 * Add:成都市天府软件园E3
 */
public class AppUsageActivity extends BaseActivity {
    private static final String TAG = "AppUsageActivity";
    private ActivityAppUsageBinding mHolder;
    private List<AppUsageBean> mItems; //使用时长列表
    private CommonRecyclerAdapter<AppUsageBean> mAdapter;
    //设置切换tab
    private static final String[] TAB_NAMES = {"今日数据", "昨日数据", "本周数据", "本月数据", "年度数据"};
    private boolean isGoToGrand = false;// 是否去过授权页面


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定UI
        mHolder = ActivityAppUsageBinding.inflate(getLayoutInflater());
        setContentView(mHolder.getRoot());

        // 初始化Tab
        int c = 0;
        TabLayout tabLayout = mHolder.tabCondition;
        for (String name : TAB_NAMES) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setTag(c);
            tab.view.setOnClickListener(v -> onTabClick((int) tab.getTag()));
            tabLayout.addTab(tab.setText(name));
            c++;
        }

        // 授权|加载数据
        initData();
    }

    private void initData() {
        if (AppUsageUtil.hasAppUsagePermission(this)) {
            // 默认加载今天的数据
            isGoToGrand = false;
            onTabClick(0);
        } else {
            isGoToGrand = true;
            // TODO 这里有点强制开启的意思，实际应用中最好弹出一个对话框让用户知道，并可以选择【授权】或【退出】
            AppUsageUtil.requestAppUsagePermission(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGoToGrand) {// 如果从应用跳转到了授权，那么返回应用的时候 需要重新执行一次
            initData();
        }
    }

    //切换日数据、周数据
    public void onTabClick(int position) {
        Log.d(TAG, "onTabClick() called with: position = [" + position + "]");
        setTitle(TAB_NAMES[position]);
        //系统现在的时间
        long currTime = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d(TAG, "onTabClick: 时间"+dateformat.format(currTime));
        Log.d(TAG, "onTabClick: 零点"+dateformat.format(getTodayTime0()));
        Log.d(TAG, "onTabClick: 昨天"+dateformat.format(lastDay()));
        switch (position) {
            case 0:// 今天的数据  00:00 到 现在
                getAppUsage(getTodayTime0(), currTime);
                break;
            case 1:// 昨天的数据  昨天00:00 - 今天00:00
                long todayTime0 = getTodayTime0();
                getAppUsage(todayTime0 - DateUtils.DAY_IN_MILLIS, todayTime0);
                break;
            case 2:// 最近7天数据
                getAppUsage(currTime - DateUtils.WEEK_IN_MILLIS, currTime);
                break;
            case 3:// 最近30天数据
                getAppUsage(currTime - DateUtils.DAY_IN_MILLIS * 30, currTime);
                break;
            case 4:// 最近一年的数据
                getAppUsage(currTime - DateUtils.DAY_IN_MILLIS * 365, currTime);
                break;
        }
    }

    /**
     * @return 今日零点的时间
     */
    private long getTodayTime0() {
        // 获取今天凌晨0点0分0秒的time
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        return calendar.getTimeInMillis();
    }

    //昨天的现在时刻
    private long lastDay(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,   -1);
        return cal.getTimeInMillis();
    }


    private LoadAppUsageTask mLoadAppUsageTask;

    //根据时间范围获取应用使用情况
    private void getAppUsage(long beginTime, long endTime) {
        String fmt = "yyyy-MM-dd HH:mm:ss";
        //时间范围显示
        mHolder.tvTimeRange.setText(String.format("(%s - %s)",
                JDateKit.timeToDate(fmt, beginTime),
                JDateKit.timeToDate(fmt, endTime)));
        Log.d(TAG, "getAppUsage:" + JDateKit.timeToDate(fmt, beginTime) + " " + JDateKit.timeToDate(fmt, endTime));
        // setTitle("数据分析中...");
        //画环形统计图
//        SelfStatistics selfStatistics = mHolder.progress;
//        float datas[] = new float[]{4000,3000,7000,8000};
//        selfStatistics.setDatas(datas);
//        selfStatistics.startDraw();
//        PieChart pieChart = mHolder.progress;

        showLoading("数据分析中...");

        // a task can be executed only once,init is required every time
        mLoadAppUsageTask = new LoadAppUsageTask(beginTime, endTime, list -> {
            mItems = list;
            initAdapter();
        });
        mLoadAppUsageTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadAppUsageTask != null) {
            mLoadAppUsageTask.cancel(true);
            mLoadAppUsageTask = null;
        }
    }

    private long maxTime;// 当前列表中 使用最久的APP时间 用于计算进度条百分比

    //将数据展示在RecyclerView
    private void initAdapter() {
        if (JListKit.isNotEmpty(mItems)) {
            Collections.sort(mItems);// 按使用时长排序
            maxTime = mItems.get(0).getTotalTimeInForeground();//转成总的毫秒数
        } else {
            maxTime = 1;
        }
        setTitle(String.format("%s (共%s条)", getTitle(), mItems.size()));
        //环形统计图设置仅显示前五条数据
        PieChart pieChart = mHolder.progress;
        List<PieData> pieData = new ArrayList<>();
//        pieData.add(new PieData("text", (float) Math.random() * 90));
        for (int i = 0; i < mItems.size(); i++){
            if (i >= 5) break;
            pieData.add(new PieData(mItems.get(i).getAppName(),mItems.get(i).getTotalTimeInForeground()));
//            Log.d(TAG, "initAdapter: " + JDateKit.timeToStringChineChinese(mItems.get(i).getTotalTimeInForeground())); //打印应用使用时长
            Log.d(TAG, "getAppUsage: 应用名称："+mItems.get(i).getAppName());
            Log.d(TAG, "initAdapter: 应用总时长："+mItems.get(i).getTotalTimeInForeground()+"  " + JDateKit.timeToStringChineChinese(mItems.get(i).getTotalTimeInForeground())); //打印应用使用时长
        }
        pieChart.setData(pieData,1);

        if (mAdapter == null) {
            String fmt = "yyyy-MM-dd HH:mm:ss";
            mAdapter = new CommonRecyclerAdapter<AppUsageBean>(R.layout.item_app_usage, mItems) {
                @Override
                protected void convert(@NonNull BaseViewHolder helper, AppUsageBean item) {
                    //设置应用名称和图标
                    helper.setText(R.id.id_tv_app_name, String.format("%s", item.getAppName()));
                    Drawable appIcon = item.getAppIcon();
                    if (appIcon != null) {
                        helper.setImageDrawable(R.id.id_iv_app_icon, appIcon);
                    } else {
                        helper.setImageResource(R.id.id_iv_app_icon, R.mipmap.ic_launcher);
                    }
                    long totalTimeInForeground = item.getTotalTimeInForeground();
                    helper.setText(R.id.id_tv_time_in_foreground, String.format("%s", JDateKit.timeToStringChineChinese(totalTimeInForeground), totalTimeInForeground));
//                    helper.setText(R.id.id_tv_last_usage, String.format("上次使用:%s", JDateKit.timeToDate(fmt, item.getLastTimeUsed())));
                    // 计算进度条百分比
                    float percent = (float) item.getTotalTimeInForeground() / maxTime;
//                    Guideline guideline = helper.getView(R.id.guideline);
//                    guideline.setGuidelinePercent(percent);
//                    loadProgressBar.setProgress((int) percent);
                    ProgressBar loadProgressBar = helper.getView(R.id.loading_progress);
                    loadProgressBar.setProgress((int) (percent*100));
                }
            };
            mHolder.rvAppUsage.setAdapter(mAdapter);
            mHolder.rvAppUsage.setLayoutManager(new LinearLayoutManager(this));
        } else {
            mAdapter.setNewInstance(mItems);
        }
//        PieChart pieChart = mHolder.progress;
//        List<PieData> pieData = new ArrayList<>();
//        for (int i = 0; i < mItems.size(); i++){
//            Log.d(TAG, "initAdapter: " + JDateKit.timeToStringChineChinese(mItems.get(i).getTotalTimeInForeground())); //打印应用使用时长
//            pieData.add(new PieData(mItems.get(i).getAppName(),(float) mItems.get(i).getTotalTimeInForeground()));
//        }
//        pieChart.setData(pieData,1);
        hideLoading();
    }

}
//
//    PieChart pieChart = mHolder.progress;
//    List<PieData> pieData = new ArrayList<>();
//        for (int i = 0; i < mItems.size(); i++){
//        Log.d(TAG, "getAppUsage: 应用名称："+mItems.get(i).getAppName());
//        Log.d(TAG, "initAdapter: 应用总时长："+mItems.get(i).getTotalTimeInForeground()+"  " + JDateKit.timeToStringChineChinese(mItems.get(i).getTotalTimeInForeground())); //打印应用使用时长
////            pieData.add(new PieData(mItems.get(i).getAppName(),(float) mItems.get(i).getTotalTimeInForeground()));
//        }
//        for (int i = 0; i < 6; i++) {
//        pieData.add(new PieData("text" + i, (float) Math.random() * 90));
//        }
//        pieChart.setData(pieData,1);
