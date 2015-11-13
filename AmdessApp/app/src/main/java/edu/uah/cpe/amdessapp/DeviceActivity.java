package edu.uah.cpe.amdessapp;

import android.app.ActionBar;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DeviceActivity extends AppCompatActivity
{
    private class BluetoothInfoReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("onReceive", "I got here!!!!!!!!!!!!!!!!!!!!!!!");

            String deviceAddress = intent.getStringExtra(Constants.INFO_DEVICE_ADDRESS);

            if (deviceAddress == null || deviceAddress.isEmpty())
            {
                Log.w("onReceive", "deviceAddress is null or an empty string");
                return;
            }

            if (address.equals(deviceAddress))
            {
                syncWithInfo();
            }
        }
    }

    private String address = "";
    private BluetoothInfoReceiver infoReceiver = new BluetoothInfoReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // add actions to intent filter
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent intent = getIntent();
        address = intent.getStringExtra(Constants.INFO_DEVICE_ADDRESS);

        BluetoothDevice device = AmdessDevices.getInstance().getDevice(address);

        // device name
        String name = device.getName();
        if (name == null)
        {
            name = "None";
        }
        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
        nameTextView.setText(name);

        // device address
        TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
        addressTextView.setText(address);

        syncWithInfo();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        registerReceiver(infoReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        unregisterReceiver(infoReceiver);
    }

    private void syncWithInfo()
    {
        Log.d("syncWithInfo", "start");
        BluetoothLeService.DeviceInfo info = BluetoothLeService.getDeviceInfo(address);
        if (info == null)
        {
            Log.w("syncWithInfo", "info is null!!!");
            return;
        }

        TextView connectionStatusTextView = (TextView) findViewById(R.id.connectionStatusTextView);

        String connectionStatus;
        int color;
        if (info.connected)
        {
            Log.d("syncWithInfo", "Connected");
            connectionStatus = "Connected";
            color = Color.GREEN;
        }
        else
        {
            Log.d("syncWithInfo", "Disconnected");
            connectionStatus = "Disconnected";
            color = Color.RED;

        }
        connectionStatusTextView.setText(connectionStatus);
        connectionStatusTextView.setTextColor(color);
    }
}
