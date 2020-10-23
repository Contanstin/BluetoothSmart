package com.example.bluetoothsmart.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothsmart.Config;
import com.example.bluetoothsmart.R;
import com.example.bluetoothsmart.Service.BluetoothService;

import net.steamcrafted.loadtoast.LoadToast;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModificationActivity extends BaseActivity implements View.OnClickListener {
    private final String tag = "ModificationActivity";
    private TextView textView;
    private ImageButton back;
    private ImageView ble_connect;
    private EditText name;
    private EditText interval;
    private Button startsetName;
    private Button startsetInterval;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modification);
        init();
        showConnectStaust(Config.connectState);
        if (!Config.connectState) {
            Toast.makeText(ModificationActivity.this,
                    "蓝牙未连接，请点击返回连接蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, bleBroadcastIntentFilter());
        showConnectStaust(Config.connectState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void init() {
        ble_connect = findViewById(R.id.ble_state);
        ble_connect.setVisibility(View.VISIBLE);
//        back=findViewById(R.id.title_backBtn);
        textView = findViewById(R.id.title_centerText);
        name = findViewById(R.id.name);
        interval = findViewById(R.id.Interval);
        startsetName = findViewById(R.id.startsetName);
        startsetInterval = findViewById(R.id.startsetInterval);
        ble_connect.setOnClickListener(this);
        startsetName.setOnClickListener(this);
        startsetInterval.setOnClickListener(this);
//        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.title_backBtn:
//                Intent intent=new Intent(ModificationActivity.this,MainActivity.class);
//                startActivity(intent);
//                ModificationActivity.this.finish();
//                break;
            case R.id.ble_state:
                break;
            case R.id.startsetName:
                ChangeName();
                break;
//            case R.id.startsetInterval:
//                ChangeInterval();
//                break;
            default:
                break;
        }
    }

    /**
     * 修改蓝牙的间隔
     */
    private void ChangeInterval() {
        if (interval.getText().toString().length() == 0) {
            Toast.makeText(this, "请输入有效的间隔", Toast.LENGTH_SHORT).show();
        } else {
            byte[] bytes = HextoByte(interval.getText().toString());
            mBluetoothService.writeBLueToothInterval(bytes);
            Toast.makeText(this, "间隔设置成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 修改蓝牙的名字
     */
    private void ChangeName() {
        String mName = name.getText().toString();
        if (mName.length() != 6) {
            final LoadToast toast = new LoadToast(ModificationActivity.this);
            toast.setText("请输入有效的名字");
            toast.setTextColor(Color.RED);
            toast.setTranslationY(1400);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.hide();
                }
            }, 3000);

        } else {
            String tmp="MT"+name.getText().toString();
            byte[] names = HextoByte(tmp);
            mBluetoothService.writeBLueToothName(names);
            final LoadToast toast = new LoadToast(ModificationActivity.this);
            toast.setText("名字设置成功");
            toast.setTextColor(Color.RED);
            toast.setTranslationY(1400);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.hide();
                }
            }, 3000);
        }

    }

    /**
     * 去掉空格和换行符
     *
     * @param str 待处理字符串
     * @return 处理后字符串
     */
    private String replaceBlankAndBreakLine(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * @param hex 16进制字符串转byte数组
     * @return
     */
    private byte[] HextoByte(String hex) {
        byte[] bytes = hex.getBytes();
        return bytes;
    }

    /**
     * 蓝牙连接状态处理
     *
     * @param connectState
     */
    private void showConnectStaust(final boolean connectState) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connectState) {
                    ble_connect.setImageResource(R.mipmap.ble_contect);
                } else {
                    ble_connect.setImageResource(R.mipmap.ble_disconnect);
                    final LoadToast toast = new LoadToast(ModificationActivity.this);
                    toast.setText("连接超时，请检查蓝牙设备是否正常");
                    toast.setTextColor(Color.RED);
                    toast.setTranslationY(2000);
                    toast.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.hide();
                        }
                    }, 4500);
                }
            }
        });
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


    public void bleReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
            Config.connectState = true;
            application.isConnect = true;
        } else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
            Config.connectState = false;
            application.isConnect = false;
            showConnectStaust(Config.connectState);
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("蓝牙断开连接");
            builder.setMessage("连接超时，请检查蓝牙设备是否正常");
            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent1 = new Intent(ModificationActivity.this, MainActivity.class);
                    startActivity(intent1);
                    ModificationActivity.this.finish();
                }
            });
           builder.create();
           builder.show();
        }
    }

    /**
     * 广播过滤策略
     *
     * @return
     */
    private static IntentFilter bleBroadcastIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}