package com.fei_ke.btforward.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.alibaba.fastjson.JSON;
import com.fei_ke.btforward.R;
import com.fei_ke.btforward.bean.SmsBean;
import com.fei_ke.btforward.service.BTForwardService;
import com.fei_ke.btforward.ui.SlideTransformer;
import com.fei_ke.btforward.ui.adapter.MainAdapter;
import com.fei_ke.btforward.ui.fragment.DeviceListFragment;
import com.fei_ke.btforward.ui.view.FixedSpeedScroller;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;


public class MainActivity extends AppCompatActivity {
    protected ViewPager viewPager;
    private MainAdapter mainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startService(new Intent(this, BTForwardService.class));
        Logger.i("启动服务");

        Logger.init().setMethodCount(1).hideThreadInfo();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(this, R.id.viewPager);
        mainAdapter = new MainAdapter(getSupportFragmentManager(), viewPager);
        viewPager.setPageTransformer(false, new SlideTransformer());

        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(),
                    new DecelerateInterpolator());
            field.set(viewPager, scroller);
            scroller.setmDuration(300);
        } catch (Exception e) {
            e.printStackTrace();
        }


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);


        DeviceListFragment deviceListFragment = DeviceListFragment.newInstance();
        mainAdapter.add(deviceListFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.action_test) {
            BTForwardService instance = BTForwardService.getInstance();
            if (instance != null) {
                SmsBean smsBean = new SmsBean();
                smsBean.setFrom("10001");
                smsBean.setBody("短信测试");
                instance.write(JSON.toJSONBytes(smsBean));
            }
            //mainAdapter.add(new FragmentTest_());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mainAdapter.pop()) {
            super.onBackPressed();
        }
    }

    public static <T> T findViewById(View v, int id) {
        return (T) v.findViewById(id);
    }

    public static <T> T findViewById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }
}
