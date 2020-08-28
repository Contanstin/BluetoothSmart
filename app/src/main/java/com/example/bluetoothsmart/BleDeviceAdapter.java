package com.example.bluetoothsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchResult;

import java.util.ArrayList;

public class BleDeviceAdapter extends BaseAdapter {
    private ArrayList<SearchResult> deviceList = new ArrayList<>();
    private Context mContext;

    public BleDeviceAdapter(Context mContext, ArrayList<SearchResult> deviceList){
        this.deviceList = deviceList;
        this.mContext = mContext;
    }
    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return deviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        holderView hView = null;
        if(convertView == null){
            hView = new holderView();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.scandevice,null);
            hView.txt_name = (TextView)convertView.findViewById(R.id.scandevice_name);
            convertView.setTag(hView);
        }
        else{
            hView = (holderView)convertView.getTag();
        }

        SearchResult device = deviceList.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            hView.txt_name.setText(deviceName);
        }
        else {
            hView.txt_name.setText("未知设备");
        }
        return convertView;
    }
    public class holderView {
        public TextView txt_name;
    }
}
