package com.example.bluetoothsmart.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothsmart.Activity.ModificationActivity;
import com.example.bluetoothsmart.BleDetailItem;
import com.example.bluetoothsmart.ClientManager;
import com.example.bluetoothsmart.Config;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.example.bluetoothsmart.Config.UUID_CONFIG_DESCRIPTOR;
import static com.example.bluetoothsmart.Config.UUID_HEART_RATE_MEASUREMENT;
import static com.example.bluetoothsmart.Config.UUID_Interval;
import static com.example.bluetoothsmart.Config.UUID_Name;
import static com.example.bluetoothsmart.Config.UUID_SERVER;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

public class BluetoothService extends Service {
    //获取类BleService简写名
    private final static String TAG = BluetoothService.class.getSimpleName();

    private Handler mHandler = new Handler();
    public List<BleDetailItem> bleServiceCharacters = new ArrayList<BleDetailItem>();
    public String mBluetoothDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothGatt mBluetoothGatt;
    //名字的BluetoothGattCharacteristic
    private BluetoothGattCharacteristic SERVER_Characteristic;
    //间隔的BluetoothGattCharacteristic
    private BluetoothGattCharacteristic BLUE_DATA;
    private BluetoothManager mBluetoothManager;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetoothsmart.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetoothsmart.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetoothsmart.ACTION_DATA_AVAILABLE";
    public final static String RECEIVE_DATA = "com.example.bluetoothsmart.RECEIVE_DATA";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetoothsmart.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_WRITE = "com.example.bluetoothsmart.ACTION_DATA_WRITE";
    public final static String WRITE_DATA = "com.example.bluetoothsmart.Write";
    public final static String EXTRA_DATA = "com.example.bluetoothsmart.EXTRA_DATA";


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
//				Log.e(TAG, "Attempting to start service discovery:"
//						+ );
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                //Log.e(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                List<BluetoothGattService> servers = getSupportedGattServices();
                for (BluetoothGattService gattService : servers) {

                    if (gattService.getUuid().toString()
                            .equals(UUID_SERVER)) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                                .getCharacteristics();

                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                            if (gattCharacteristic
                                    .getUuid()
                                    .toString()
                                    .equals(UUID_Name)) {
                                SERVER_Characteristic = gattCharacteristic;
                            } else if (gattCharacteristic
                                    .getUuid()
                                    .toString()
                                    .equals(UUID_Interval)) {
                                final int charaProp = gattCharacteristic
                                        .getProperties();
                                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    BLUE_DATA = gattCharacteristic;
                                    readBleuData();
                                    setCharacteristicNotification(
                                            gattCharacteristic, true);
                                    BluetoothGattDescriptor clientConfig = gattCharacteristic
                                            .getDescriptor(UUID_CONFIG_DESCRIPTOR);

                                    if (clientConfig != null) {
                                        clientConfig
                                                .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        mBluetoothGatt
                                                .writeDescriptor(clientConfig);

                                        Log.e(TAG,
                                                "setCharacteristicNotification success!");
                                    } else {
                                        Log.e(TAG,
                                                "setCharacteristicNotification failed! descriptor is null! ");
                                    }
                                }
                            }
                        }

                    }

                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
//			Log.e("getData",
//					String.valueOf(characteristic.getValue().toString())
//							+ "========");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //
//			Log.e("getData2",
//					String.valueOf(characteristic.getValue().toString())
//							+ "========");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(
                        data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));

                // LogUtil.e("write", stringBuilder.toString());
                // readBleuData();
                broadcastUpdate(ACTION_DATA_WRITE, characteristic);

            }

        }

    };
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                //Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        return mBluetoothAdapter != null;
    }
    /**
     * 获取到蓝牙数据后发送通知
     *
     * @param action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            // Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            // intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(
                        data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));

                if (action.equals(ACTION_DATA_WRITE)) {
                    intent.putExtra(WRITE_DATA, stringBuilder.toString());
                } else {
                    intent.putExtra(EXTRA_DATA, stringBuilder.toString());
                }

            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();



    /**
     * 连接蓝牙
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
//			Log.w(TAG,
//					"BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG,
                    "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * 断开蓝牙连接
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.disconnect();
                close();
            }
        }, 100);

//		close();
    }

    /**
     * 使用给定的BLE设备后,应用程序必须调用这个方法,以确保正确地释放资源
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        SERVER_Characteristic = null;
        BLUE_DATA = null;

    }

    /**
     * 向蓝牙写数据
     */
    private void sendCommandImmediate(
            BluetoothGattCharacteristic bleGattCharacteristic,
            BluetoothGatt bleGatt, byte[] command) {

        Log.i(TAG, "sendCommandImmediate");
        if (bleGattCharacteristic == null) {
            Log.e("bleGattCharacteristic", "null");
        }
        if (bleGattCharacteristic != null && bleGatt != null) {
            bleGattCharacteristic
                    .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            bleGattCharacteristic.setValue(command);
            bleGatt.writeCharacteristic(bleGattCharacteristic);
            // readBleuData();
            Log.i(TAG, "writeCharacteristic");
        } else {
            Log.e(
                    TAG,
                    "sendCommandImmediate error! please check bleGattCharacteristic,bleGatt if initial");
        }
    }



    /**
     * 向蓝牙写入名字
     */
    public void writeBLueToothName(byte[] names) {

        sendCommandImmediate(SERVER_Characteristic, mBluetoothGatt, names);


    }
    /**
     * 向蓝牙写入间隔
     */
    public void writeBLueToothInterval(byte[] interval) {



        sendCommandImmediate(BLUE_DATA, mBluetoothGatt, interval);


    }




    /**
     * 读取数据
     */
    public void readBleuData() {
        Log.e(TAG, "readBattery");
        if (mBluetoothGatt != null) {
            if (BLUE_DATA != null) {
                mBluetoothGatt.readCharacteristic(BLUE_DATA);
            } else {
                Log.w(TAG, "BATTERY_CHAR is null");
            }
        }
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        disconnect();
        close();
    }
}

