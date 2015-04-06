package com.fei_ke.btforward.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Stack;

/**
 */
public class MainAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
    private Stack<Fragment> fragments;
    private ViewPager viewPager;

    public MainAdapter(FragmentManager fm, ViewPager viewPager) {
        super(fm);
        this.viewPager = viewPager;
        fragments = new Stack<>();
        viewPager.setAdapter(this);
        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    public void add(Fragment fragment) {
        int size = fragments.size();
        fragments.push(fragment);
        notifyDataSetChanged();
        viewPager.setCurrentItem(size);
    }

    public void pop() {
        int last = fragments.size() - 1;
        if (last != 0) {
            viewPager.setCurrentItem(last - 1);
        }
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        if (fragments.contains(object)) {
            return PagerAdapter.POSITION_UNCHANGED;
        }
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (ViewPager.SCROLL_STATE_IDLE == state) {
            int cur = viewPager.getCurrentItem();
            if (cur < fragments.size() - 1) {
                while (cur < fragments.size() - 1) {
                    fragments.pop();
                }
                notifyDataSetChanged();
            }
        }
    }
}
