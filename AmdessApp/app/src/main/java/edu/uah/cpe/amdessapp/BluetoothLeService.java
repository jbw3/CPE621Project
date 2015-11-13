package edu.uah.cpe.amdessapp;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
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

                Intent intent = new Intent(Constants.ACTION_GATT_CONNECTED);
                intent.putExtra(Constants.INFO_DEVICE_ADDRESS, address);
                sendBroadcast(intent);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d("onConnectionStateChange", "Disconnected");

                setConnectionStatus(address, false);

                Intent intent = new Intent(Constants.ACTION_GATT_DISCONNECTED);
                intent.putExtra(Constants.INFO_DEVICE_ADDRESS, address);
                sendBroadcast(intent);
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
}
