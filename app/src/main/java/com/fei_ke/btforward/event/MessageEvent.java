package com.fei_ke.btforward.event;

import com.fei_ke.btforward.bean.SmsBean;

/**
 * Created by 杨金阳 on 2015/4/20.
 */
public class MessageEvent {
    SmsBean smsBean;

    public MessageEvent(SmsBean smsBean) {
        this.smsBean = smsBean;
    }

    public SmsBean getSmsBean() {
        return smsBean;
    }

    public void setSmsBean(SmsBean smsBean) {
        this.smsBean = smsBean;
    }
}
