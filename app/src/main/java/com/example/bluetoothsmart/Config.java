package com.example.bluetoothsmart;



import java.util.UUID;

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

}
