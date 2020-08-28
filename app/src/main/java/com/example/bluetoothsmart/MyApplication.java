package com.example.bluetoothsmart;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.bluetoothsmart.Service.BluetoothService;
import com.inuker.bluetooth.library.BluetoothContext;

import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application {
    public long outTime = System.currentTimeMillis();
    private static MyApplication instance;
    public BluetoothService mBluetoothService;
    public boolean isConnect;
    private List<Activity> activityList = new LinkedList<>();

    @Override
    public void onCreate(){
        // TODO Auto-generated method stub
        super.onCreate();
        initApplication();
        instance = this;
        BluetoothContext.set(this);
    }

    public static Application getInstance(){
        return instance;
    }

    protected final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()){

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    private  void initApplication(){
        Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }
    public BluetoothService getBluetoothLeService() {
        // TODO Auto-generated method stub
        return this.mBluetoothService;
    }

    /**添加activity
     * @param activity
     */
    public void addActivity(Activity activity){
        activityList.add(activity);
    }

    /**
     * 移除activity
     * @param activity
     */
    public void removeActivity(Activity activity){
        activityList.remove(activity);
        if(!activity.isFinishing()){
            activity.finish();
        }
    }

    /**
     * 关闭所有activity
     */
    public void finishAll(){
        for(Activity activity:activityList){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
        //关闭蓝牙服务
        if(mBluetoothService != null){
            mBluetoothService.disconnect();
            unbindService(mServiceConnection);
            mBluetoothService = null;
        }
        System.exit(0);
    }


}
