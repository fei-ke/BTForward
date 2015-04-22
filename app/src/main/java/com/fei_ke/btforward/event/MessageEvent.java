package com.fei_ke.btforward.event;

import android.bluetooth.BluetoothDevice;

import com.fei_ke.btforward.bean.SmsBean;

/**
 * Created by 杨金阳 on 2015/4/20.
 */
public class MessageEvent {
    SmsBean smsBean;
    BluetoothDevice device;

    public MessageEvent(SmsBean smsBean) {
        this.smsBean = smsBean;
    }

    public SmsBean getSmsBean() {
        return smsBean;
    }

    public void setSmsBean(SmsBean smsBean) {
        this.smsBean = smsBean;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
