package com.fei_ke.btforward.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fei_ke.btforward.R;
import com.fei_ke.btforward.ui.SlideTransformer;
import com.fei_ke.btforward.ui.adapter.MainAdapter;
import com.fei_ke.btforward.ui.fragment.FragmentTest_;
import com.orhanobut.logger.Logger;


public class MainActivity extends ActionBarActivity {
    protected ViewPager viewPager;
    private MainAdapter mainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init().setMethodCount(1).hideThreadInfo();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(this, R.id.viewPager);
        mainAdapter = new MainAdapter(getSupportFragmentManager(), viewPager);
        viewPager.setPageTransformer(false, new SlideTransformer());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);
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
            mainAdapter.pop();
        } else if (itemId == R.id.action_test) {
            mainAdapter.add(new FragmentTest_());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mainAdapter.pop();
    }

    public static <T> T findViewById(View v, int id) {
        return (T) v.findViewById(id);
    }

    public static <T> T findViewById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }
}
