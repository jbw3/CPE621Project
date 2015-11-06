package edu.uah.cpe.amdessapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class SensorListActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 0xB;

    private BluetoothAdapter btAdapter = null;
    private ArrayList<String> devices;
    private ArrayAdapter<String> devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // set up list view and adapter
        devices = new ArrayList<String>();
        devicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);
        ListView listView = (ListView) findViewById(R.id.devicesListView);
        listView.setAdapter(devicesAdapter);

        // check for Bluetooth support
        Button scanButton = (Button) findViewById(R.id.scanButton);
        // if Bluetooth is not supported, disable the scan button
        if (btAdapter == null)
        {
            scanButton.setEnabled(false);
        }
        else // enable the scan button and load bonded devices
        {
            scanButton.setEnabled(true);
            loadBondedDevices();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (btAdapter != null)
        {
            // if bluetooth is disabled, ask to enable it
            if (!btAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void onScanClick(View v)
    {
        // launch the scan activity
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void loadBondedDevices()
    {
        for (BluetoothDevice device : btAdapter.getBondedDevices())
        {
            String name = device.getName();
            if (name == null)
            {
                name = "None";
            }

            String str = String.format("%s (%s)", name, device.getAddress());
            devices.add(str);
        }
        devicesAdapter.notifyDataSetChanged();
    }
}
