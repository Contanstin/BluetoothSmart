package com.example.bluetoothsmart.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

import java.nio.ByteBuffer;

public class ModificationActivity extends BaseActivity implements View.OnClickListener {
    private final String tag="ModificationActivity";
    private TextView textView;
    private ImageButton back;
    private ImageView ble_connect;
    private EditText name;
    private EditText interval;
    private Button startset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modification);
        init();
        showConnectStaust(Config.connectState);
        if(!Config.connectState){
            Toast.makeText(ModificationActivity.this,
                    "蓝牙未连接，请点击返回连接蓝牙",Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        ble_connect=findViewById(R.id.ble_state);
        ble_connect.setVisibility(View.VISIBLE);
        back=findViewById(R.id.title_backBtn);
        textView=findViewById(R.id.title_centerText);
        name=findViewById(R.id.name);
        interval=findViewById(R.id.Interval);
        startset=findViewById(R.id.startset);
        ble_connect.setOnClickListener(this);
        startset.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_backBtn:
                Intent intent=new Intent(ModificationActivity.this,MainActivity.class);
                startActivity(intent);
                ModificationActivity.this.finish();
                break;
            case R.id.ble_state:
                if(!Config.connectState){
                    Intent intent1=new Intent(ModificationActivity.this,MainActivity.class);
                    startActivity(intent1);
                }
                break;
            default:
                break;
            case R.id.startset:
                byte[] names=HextoByte(name.getText().toString());
                byte[] bytes=HextoByte(interval.getText().toString());
                    mBluetoothService.writeBLueToothName(names);
                    mBluetoothService.writeBLueToothInterval(bytes);
                    Toast.makeText(this,"名字和间隔设置成功",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param hex 16进制字符串转byte数组
     * @return
     */
    private byte[] HextoByte(String hex){
        byte[] bytes=hex.getBytes();
        return bytes;
    }
    /**
     * 蓝牙连接状态处理
     * @param connectState
     */
    private void showConnectStaust(final boolean connectState) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connectState) {
                    ble_connect.setImageResource(R.mipmap.ble_contect);
                }
                else {
                    ble_connect.setImageResource(R.mipmap.ble_disconnect);
                }
            }
        });
    }

}