package com.fei_ke.btforward.ui.fragment;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by 杨金阳 on 2015/4/6.
 */
@EFragment
public abstract class BaseFragment extends Fragment {
    @AfterViews
    protected void privateAfterViews() {
        onAfterViews();
    }

    protected abstract void onAfterViews();
}
