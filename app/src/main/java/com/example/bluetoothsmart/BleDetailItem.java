package com.example.bluetoothsmart;

import java.util.UUID;

public class BleDetailItem {
    public static final int TYPE_SERVICE = 0;
    public static final int TYPE_CHARACTER = 1;

    public int type;
    public int properties;
    public UUID uuid_service;
    public UUID uuid_character;

    public BleDetailItem(int type,int properties,UUID uuid_service,UUID uuid_character) {
        this.type = type;
        this.uuid_service = uuid_service;
        this.uuid_character = uuid_character;
        this.properties = properties;
    }
}
