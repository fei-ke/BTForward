package com.fei_ke.btforward.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.alibaba.fastjson.JSON;
import com.fei_ke.btforward.bean.SmsBean;
import com.fei_ke.btforward.service.BTForwardService;
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
                Bundle bundle = intent.getExtras();
                Object messages[] = (Object[]) bundle.get("pdus");
                if (messages != null && messages.length > 0) {
                    SmsMessage smsMessage[] = new SmsMessage[messages.length];
                    for (int n = 0; n < smsMessage.length; n++) {
                        smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                    }
                    SmsBean smsBean = new SmsBean();
                    StringBuilder contentBuider = new StringBuilder();
                    for (SmsMessage message : smsMessage) {
                        String content = message.getMessageBody();//得到短信内容
                        String sender = message.getOriginatingAddress();//得到发件人号码

                        smsBean.setFrom(sender);
                        contentBuider.append(content);
                    }
                    smsBean.setBody(contentBuider.toString());
                    Logger.i(smsBean.toString());
                    BTForwardService.getInstance().write(JSON.toJSONBytes(smsBean));
                }
            }
        }
    }
}
