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
import android.view.View;
import android.widget.TextView;

import java.util.UUID;

public class DeviceActivity extends AppCompatActivity
{
    private class BluetoothInfoReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("onReceive", "start...");

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
    private TextView connectionStatusTextView;
    private TextView armedStatusTextView;
    private TextView alarmTextView;
    private TextView servicesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        connectionStatusTextView = (TextView) findViewById(R.id.connectionStatusTextView);
        armedStatusTextView = (TextView) findViewById(R.id.armStatusTextView);
        alarmTextView = (TextView) findViewById(R.id.alarmTextView);
        servicesTextView = (TextView) findViewById(R.id.servicesTextView);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // add actions to intent filter
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DEVICE_ARM);
        intentFilter.addAction(Constants.ACTION_DEVICE_ALARM);
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

    public void onArmDisarmClicked(View v)
    {
        Log.d("onArmDisarmClicked", "TODO: Arm/Disarm AMDeSS device");
    }

    private void syncWithInfo()
    {
        Log.d("syncWithInfo", "start");

        BluetoothLeService.DeviceInfo info = BluetoothLeService.getDeviceInfo(address);
        if (info == null)
        {
            Log.w("syncWithInfo", "info is null!!!");
            connectionStatusTextView.setText("Disconnected");
            connectionStatusTextView.setTextColor(Constants.RED_COLOR);
            armedStatusTextView.setText("Disarmed");
            armedStatusTextView.setTextColor(Constants.GREEN_COLOR);
            alarmTextView.setText("");
            servicesTextView.setText("Services:\n");
            return;
        }

        // update connection status
        String connectionStatus;
        int color;
        if (info.connected)
        {
            Log.d("syncWithInfo", "Connected");
            connectionStatus = "Connected";
            color = Constants.GREEN_COLOR;
        }
        else
        {
            Log.d("syncWithInfo", "Disconnected");
            connectionStatus = "Disconnected";
            color = Constants.RED_COLOR;
        }
        connectionStatusTextView.setText(connectionStatus);
        connectionStatusTextView.setTextColor(color);

        // update armed status
        String armedStatus;
        int armedColor;
        if (info.armed)
        {
            Log.d("syncWithInfo", "Armed");
            armedStatus = "Armed";
            armedColor = Constants.RED_COLOR;
        }
        else
        {
            Log.d("syncWithInfo", "Disarmed");
            armedStatus = "Disarmed";
            armedColor = Constants.GREEN_COLOR;
        }
        armedStatusTextView.setText(armedStatus);
        armedStatusTextView.setTextColor(armedColor);

        // update alarm state
        String alarmState = "";
        if (info.alarming)
        {
            alarmState = "Alarm!";
        }
        alarmTextView.setText(alarmState);

        // update services
        String services = "Services:\n";
        for (UUID id : info.services)
        {
            String serviceName = Constants.GATT_SERVICE_NAMES.get(id);
            if (serviceName == null)
            {
                serviceName = "???";
            }

            services = services.concat(String.format("%s (%s)\n", id.toString(), serviceName));
        }

        servicesTextView.setText(services);
    }
}
