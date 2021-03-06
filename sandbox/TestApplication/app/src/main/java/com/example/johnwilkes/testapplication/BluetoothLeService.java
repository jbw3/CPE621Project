package com.example.johnwilkes.testapplication;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

public class BluetoothLeService extends Service
{
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING   = 1;
    private static final int STATE_CONNECTED    = 2;

    public final static UUID UUID_GENERIC_ACCESS = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_GENERIC_ATTRIBUTE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_IMMEDIATE_ALERT = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

    public final static HashMap<UUID, String> GATT_SERVICE_NAMES;
    static
    {
        GATT_SERVICE_NAMES = new HashMap<>();
        GATT_SERVICE_NAMES.put(UUID_GENERIC_ACCESS,    "GenericAccess");
        GATT_SERVICE_NAMES.put(UUID_GENERIC_ATTRIBUTE, "GenericAttribute");
        GATT_SERVICE_NAMES.put(UUID_IMMEDIATE_ALERT,   "ImmediateAlert");
    }

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private BluetoothGatt bluetoothGatt = null;
    private int mConnectionState = STATE_DISCONNECTED;

    // ------ GATT Callback ------

    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback()
            {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
                {
                    Log.d(TAG, "onConnectionStateChange");

                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED)
                    {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                bluetoothGatt.discoverServices());
                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED)
                    {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status)
                {
                    Log.d(TAG, "onServicesDiscovered");

                    Log.d(TAG, String.format("found %s services", gatt.getServices().size()));
                    for (BluetoothGattService service : gatt.getServices())
                    {
                        Log.d(TAG, String.format("%s", service.getUuid().toString()));
                    }

                    if (status == BluetoothGatt.GATT_SUCCESS)
                    {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    }
                    else
                    {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    Log.d(TAG, "onCharacteristicRead");

                    if (status == BluetoothGatt.GATT_SUCCESS)
                    {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                private void broadcastUpdate(final String action)
                {
                    final Intent intent = new Intent(action);
                    sendBroadcast(intent);
                }

                private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
                {
                    final Intent intent = new Intent(action);

                    // write the data formatted in HEX
                    final byte[] data = characteristic.getValue();
                    if (data != null && data.length > 0)
                    {
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for (byte byteChar : data)
                        {
                            stringBuilder.append(String.format("%02X ", byteChar));
                        }
                        intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                                stringBuilder.toString());
                    }
                    sendBroadcast(intent);
                }
            };

    // ------ Binder ------
    public class BleBinder extends Binder
    {
        public void connect(BluetoothDevice device)
        {
            String name = device.getName();
            if (name == null)
            {
                name = "None";
            }
            Log.d("BleBinder.connect", name);

            bluetoothGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
        }
    }

    BleBinder bleBinder = new BleBinder();

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return bleBinder;
    }

    @Override
    public void onDestroy()
    {
        Log.d("service", "onDestroy");

        if (bluetoothGatt != null)
        {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}
