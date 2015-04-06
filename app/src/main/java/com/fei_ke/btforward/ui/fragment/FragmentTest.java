package com.fei_ke.btforward.ui.fragment;

import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import com.fei_ke.btforward.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.Random;

/**
 * Created by 杨金阳 on 2015/4/6.
 */
@EFragment(R.layout.fragment_test)
public class FragmentTest extends BaseFrafment {
    public static int i;
    @ViewById
    TextView textView;

    @InstanceState
    String text;

    @AfterViews
    protected void onAfterViews() {
        if (TextUtils.isEmpty(text)) {
            text = i++ + "";
        }
        textView.setText(text);
        Random r = new Random();
        int color = Color.argb(255, r.nextInt(255), r.nextInt(255), r.nextInt(255));
        textView.setBackgroundColor(color);
    }

}
