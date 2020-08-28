package com.example.bluetoothsmart.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import com.example.bluetoothsmart.Config;
import com.example.bluetoothsmart.MyApplication;
import com.example.bluetoothsmart.R;
import com.example.bluetoothsmart.Service.BluetoothService;

import java.util.List;

public class BaseActivity extends Activity {
    protected MyApplication application;
    public BluetoothService mBluetoothService;
    //判断是否前台进程
    public boolean isActive = true;
    //单击时间间隔
    private long clickInterval = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        application = (MyApplication)getApplication();
        application.addActivity(this);
        mBluetoothService = application.getBluetoothLeService();
    }

    @Override
    protected void onResume(){
        // TODO Auto-generated method stub
        super.onResume();

        if(!isActive){
            //APP从后台唤醒
            isActive = true;
        }
    }

    @Override
    protected void onPause(){
        // TODO Auto-generated method stub
        super.onPause();
        if(!isAppOnForeground()){
            //APP进入后台
            isActive = false;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        application.removeActivity(this);

    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    private boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null){
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * 连续双击退出软件
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - clickInterval) > 2000) {
                Toast.makeText(BaseActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                clickInterval = System.currentTimeMillis();
            }
            else {
                finish();
                application.finishAll();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }





    /**
     * 数据接收
     * @param data
     */
    private void dataReceive(String data) {
        if (data != null) {
            Log.d("receive_data",data);
            String[] dataArr = data.split(" ");
            int[] receivedDataArr = new int[dataArr.length];
            for (int i = 0; i < receivedDataArr.length; i++) {
                receivedDataArr[i] = Integer.parseInt(dataArr[i], 16);
            }

        }
    }



    private void showConnectStaust(final boolean connectState){
        //doing something（蓝牙状态显示）
    }
}