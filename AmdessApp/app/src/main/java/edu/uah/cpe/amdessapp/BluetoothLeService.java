package edu.uah.cpe.amdessapp;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BluetoothLeService extends Service
{
    private BluetoothGatt bluetoothGatt = null;

    // ------ GATT Callback ------
    private class BtGattCallback extends BluetoothGattCallback
    {
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
