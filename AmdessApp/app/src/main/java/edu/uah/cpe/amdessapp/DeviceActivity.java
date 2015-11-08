package edu.uah.cpe.amdessapp;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DeviceActivity extends AppCompatActivity
{
    private String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Intent intent = getIntent();
        address = intent.getStringExtra(SensorListActivity.DEVICE_ADDRESS_ID);

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
    }
}
