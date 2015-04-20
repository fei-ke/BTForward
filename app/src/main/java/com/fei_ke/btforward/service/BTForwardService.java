package com.fei_ke.btforward.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;

import com.alibaba.fastjson.JSON;
import com.fei_ke.btforward.bean.SmsBean;
import com.fei_ke.btforward.event.ConnectEvent;
import com.fei_ke.btforward.event.MessageEvent;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.greenrobot.event.EventBus;

import static com.fei_ke.btforward.event.ConnectEvent.STATE_CONNECTED;
import static com.fei_ke.btforward.event.ConnectEvent.STATE_CONNECTING;
import static com.fei_ke.btforward.event.ConnectEvent.STATE_LISTEN;
import static com.fei_ke.btforward.event.ConnectEvent.STATE_NONE;

/**
 * Created by 杨金阳 on 2015/4/19.
 */
public class BTForwardService extends Service {
    private static BTForwardService instance;

    public static final String EXTRA_DEVICE = "device";
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE = UUID.fromString("79bd6d79-3be1-43c8-aca6-7b473533273b");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("79bd6d79-3be1-43c8-aca6-7b473533273b");

    // Member fields
    private BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    private BluetoothDevice mDevice;

    public static BTForwardService getInstance() {
        return instance;
    }


    Handler handler = new Handler();

    public static synchronized void connect(Context context, BluetoothDevice device) {
        if (instance != null) {
            instance.connect(device);
        } else {
            Intent intent = new Intent(context, BTForwardService.class);
            intent.putExtra(EXTRA_DEVICE, device);
            context.startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;

        start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_DEVICE)) {
            BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
            if (device != null) {
                connect(device);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private synchronized void connect(BluetoothDevice device) {
        connect(mAdapter.getRemoteDevice(device.getAddress()), true);
    }


    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Logger.i(state + "");
        mState = state;
        if (state != STATE_CONNECTED || state != STATE_CONNECTING) {
            mDevice = null;
        }
        //Give the new state to the Handler so the UI Activity can update
        EventBus.getDefault().postSticky(new ConnectEvent(mDevice, state));
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Logger.i("start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Logger.i(device.toString() + " " + secure);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {

        Logger.i("socketType : " + socketType);

        ParcelUuid[] uuids = device.getUuids();
        for (ParcelUuid p : uuids) {
            System.out.println(p.toString());
        }

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        mDevice = device;

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Logger.i("stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        //TODO Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        //Bundle bundle = new Bundle();
        //bundle.putString(Constants.TOAST, "Unable to connect device");
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        //TODO 重试
        //BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        //TODO Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        //Bundle bundle = new Bundle();
        //bundle.putString(Constants.TOAST, "Device connection was lost");
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        //TODO 重试
        //BluetoothChatService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Logger.e("Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Logger.i("Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);

            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Logger.e("Socket Type: " + mSocketType + "accept() failed", e);
                    e.printStackTrace();
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BTForwardService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Logger.e("Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Logger.i("END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Logger.i("Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Logger.e("Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Logger.e("Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Logger.i("BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Logger.e("unable to close() " + mSocketType + " socket during connection failure", e2);
                }
                e.printStackTrace();
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BTForwardService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Logger.e("close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Logger.i("create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Logger.e("temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Logger.i("BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    final String message = new String(buffer, 0, bytes);
                    Logger.d(message);

                    //Send the obtained bytes to the UI Activity
                    EventBus.getDefault().post(new MessageEvent(JSON.parseObject(message, SmsBean.class)));
                } catch (IOException e) {
                    Logger.e("disconnected", e);
                    connectionLost();
                    //TODO 重试
                    //BluetoothChatService.this.start();
                    e.printStackTrace();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                //TODO Share the sent message back to the UI Activity
                //mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Logger.e("Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Logger.e("close() of connect socket failed", e);
            }
        }
    }
}
