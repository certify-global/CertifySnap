package com.certify.snap.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.bluetooth.model.DeviceItem;

import java.util.ArrayList;


public class DeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<DeviceItem> data;
    private LayoutInflater mInflator;
    private int layout;

    public DeviceListAdapter(Context context, int layout) {
        this.mLeDevices = new ArrayList<BluetoothDevice>();
        this.mInflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
        this.data=new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
            DeviceItem deviceItem = new DeviceItem(device.getName(), device.getAddress());
            data.add(deviceItem);
            notifyDataSetChanged();
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        // general listView optimization code.
        if (view == null)
            view = mInflator.inflate(layout, viewGroup, false);
        DeviceItem deviceItem = data.get(pos);

        // setText device name
        TextView data = (TextView)view.findViewById(R.id.item_bluetoothdevice_tv_deviceid);
        data.setText(deviceItem.getName());

        // setText device uuid address
        TextView content = (TextView)view.findViewById(R.id.item_bluetoothdevice_tv_uuid);
        content.setText(deviceItem.getAddress());

        return view;
    }
}