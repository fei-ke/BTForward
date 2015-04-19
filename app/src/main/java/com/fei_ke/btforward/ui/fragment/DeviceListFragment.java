package com.fei_ke.btforward.ui.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.fei_ke.btforward.R;
import com.orhanobut.logger.Logger;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by 杨金阳 on 2015/4/19.
 */
@EFragment(R.layout.fragment_device_list)
public class DeviceListFragment extends BaseFragment {
    @ViewById
    protected ExpandableListView listView;

    private DeviceListAdapter listAdapter;

    private BluetoothAdapter mBtAdapter;
    private List<BluetoothDevice> scanDevices;

    public static DeviceListFragment newInstance() {
        return DeviceListFragment_.builder().build();
    }

    @Override
    protected void onAfterViews() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        listAdapter = new DeviceListAdapter();
        listView.setAdapter(listAdapter);
        scanDevices = new ArrayList<>();

        getDevices();

        setHasOptionsMenu(true);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Logger.i("注册监听器");

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);

    }

    @Override
    public void onDetach() {
        Logger.i("注销监听器");
        getActivity().unregisterReceiver(mReceiver);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_device_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            scanDevice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDevices() {
        Set set = mBtAdapter.getBondedDevices();
        List<BluetoothDevice> bondedDevices = new ArrayList<>(set);

        List<List<BluetoothDevice>> devices = new ArrayList<>();
        devices.add(bondedDevices);

        devices.add(scanDevices);

        listAdapter.setData(devices);
        listAdapter.notifyDataSetChanged();

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return true;
            }
        });
    }

    private void scanDevice() {

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (!scanDevices.contains(device)) {
                        scanDevices.add(device);
                        listAdapter.notifyDataSetChanged();
                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Logger.i("扫描完成");
            }
        }
    };

    private static class DeviceListAdapter extends BaseExpandableListAdapter {
        private List<List<BluetoothDevice>> mData;

        public List<List<BluetoothDevice>> getData() {
            return mData;
        }

        public void setData(List<List<BluetoothDevice>> data) {
            this.mData = data;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).size();
        }

        @Override
        public List<BluetoothDevice> getGroup(int groupPosition) {
            return mData.get(groupPosition);
        }

        @Override
        public BluetoothDevice getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = new TextView(parent.getContext());
            textView.setText(groupPosition + "");
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view = View.inflate(parent.getContext(), R.layout.layout_divice_item, null);
            TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
            TextView textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);

            BluetoothDevice device = getChild(groupPosition, childPosition);
            textViewName.setText(device.getName());
            textViewAddress.setText(device.getAddress());
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
