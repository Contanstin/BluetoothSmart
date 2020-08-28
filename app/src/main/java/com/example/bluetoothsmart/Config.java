package com.example.bluetoothsmart;



import android.app.Activity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.UUID;

import io.reactivex.functions.Consumer;


public class Config {
    //服务
    public static final String UUID_SERVER = "0000ff12-0000-1000-8000-00805f9b34fb";
    //间隔
    public static final String UUID_Interval = "0000ff04-0000-1000-8000-00805f9b34fb";

    //名字
    public static final String UUID_Name = "0000ff06-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
            .fromString("00002a37-0000-1000-8000-00805f9b34fb");
    //连接状态
    public static  boolean connectState=false;
    /**
     * 权限请求（多个权限同时请求）
     * @param permissions 权限数组
     * @param activity
     */
    public static void requestPermissions(String[] permissions,final Activity activity){
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(permissions).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if(!aBoolean){
                    //Common.toast(activity.getResources().getString(R.string.login_func_limit));
                }
            }
        });
    }

}
