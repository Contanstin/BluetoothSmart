package com.example.bluetoothsmart.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bluetoothsmart.BleDeviceAdapter;
import com.example.bluetoothsmart.ClientManager;
import com.example.bluetoothsmart.Config;
import com.example.bluetoothsmart.MyApplication;
import com.example.bluetoothsmart.R;
import com.example.bluetoothsmart.Service.BluetoothService;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private Button btn_search;
    private ListView list_device;
    private long OVERTIME = 10000;
    private ArrayList<SearchResult> bleDeviceList = new ArrayList<>();
    private static final int REQUEST_ENABLE_BLE = 1;
    private ProgressDialog dialog;
    private BleDeviceAdapter bleDeviceAdapter;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String[] permissions = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION};
        Config.requestPermissions(permissions, this);
        //获取蓝牙服务
        if(!ClientManager.getClient().isBleSupported()){
            Toast.makeText(this,"不支持蓝牙！",Toast.LENGTH_SHORT).show();
            MainActivity.this.finish();
            return;
        }
        init();
        mBluetoothService = application.getBluetoothLeService();

    }

    protected void onResume(){
        // TODO Auto-generated method stub
        super.onResume();
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!ClientManager.getClient().isBluetoothOpened()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLE);
        }
        registerReceiver(mBroadcastReceiver,makeGattUpdateIntentFilter());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BLE && resultCode == Activity.RESULT_CANCELED) {
            MainActivity.this.finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy(){
        // TODO Auto-generated method stub
        super.onDestroy();
        scanBleDevice(false);
        unregisterReceiver(mBroadcastReceiver);
    }


    private void init(){

        bleDeviceAdapter=new BleDeviceAdapter(MainActivity.this,bleDeviceList);
        list_device=findViewById(R.id.device_list);

        list_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                final SearchResult device=bleDeviceList.get(position);
                if(device==null){
                    return;
                }
                scanBleDevice(false);
                mBluetoothService.connect(device.getAddress());
                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeProgress();
                        if(!application.isConnect){


                        }
                    }
                },4000);
                showProgress();
            }
        });
        list_device.setAdapter(bleDeviceAdapter);
        btn_search=findViewById(R.id.search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleDeviceList.clear();
                bleDeviceAdapter.notifyDataSetChanged();
                scanBleDevice(true);
            }
        });
    }
    /**
     * 蓝牙扫描
     * @param enable
     */
    private void scanBleDevice(final boolean enable){
        if(enable){
            bleDeviceList.clear();
            ClientManager.getClient().search(request, bleSearchResponse);
        }else {

        }
        invalidateOptionsMenu();
    }
    SearchRequest request = new SearchRequest.Builder()
            //扫描BLE设备10秒
            .searchBluetoothLeDevice(10000)
            //扫描经典蓝牙2秒
            .searchBluetoothClassicDevice(2000)
            //扫描BLE设备4秒
            .searchBluetoothLeDevice(4000)
            .build();

    SearchResponse bleSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            try{
                if(bleDeviceList == null){
                    return;
                }
                if(device == null){
                    return;
                }
                if(bleContains(device.device)){
                    for(SearchResult ble : bleDeviceList){
                        if(ble.getAddress().contentEquals(device.getAddress())){
                            ble.rssi = device.rssi;
                           bleDeviceAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
                else{
                    if(device.getName() != null){
//                        && device.getName().contains("MT")){
                        if(!device.getName().equals("NULL")) {
                            bleDeviceList.add(device);
                        }
                        bleDeviceAdapter.notifyDataSetChanged();
                    }
                }
            }
            catch (Exception e){
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        @Override
        public void onSearchStopped() {

        }

        @Override
        public void onSearchCanceled() {

        }
    };
    /**
     * 判断蓝牙List是否包含device
     * @param device
     * @return true：包含；false：不包含
     */
    boolean bleContains(BluetoothDevice device){
        if (device == null) {
            return false;
        }
        if (bleDeviceList == null) {
            return false;
        }
        for(SearchResult ble: bleDeviceList){
            if(ble.getAddress().contentEquals(device.getAddress())){
                return true;
            }
        }
        return false;
    }

    /**
     * 蓝牙接收广播
     */
    public final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bleReceive(context, intent);
        }
    };


    public void bleReceive(Context context, Intent intent){
        final String action = intent.getAction();
        if(BluetoothService.ACTION_GATT_CONNECTED.equals(action)){
            Config.connectState = true;
            application.isConnect=true;
            closeProgress();
            Intent bleIntent=new Intent(MainActivity.this,ModificationActivity.class);
            startActivity(bleIntent);
            MainActivity.this.finish();
        }
        else if(BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)){
            Config.connectState = false;
            application.isConnect=false;
        }else if(BluetoothService.ACTION_GATT_CONNECTED.equals(action)){
            Config.connectState=true;
            application.isConnect = true;
        }else if(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
            Config.connectState=true;
            application.isConnect = true;
        }else if(BluetoothService.WRITE_DATA.equals(action)){
            Config.connectState=true;
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.ACTION_DATA_WRITE);
        return intentFilter;
    }
    /**
     * 显示进度条
     */
    private void showProgress() {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog == null) {
                    dialog = ProgressDialog.show(MainActivity.this, "",
                            "正在连接");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        });
    }

    /**
     * 关闭进度条
     */
    private void closeProgress() {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
    }
}