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
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

public class DeviceActivity extends AppCompatActivity
{
    private class BluetoothInfoReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
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
    private TextView batteryTextView;
    private TextView capacitanceTextView;
    private TextView servicesTextView;
    private Button armStateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        connectionStatusTextView = (TextView) findViewById(R.id.connectionStatusTextView);
        armedStatusTextView = (TextView) findViewById(R.id.armStatusTextView);
        alarmTextView = (TextView) findViewById(R.id.alarmTextView);
        batteryTextView = (TextView) findViewById(R.id.batteryTextView);
        capacitanceTextView = (TextView) findViewById(R.id.capacitanceTextView);
        servicesTextView = (TextView) findViewById(R.id.servicesTextView);
        armStateButton = (Button) findViewById(R.id.armButton);

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
        intentFilter.addAction(Constants.ACTION_DEVICE_BATTERY_LEVEL);
        intentFilter.addAction(Constants.ACTION_DEVICE_CAPACITANCE);
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
        Intent intent = new Intent(Constants.ACTION_TOGGLE_ARM_STATE);
        intent.putExtra(Constants.INFO_DEVICE_ADDRESS, address);
        sendBroadcast(intent);
    }

    private void syncWithInfo()
    {
        BluetoothLeService.DeviceInfo info = BluetoothLeService.getDeviceInfo(address);
        if (info == null)
        {
            Log.w("syncWithInfo", "info is null!!!");
            connectionStatusTextView.setText("Disconnected");
            connectionStatusTextView.setTextColor(Constants.RED_COLOR);
            armedStatusTextView.setText("Disarmed");
            armedStatusTextView.setTextColor(Constants.GREEN_COLOR);
            armStateButton.setText("Arm");
            alarmTextView.setText("");
            batteryTextView.setText("");
            capacitanceTextView.setText("Capacitance:");
            servicesTextView.setText("Services:\n");
            return;
        }

        // update connection status
        String connectionStatus;
        int color;
        if (info.connected)
        {
            connectionStatus = "Connected";
            color = Constants.GREEN_COLOR;
        }
        else
        {
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
            armStateButton.setText("Disarm");
        }
        else
        {
            Log.d("syncWithInfo", "Disarmed");
            armedStatus = "Disarmed";
            armedColor = Constants.GREEN_COLOR;
            armStateButton.setText("Arm");
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

        // update capacitance
        double capacitance = 4096.0 * info.rawCapacitance / 16777215.0;
        String capStr = String.format("Capacitance: %.1f fF", capacitance);
        capacitanceTextView.setText(capStr);

        // update battery level
        String batteryLevel = "";
        if (info.batteryLevel >= 0 && info.batteryLevel <= 100)
        {
            batteryLevel = String.format("Battery: %s%%", info.batteryLevel);
        }
        batteryTextView.setText(batteryLevel);

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
