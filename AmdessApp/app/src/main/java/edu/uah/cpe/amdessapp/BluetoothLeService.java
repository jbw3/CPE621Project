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

public class BluetoothLeService extends Service
{
    private BluetoothGatt bluetoothGatt = null;

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
                Intent intent = new Intent(Constants.ACTION_GATT_CONNECTED);
                intent.putExtra(Constants.INFO_DEVICE_ADDRESS, address);
                sendBroadcast(intent);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d("onConnectionStateChange", "Disconnected");
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
}
