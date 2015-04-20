package com.fei_ke.btforward.event;

import android.bluetooth.BluetoothDevice;

/**
 * Created by 杨金阳 on 2015/4/20.
 */
public class ConnectEvent {
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private BluetoothDevice device;
    private int state;

    public ConnectEvent(BluetoothDevice device, int state) {
        this.device = device;
        this.state = state;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
