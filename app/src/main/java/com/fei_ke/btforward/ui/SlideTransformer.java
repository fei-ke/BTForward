package com.fei_ke.btforward.ui;

import android.view.View;

import com.ToxicBakery.viewpager.transforms.ABaseTransformer;

/**
 * Created by 杨金阳 on 2015/4/6.
 */
public class SlideTransformer extends ABaseTransformer {
    private static final float MIN_SCALE = 0.75F;

    public SlideTransformer() {
    }

    protected void onTransform(View view, float position) {
        if (position >= 0.0F && position <= 1.f) {
            view.setTranslationX(0.0F);
            view.setScaleX(1.0F);
            view.setScaleY(1.0F);
            float alpha = 1.0F - position;
            view.setAlpha(alpha > 0.5f ? alpha : 0.5f);
        } else if (position < 0.0F) {
            float scaleFactor = 0.75F + 0.25F * (1.0F - Math.abs(position));
            view.setAlpha(1.0F + position);
            view.setPivotY(0.5F * (float) view.getHeight());
            view.setTranslationX((float) view.getWidth() * -position);
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        }

    }

    protected boolean isPagingEnabled() {
        return false;
    }
}
