package edu.uah.cpe.amdessapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

public class BluetoothLeService extends Service
{
    public class DeviceInfo
    {
        public boolean connected = false;
    }

    // ------ GATT Callback ------
    private class BtGattCallback extends BluetoothGattCallback
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.d("onConnectionStateChange", "Connected");

                setConnectionStatus(address, true);

                broadcastUpdate(Constants.ACTION_GATT_CONNECTED, address);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d("onConnectionStateChange", "Disconnected");

                setConnectionStatus(address, false);

                broadcastUpdate(Constants.ACTION_GATT_DISCONNECTED, address);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            if (characteristic.getUuid().equals(Constants.UUID_IMMEDIATE_ALERT))
            {
                onReceiveImmediateAlert(gatt, characteristic);
            }
        }
    }

    // ------ Binder ------
    public class BleBinder extends Binder
    {
        public void connect(BluetoothDevice device)
        {
            bluetoothGatt = device.connectGatt(getApplicationContext(), true, gattCallback);
        }
    }

    private static final int NOTIFICATION_ID = 123;
    private final long[] VIBRATE_PATTERN = {0, 400, 100, 400, 100, 400, 100, 400};
    private final BtGattCallback gattCallback = new BtGattCallback();
    private BleBinder bleBinder = new BleBinder();
    private BluetoothGatt bluetoothGatt = null;
    static private HashMap<String, DeviceInfo> infoMap = new HashMap<>();

    static public DeviceInfo getDeviceInfo(String address)
    {
        return infoMap.get(address);
    }

    public BluetoothLeService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return bleBinder;
    }

    @Override
    public void onDestroy()
    {
        if (bluetoothGatt != null)
        {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private void setConnectionStatus(String address, boolean connected)
    {
        // get the current info for the device
        DeviceInfo info = infoMap.get(address);

        // if the info does not exist, create it
        if (info == null)
        {
            info = new DeviceInfo();
        }

        info.connected = connected;

        // update the info in the map
        infoMap.put(address, info);
    }

    private void onReceiveImmediateAlert(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        Log.d("onReceiveImmediateAlert", "Received immediate alert");

        String name = gatt.getDevice().getName();
        String address = gatt.getDevice().getAddress();
        if (name == null)
        {
            name = address;
        }
        String text = String.format("Alarm from %s", name);

        // send broadcast
        broadcastUpdate(Constants.ACTION_DEVICE_ALARM, address);

        // send notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder =
                new Notification.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Alarm!")
                    .setContentText(text)
                    .setVibrate(VIBRATE_PATTERN);
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void broadcastUpdate(String action, String address)
    {
        Intent intent = new Intent(action);
        intent.putExtra(Constants.INFO_DEVICE_ADDRESS, address);
        sendBroadcast(intent);
    }
}
