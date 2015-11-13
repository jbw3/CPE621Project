package edu.uah.cpe.amdessapp;

import android.app.ActionBar;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;

public class DeviceActivity extends AppCompatActivity
{
    private class BluetoothInfoReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("onReceive", "I got here!!!!!!!!!!!!!!!!!!!!!!!");

            String action = intent.getAction();
            String deviceAddress = intent.getStringExtra(Constants.INFO_DEVICE_ADDRESS);

            if (deviceAddress == null || deviceAddress.isEmpty())
            {
                return;
            }

            // get the current info for the device
            DeviceInfo info = infoMap.get(deviceAddress);

            // if the info does not exist, create it
            if (info == null)
            {
                info = new DeviceInfo();
            }

            if (action.equals(Constants.ACTION_GATT_CONNECTED))
            {
                info.connected = true;
            }
            else if (action.equals(Constants.ACTION_GATT_DISCONNECTED))
            {
                info.connected = false;
            }

            // update the info in the map
            infoMap.put(deviceAddress, info);

            if (address.equals(deviceAddress))
            {
                syncWithInfo();
            }
        }
    }

    private class DeviceInfo
    {
        public boolean connected = false;
    }

    private String address = "";
    private HashMap<String, DeviceInfo> infoMap = new HashMap<>();
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
        DeviceInfo info = infoMap.get(address);
        if (info == null)
        {
            return;
        }

        String connectionStatus = info.connected ? "Connected" : "Disconnected";

        TextView connectionStatusTextView = (TextView) findViewById(R.id.connectionStatusTextView);
        connectionStatusTextView.setText(connectionStatus);
    }
}
