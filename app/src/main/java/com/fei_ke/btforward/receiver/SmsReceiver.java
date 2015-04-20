package com.fei_ke.btforward.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

/**
 * Created by 杨金阳 on 2015/4/20.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Logger.i(intent.toString());
            if (intent.getExtras() != null) {
                Logger.i(intent.getExtras().toString());
            }
        }
    }
}
