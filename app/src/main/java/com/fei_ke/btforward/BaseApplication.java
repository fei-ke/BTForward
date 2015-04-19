package com.fei_ke.btforward;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by 杨金阳 on 2015/4/19.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init().setMethodCount(1).hideThreadInfo();
    }
}
